package example.server

import akka.actor.ActorSystem
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import example.api.CounterServiceHandler

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App {

  val config = ConfigFactory
    .parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  implicit val system = ActorSystem("CounterServer", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val counterActor = system.actorOf(CounterActor.props("CounterActorId"))

  val counterServiceHandler = CounterServiceHandler.partial(new CounterServiceImpl(counterActor))
  // val anotherServiceHandler = ....

  val serviceHandlers: HttpRequest => Future[HttpResponse] =
    ServiceHandler.concatOrNotFound(
      counterServiceHandler
      /*, anotherServiceHandler*/
    )

  val serverBinding = Http().bindAndHandleAsync(
    serviceHandlers,
    interface = "localhost",
    port = 8080,
    connectionContext = HttpConnectionContext(http2 = Always)
  )

  serverBinding.onComplete {
    case Success(bound) =>
      println(
        s"Counter server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
      )
    case Failure(e) =>
      Console.err.println(s"Counter server can not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
