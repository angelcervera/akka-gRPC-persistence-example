package example

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import example.CounterActor.{Event, GetState, Increment, Incremented, State}

object CounterActor {
  def props(id: String): Props = Props(new CounterActor(id))

  case class State(events: Long, acc: Long)

  trait Command

  case class Increment(v: Long) extends Command

  case object GetState extends Command

  trait Event

  case class Incremented(v: Long) extends Event
}

class CounterActor(id: String)
  extends PersistentActor
  with ActorLogging {

  var state = State(0,0)

  override def persistenceId: String = id

  override def receiveRecover: Receive = {
    case event: Event => applyEvent(event)
  }

  override def receiveCommand: Receive = {
    case GetState =>
      sender() ! state

    case Increment(v) =>
      persist(Incremented(v)) { incremented =>
        applyEvent(incremented)
      }
  }

  private def applyEvent(event: Event) = event match {
    case Incremented(v) =>
      state = State(state.events + 1, state.acc + v)
  }

}
