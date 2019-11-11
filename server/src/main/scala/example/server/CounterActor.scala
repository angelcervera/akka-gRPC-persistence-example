package example.server

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

object CounterActor {
  def props(id: String): Props = Props(new CounterActor(id))

  case class State(events: Long, acc: Long)
  case class Done()

  trait Command

  case class Increment(v: Long) extends Command

  case object GetState extends Command

  trait Event

  case class Incremented(v: Long) extends Event
}

class CounterActor(id: String)
  extends PersistentActor
    with ActorLogging {

  var state = CounterActor.State(0,0)

  override def persistenceId: String = id

  override def receiveRecover: Receive = {
    case event: CounterActor.Event => applyEvent(event)
  }

  override def receiveCommand: Receive = {
    case CounterActor.GetState =>
      sender() ! state

    case CounterActor.Increment(v) =>
      persist(CounterActor.Incremented(v)) { incremented =>
        applyEvent(incremented)
        sender() ! CounterActor.Done()
      }
  }

  private def applyEvent(event: CounterActor.Event) = event match {
    case CounterActor.Incremented(v) =>
      state = CounterActor.State(state.events + 1, state.acc + v)
  }

}
