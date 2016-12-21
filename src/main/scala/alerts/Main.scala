package alerts

import scala.concurrent.ExecutionContext

object Main extends App {
  val events1 = EventServer(9000, evt => println(s"received: $evt"))
  events1.start(ExecutionContext.global)
  // events1.blockUntilShutdown()
  
  java.lang.Thread.sleep(10000)
}