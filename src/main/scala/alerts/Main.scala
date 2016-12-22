package alerts

import scala.concurrent.ExecutionContext

object Main extends App {
  val db = AlertsDb(Seq("192.168.99.100"), 9042, "demo")
  
  def handleEvent(evt: PairedEvent): Unit = {
    println(s"received: $evt")
    Rules.heartRateAlert(evt.id, evt.hr)
      .map(db.alerts.store(_))  // TODO: log failure/success
    ()
  }
  
  val events1 = EventServer(9000, handleEvent)
  events1.start(ExecutionContext.global)
  // events1.blockUntilShutdown()
  
  java.lang.Thread.sleep(10000)
}