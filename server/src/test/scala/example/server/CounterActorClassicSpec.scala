package example.server

import akka.actor.{ActorSystem, Kill}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CounterActorClassicSpec
  extends TestKit(ActorSystem("CounterActorSpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  "CounterActor" should {
    "Start with zero" in {
      val counter = system.actorOf(CounterActorClassic.props("test_zero"))
      counter ! CounterActorClassic.GetState
      expectMsg(CounterActorClassic.State(0,0))
    }

    "Increment and count events processed" in {
      val counter = system.actorOf(CounterActorClassic.props("test_inc"))
      counter ! CounterActorClassic.Increment(10)
      counter ! CounterActorClassic.GetState
      expectMsg(CounterActorClassic.Done())
      expectMsg(CounterActorClassic.State(1,10))
    }
  }

  "Testing the AKKA Persistence" should {
    "recover" in {
      val counter = system.actorOf(CounterActorClassic.props("testing_recovering"), "testing_initializing")
      1 to 100 foreach (_ => counter ! CounterActorClassic.Increment(10))

      counter ! CounterActorClassic.GetState
      receiveN(100)
      expectMsg(CounterActorClassic.State(100, 1000))

      counter ! Kill

      val counterRecovered = system.actorOf(CounterActorClassic.props("testing_recovering"), "testing_recovered")
      counterRecovered ! CounterActorClassic.GetState
      expectMsg(CounterActorClassic.State(100, 1000))
    }
  }

}
