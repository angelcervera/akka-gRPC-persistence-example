package example.server

import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import com.typesafe.config.{Config, ConfigFactory}
import example.CounterActorTyped
import example.api.CounterServiceHandler

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object MainTyped {

  def run(defaultConfig: Config): Unit = {
    val config = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(defaultConfig)

    implicit val actor =
      ActorSystem(CounterActorTyped("CounterActorId"), "CounterActor", config)
    import actor.executionContext
    implicit val scheduler: Scheduler = actor.scheduler

    val counterServiceHandler =
      CounterServiceHandler.partial(new CounterServiceImplTyped(actor))
    // val anotherServiceHandler = ....

    val serviceHandlers: HttpRequest => Future[HttpResponse] =
      ServiceHandler.concatOrNotFound(
        counterServiceHandler
        /*, anotherServiceHandler*/
      )

    val serverBinding =
      Http()(system = actor.toClassic).bindAndHandleAsync(
        serviceHandlers,
        interface = config.getString("server.interface"),
        port = config.getInt("server.port"),
        connectionContext = HttpConnectionContext()
      )

    serverBinding.onComplete {
      case Success(bound) =>
        println(
          s"Counter server typed online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
        )
      case Failure(e) =>
        Console.err.println(s"Counter server typed can not start!")
        e.printStackTrace()
        actor.terminate()
    }

    Await.result(actor.whenTerminated, Duration.Inf)
  }

}
