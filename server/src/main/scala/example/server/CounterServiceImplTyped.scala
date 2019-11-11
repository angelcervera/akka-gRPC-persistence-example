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

class CounterServiceImplTyped(counter: ActorRef[CounterActorTyped.Command])(
  implicit
  executionContext: ExecutionContext,
  scheduler: Scheduler
) extends api.CounterService {

  // FIXME: Temporal timeout for the example
  implicit val timeout = Timeout(15 minutes)

  override def inc(in: api.Increment): Future[api.Done] = ???

  override def get(in: api.Empty): Future[api.State] = ???

  override def incs(in: Source[api.Increment, NotUsed]): Source[api.Done, NotUsed] = ???

}
