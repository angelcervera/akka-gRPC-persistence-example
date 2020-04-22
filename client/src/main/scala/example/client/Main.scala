package example.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import example.api.{CounterService, CounterServiceClient, Empty, Increment}

import scala.util.{Failure, Success}

object Main extends App {

  val help =
    """
      | Invalid parameters.
      | - No parameters, print the state of the counter.
      | - One parameter, increment the counter the value of the parameter.
      | - Two parameters, execute increment the counter using the range as parameters.
      |""".stripMargin

  implicit val sys = ActorSystem("CounterClient")
  import sys.dispatcher

  val clientSettings = GrpcClientSettings.fromConfig(CounterService.name)
  val client: CounterService = CounterServiceClient(clientSettings)

  args match {
    case Array()         => printState
    case Array(value)    => increment(value.toInt)
    case Array(from, to) => incrementStream(from.toInt.to(to.toInt))
    case _               => println(help)
  }

  private def increment(i: Long) =
    client
      .inc(Increment(i))
      .onComplete {
        case Success(_) =>
          println("Successfully incremented.")
          sys.terminate
        case Failure(e) =>
          sys.log.error("Error incrementing: {}", e.getMessage)
          e.printStackTrace()
          sys.terminate
      }

  private def printState() =
    client
      .get(Empty())
      .onComplete {
        case Success(state) =>
          println(
            s"Current state: Accumulator [${state.acc}] / Events executed [${state.events}]."
          )
          sys.terminate
        case Failure(e) =>
          sys.log.error("Error printing: {}", e.getMessage)
          e.printStackTrace()
          sys.terminate
      }

  private def incrementStream(range: Range) = {

    val reply =
      client
        .incs(Source(range).map(Increment(_)))
        .runForeach(reply => println(s"Incremented ${reply}"))

    reply
      .onComplete {
        case Success(_) =>
          println(s"Incremented range [${range}].")
          sys.terminate
        case Failure(e) =>
          sys.log.error("Error incrementing range: {}", e.getMessage)
          e.printStackTrace()
          sys.terminate
      }

  }
}
