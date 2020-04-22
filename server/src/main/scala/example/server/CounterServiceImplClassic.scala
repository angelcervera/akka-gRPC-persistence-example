package example.server

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import example.api

import scala.concurrent.Future
import scala.concurrent.duration._

class CounterServiceImplClassic(counter: ActorRef)(implicit mat: Materializer)
    extends api.CounterService {

  import mat.executionContext

  implicit val timeout = Timeout(15.minutes)

  override def inc(in: api.Increment): Future[api.Done] =
    (counter ? CounterActorClassic.Increment(in.v))
      .mapTo[CounterActorClassic.Done]
      .map(_ => api.Done())

  override def get(in: api.Empty): Future[api.State] =
    (counter ? CounterActorClassic.GetState)
      .mapTo[CounterActorClassic.State]
      .map(s => api.State(s.events, s.acc))

  override def incs(
    in: Source[api.Increment, NotUsed]
  ): Source[api.Done, NotUsed] =
    in.map(i => CounterActorClassic.Increment(i.v))
      .ask[CounterActorClassic.Done](counter)
      .map(_ => api.Done())

}
