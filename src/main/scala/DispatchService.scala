import java.util.UUID

import DispatchActor._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.softwaremill.react.kafka.KafkaMessages.KafkaMessage
import com.softwaremill.react.kafka.ReactiveKafka
import conf.KafkaConfiguration

class DispatchService(implicit fm: Materializer, system: ActorSystem) extends Directives {
  // Currently only one DispatchActor per service
  // This actor is what holds a reference to all the other actors subscribed
  lazy val dispatchActor = system.actorOf(Props[DispatchActor])

  // Wrapping our dispatchActor in a sink, all elements in stream will be sent to this actor
  // We instruct the sink send an Unsubscribe message when the stream terminates
  def dispatchInSink(sender: String) = Sink.actorRef[DispatchEvent](dispatchActor, Unsubscribe(sender))

  // Constructs and returns a flow that takes a received message, dispatches it to all subscribed actors
  def dispatchActorFlow(sender: String): Flow[ReceivedMessage, KafkaMessage[String], Unit] = {
    val in = Flow[ReceivedMessage].to(dispatchInSink(sender))

    val kafka = new ReactiveKafka

    val out: Source[KafkaMessage[String], Unit] = Source.fromPublisher(kafka.consume(KafkaConfiguration.consumerProperties("consumer" + UUID.randomUUID().toString)))
    // When the flow is materialized for the first time (websocket connect) we create an actor for that flow
    // The newly created ActorRef is sent to our DispatchActor in a subscribe message so the DispatchActor will
    // broadcast new messages to this actor
    Flow.fromSinkAndSource(in, out)
  }

  // The flow from beginning to end to be passed into handleWebsocketMessages
  def websocketDispatchFlow(sender: String): Flow[Message, Message, Unit] =
    Flow[Message]
      // First we convert the TextMessage to a ReceivedMessage
      .collect {
      case TextMessage.Strict(msg) => ReceivedMessage(sender, msg)
    }
      // Then we send the message to the dispatch actor which fans it out
      .via(dispatchActorFlow(sender))
      // The message is converted back to a TextMessage for serialization across the socket
      .map {
      (message: Any) => new TextMessage.Strict(message.toString)
    }

  def route =
    (get & path("chat") & parameter('name)) { name =>
      handleWebsocketMessages(websocketDispatchFlow(sender = name))
    }
}