package alerts

import com.websudos.phantom.dsl._
import com.datastax.driver.core.{SocketOptions}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class Alerts extends CassandraTable[Alerts, IdentifiedAlert] {
  object alertId extends IntColumn(this) with PrimaryKey[Int]
  object userId extends IntColumn(this) with PartitionKey[Int]
  object heartRate extends IntColumn(this)
  // Note: the presence of the last two fields distinguishes a 
  // LowBloodPressure alert from an AbnormalHeartRate alert.
  object systolic extends OptionalIntColumn(this)
  object diastolic extends OptionalIntColumn(this)
  
  def fromRow(row: Row): IdentifiedAlert =
    IdentifiedAlert(
      AlertId(alertId(row)),
      (systolic(row), diastolic(row)) match {
        case (Some(sys), Some(dia)) => 
          LowBloodPressureAlert(
            UserId(userId(row)),
            BloodPressure(sys, dia),
            HeartRate(heartRate(row)))
        case _ => 
          AbnormalHeartRateAlert(
            UserId(userId(row)),
            HeartRate(heartRate(row)))
      })
}

abstract class ConcreteAlerts extends Alerts with RootConnector {
  def store(ia: IdentifiedAlert): Future[ResultSet] = ia.alert match {
    case AbnormalHeartRateAlert(user, hr) =>
      insert
        .value(_.alertId, ia.id.value)
        .value(_.userId, user.value)
        .value(_.heartRate, hr.value)
        .future()
    case LowBloodPressureAlert(user, bp, hr) => 
      insert
        .value(_.alertId, ia.id.value)
        .value(_.userId, user.value)
        .value(_.heartRate, hr.value)
        .value(_.systolic, Some(bp.systolic))
        .value(_.diastolic, Some(bp.diastolic))
        .future()
  }

  def getActive: Future[Seq[IdentifiedAlert]] = 
    select.iterator.map(_.toSeq)

  def getActiveByUser(id: UserId): Future[Seq[IdentifiedAlert]] = 
    select.where(_.userId eqs id.value).iterator.map(_.toSeq)

  def delete(user: UserId, id: AlertId): Future[Unit] = 
    super.delete
      .where(_.userId eqs user.value)
      .and(_.alertId eqs id.value)
      .future().map(_ => ())
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