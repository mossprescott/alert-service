package alerts

import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  val db = AlertsDb(Seq("192.168.99.100"), 9042, "demo")
  
  // Note: this is a lame but effective way to generate unique ids, assuming
  // that the DB is empty when the server starts.
  val seq = new java.util.concurrent.atomic.AtomicInteger(1)
  
  def identified(alert: Alert) = 
    new IdentifiedAlert(AlertId(seq.getAndIncrement()), alert)
  
  val eventHandler = new EventService {
    def receive(user: UserId, hr: HeartRate, bp: BloodPressure): Unit = {
      println(s"received: $user; $hr; $bp")  // DEBUG
      
      rules.heartRateAlert(user, hr)
        .map(a => db.alerts.store(identified(a)))  // TODO: log failure/success
      rules.lowBloodPressureAlert(user, hr, bp)
        .map(a => db.alerts.store(identified(a)))  // TODO: log failure/success
      
      ()
    }
  }
  
  val alertHandler = new AlertService {
    def activeAlerts: Future[Seq[IdentifiedAlert]] = 
      db.alerts.getActive
    
    def activeAlertsByUser(id: UserId): Future[Seq[IdentifiedAlert]] =
      db.alerts.getActiveByUser(id)
      
    def acknowledgeAlert(user: UserId, id: AlertId): Future[Unit] = 
      db.alerts.delete(user, id)
  }
  
  val events1 = rpc.EventServer(9000, eventHandler)
  events1.start(ExecutionContext.global)

  val alerts = rpc.AlertServer(9001, alertHandler)
  alerts.start(ExecutionContext.global)

  events1.blockUntilShutdown()
}