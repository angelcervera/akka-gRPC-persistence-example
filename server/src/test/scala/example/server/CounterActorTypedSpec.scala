package example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

class CounterActorTypedSpec
  extends ScalaTestWithActorTestKit
  with WordSpecLike
  with Matchers {

  "CounterActorTyped" should {
    "Start with zero" in {
      val counter = testKit.spawn(CounterActorTyped("test_zero"), "test_zero")
      val probe = testKit.createTestProbe[CounterActorTyped.State]()
      counter ! CounterActorTyped.GetState(probe.ref)
      probe.expectMessage(CounterActorTyped.State(0,0))
    }

    "Increment and count events processed" in {
      val counter = testKit.spawn(CounterActorTyped("test_zero"), "test_inc")
      val probe = testKit.createTestProbe[CounterActorTyped.State]()
      val response = testKit.createTestProbe[CounterActorTyped.Done]()
      counter ! CounterActorTyped.Increment(10, response.ref)
      counter ! CounterActorTyped.GetState(probe.ref)
      probe.expectMessage(CounterActorTyped.State(1,10))
    }
  }

  "Testing the AKKA Persistence" should {
    "recover" in {
      implicit val timeout = Timeout(3 seconds)


      val response = testKit.createTestProbe[CounterActorTyped.Done]()

      val counter = testKit.spawn(CounterActorTyped("testing_recovering"), "testing_initializing")
      1 to 100 foreach (_ => counter ! CounterActorTyped.Increment(10, response.ref ))

      val probe = testKit.createTestProbe[CounterActorTyped.State]()
      counter ! CounterActorTyped.GetState(probe.ref)

      probe.expectMessage(CounterActorTyped.State(100, 1000))

      testKit.stop(counter)

      val counterRecovered = testKit.spawn(CounterActorTyped("testing_recovering"), "testing_recovered")
      counterRecovered ! CounterActorTyped.GetState(probe.ref)
      probe.expectMessage(CounterActorTyped.State(100, 1000))
    }

  }


}
