package example.server

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
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
      counter ! CounterActor.GetState
      expectMsg(CounterActor.State(0,0))
    }

    "Increment and count events processed" in {
      val counter = system.actorOf(CounterActor.props("test_inc"))
      counter ! CounterActor.Increment(10)
      counter ! CounterActor.GetState
      expectMsg(CounterActor.Done())
      expectMsg(CounterActor.State(1,10))
    }
  }

}
