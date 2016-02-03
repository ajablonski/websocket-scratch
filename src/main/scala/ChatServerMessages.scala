import akka.actor.ActorRef

object ChatServerMessages {
  trait ChatEvent
  case class ReceivedMessage(sender: String, msg: String) extends ChatEvent
}