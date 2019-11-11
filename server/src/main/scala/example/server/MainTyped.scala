package example.server

import akka.actor
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.UseHttp2.Always
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import example.CounterActorTyped
import example.api.CounterServiceHandler

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object MainTyped {

  def run(defaultConfig: Config): Unit = {
    val config = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(defaultConfig)

    val system = ActorSystem[akka.Done](Behaviors.setup[akka.Done] { ctx =>
      implicit val untypedSystem: actor.ActorSystem = ctx.system.toClassic
      implicit val materializer: ActorMaterializer = ActorMaterializer()(ctx.system.toClassic)
      implicit val ec: ExecutionContextExecutor = ctx.system.executionContext
      implicit val scheduler: Scheduler = ctx.system.scheduler

      val counterActor = ctx.spawn(CounterActorTyped("CounterActorId"), "CounterActor")

      val counterServiceHandler = CounterServiceHandler.partial(new CounterServiceImplTyped(counterActor))
      // val anotherServiceHandler = ....

      val serviceHandlers: HttpRequest => Future[HttpResponse] =
        ServiceHandler.concatOrNotFound(
          counterServiceHandler
          /*, anotherServiceHandler*/
        )

      val serverBinding = Http().bindAndHandleAsync(
        serviceHandlers,
        interface = config.getString("server.interface"),
        port = config.getInt("server.port"),
        connectionContext = HttpConnectionContext(http2 = Always)
      )

      serverBinding.onComplete {
        case Success(bound) =>
          println(
            s"Counter server typed online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
          )
        case Failure(e) =>
          Console.err.println(s"Counter server typed can not start!")
          e.printStackTrace()
          ctx.self ! akka.Done
      }

      Behaviors.receiveMessage {
        case akka.Done =>
          Behaviors.stopped
      }
    }, "SimplexSpatialServer")
  }
}
