package alerts

import com.websudos.phantom.dsl._
import com.datastax.driver.core.{SocketOptions}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Alerts extends CassandraTable[Alerts, AbnormalHeartRateAlert] {
  object userId extends IntColumn(this) with PartitionKey[Int]
  object heartRate extends IntColumn(this)
  
  def fromRow(row: Row): AbnormalHeartRateAlert =
    AbnormalHeartRateAlert(
      UserId(userId(row)),
      HeartRate(heartRate(row)))
  
}

abstract class ConcreteAlerts extends Alerts with RootConnector {
  def store(alert: AbnormalHeartRateAlert): Future[ResultSet] =
    insert
      .value(_.userId, alert.userId.value)
      .value(_.heartRate, alert.hr.value)
      .future()

  def getById(userId: UserId): Future[Option[AbnormalHeartRateAlert]] = 
    select.where(_.userId eqs userId.value).one()
}

class AlertsDb(override val connector: KeySpaceDef) extends Database(connector) {
  object alerts extends ConcreteAlerts with connector.Connector
}
object AlertsDb {
  def apply(hosts: Seq[String], port: Int, keyspace: String): AlertsDb = {
    val connector = ContactPoints(hosts, port)
                      .noHeartbeat()
                      .keySpace(keyspace)
    new AlertsDb(connector)
  }
}