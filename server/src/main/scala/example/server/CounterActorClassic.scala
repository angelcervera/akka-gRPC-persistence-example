package example.server

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

object CounterActorClassic {
  def props(id: String): Props = Props(new CounterActorClassic(id))

  case class State(events: Long, acc: Long)
  case class Done()

  trait Command

  case class Increment(v: Long) extends Command

  case object GetState extends Command

  trait Event

  case class Incremented(v: Long) extends Event
}

class CounterActorClassic(id: String)
  extends PersistentActor
    with ActorLogging {

  var state = CounterActorClassic.State(0,0)

  override def persistenceId: String = id

  override def receiveRecover: Receive = {
    case event: CounterActorClassic.Event => applyEvent(event)
  }

  override def receiveCommand: Receive = {
    case CounterActorClassic.GetState =>
      sender() ! state

    case CounterActorClassic.Increment(v) =>
      persist(CounterActorClassic.Incremented(v)) { incremented =>
        applyEvent(incremented)
        sender() ! CounterActorClassic.Done()
      }
  }

  private def applyEvent(event: CounterActorClassic.Event) = event match {
    case CounterActorClassic.Incremented(v) =>
      state = CounterActorClassic.State(state.events + 1, state.acc + v)
  }

}
