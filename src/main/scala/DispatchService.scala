import java.util.UUID

import ChatServerMessages.ReceivedMessage
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.actor.ActorSubscriberMessage
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.softwaremill.react.kafka.KafkaMessages.KafkaMessage
import com.softwaremill.react.kafka.{KafkaMessages, ReactiveKafka}
import conf.KafkaConfiguration
import org.reactivestreams.Publisher

class DispatchService(implicit fm: Materializer, system: ActorSystem) extends Directives {
  val kafka: ReactiveKafka = new ReactiveKafka
  val objectMapper = new ObjectMapper() with ScalaObjectMapper {{
    registerModule(DefaultScalaModule)
  }}
  // Only one Kafka publish actor per server right now
  val publishActor: ActorRef = kafka.producerActor(KafkaConfiguration.producerProperties)

  def route =
    (get & path("chat") & parameter('name)) { name =>
      handleWebsocketMessages(websocketDispatchFlow(sender = name))
    }

  // The flow from beginning to end to be passed into handleWebsocketMessages
  def websocketDispatchFlow(sender: String): Flow[Message, Message, Unit] =
  Flow[Message]
      // Convert the TextMessage to a ReceivedMessage
      .collect { case TextMessage.Strict(msg) => ReceivedMessage(sender, msg) }
      // Serialize it to JSON
      .map(objectMapper.writeValueAsString)
      // Send the message to the Kafka flow, receive messages from the Kafka flow
      .via(kafkaFlow(sender))
      // Deserialize it from JSON
      .map(objectMapper.readValue[ReceivedMessage])
      // Convert back to a TextMessage for serialization across the socket
      .map { case ReceivedMessage(from, msg) => TextMessage.Strict(s"$from: $msg") }

  // Constructs and returns a flow that:
  // 1. takes its input and redirects it to Kafka
  // 2. provides as output all messages from Kafka
  def kafkaFlow(sender: String): Flow[String, String, Unit] = {
    val kafkaConsumer: Publisher[KafkaMessage[String]] = createKafkaConsumer(sender)

    val in: Sink[String, Unit] = Flow[String]
        .map[ActorSubscriberMessage.OnNext](serializedMessage => ActorSubscriberMessage.OnNext(serializedMessage))
        .to(Sink.actorRef(publishActor, ActorSubscriberMessage.OnComplete))

    val out: Source[String, Unit] = Source
        .fromPublisher(kafkaConsumer)
        .map[String](_.message())

    Flow.fromSinkAndSource(in, out)
  }

  def createKafkaConsumer(sender: String): Publisher[KafkaMessages.KafkaMessage[String]] = {
    val consumerName: String = s"consumer-$sender-${UUID.randomUUID.toString}"
    val kafkaConsumer: Publisher[KafkaMessage[String]] = kafka.consume(KafkaConfiguration.consumerProperties(consumerName))
    kafkaConsumer
  }
}