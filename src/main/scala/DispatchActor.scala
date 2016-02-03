import akka.actor.{Actor, ActorRef, Terminated}

object DispatchActor {
  trait DispatchEvent
  case class Subscribe(sender: String, subscriber: ActorRef) extends DispatchEvent
  case class Unsubscribe(sender: String) extends DispatchEvent
  case class ReceivedMessage(sender: String, msg: String) extends DispatchEvent
}