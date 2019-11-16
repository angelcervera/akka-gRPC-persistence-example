package example

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object CounterActorTyped {

  case class State(events: Long, acc: Long)

  case class Done()

  trait Command

  case class Increment(v: Long, ref: ActorRef[Done]) extends Command

  case class GetState(replyTo: ActorRef[State]) extends Command

  trait Event

  case class Incremented(v: Long) extends Event

  def apply(counterId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(counterId),
      emptyState = State(0, 0),
      commandHandler = (state, command) => onCommand(state, command),
      eventHandler = (state, event) => applyEvent(state, event)
    )

  private def onCommand(state: State, cmd: Command): Effect[Event, State] = cmd match {
    case GetState(replyTo) =>
      replyTo ! state
      Effect.none

    case Increment(v, replyTo) =>
      Effect.persist(Incremented(v)).thenRun { _ => replyTo ! Done() }
  }

  private def applyEvent(state: State, event: Event): State = event match {
    case Incremented(v) =>
      State(state.events + 1, state.acc + v)
  }
}
