package example

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import example.CounterActor.{GetState, Increment, State}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class CounterActorSpec
  extends TestKit(ActorSystem("CounterActorSpec"))
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  "CounterActor" should {
    "Start with zero" in {
      val counter = system.actorOf(CounterActor.props("test_zero"))
      counter ! GetState
      expectMsg(State(0,0))
    }

    "Increment and count events processed" in {
      val counter = system.actorOf(CounterActor.props("test_zero"))
      counter ! Increment(10)
      counter ! GetState
      expectMsg(State(1,10))
    }
  }

}
