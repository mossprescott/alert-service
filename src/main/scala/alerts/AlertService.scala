package alerts

import scala.concurrent.Future

/** Abstract Alerts service; decouples the implementation of alert persistence 
  * from its clients (e.g. an RPC server). */
trait AlertService {
  def activeAlerts: Future[Seq[IdentifiedAlert]]
  def activeAlertsByUser(id: UserId): Future[Seq[IdentifiedAlert]]
  def acknowledgeAlert(user: UserId, id: AlertId): Future[Unit]
}
