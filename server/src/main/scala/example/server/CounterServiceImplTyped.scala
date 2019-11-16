package example.server

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, Scheduler}
import akka.stream.scaladsl.Source
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import example.{CounterActorTyped, api}

// TODO: Check https://doc.akka.io/docs/akka/2.6/typed/from-classic.html#ask and https://doc.akka.io/docs/akka/2.6/typed/from-classic.html#pipeto
class CounterServiceImplTyped(counter: ActorRef[CounterActorTyped.Command])(
  implicit
  executionContext: ExecutionContext,
  scheduler: Scheduler
) extends api.CounterService {

  // FIXME: Temporal timeout for the example
  implicit val timeout = Timeout(1 minutes)

  override def inc(in: api.Increment): Future[api.Done] =
    counter
      .ask[CounterActorTyped.Done](replyTo => CounterActorTyped.Increment(in.v, replyTo))
      .map(_ => api.Done())

  override def get(in: api.Empty): Future[api.State] =
    counter
      .ask[CounterActorTyped.State](replyTo => CounterActorTyped.GetState(replyTo))
      .map(s => api.State(s.events, s.acc))

  override def incs(in: Source[api.Increment, NotUsed]): Source[api.Done, NotUsed] =
    in
      .via(ActorFlow.ask(counter)((inc, replyTo: ActorRef[CounterActorTyped.Done]) => CounterActorTyped.Increment(inc.v, replyTo)))
      .map(_ => api.Done())

}
