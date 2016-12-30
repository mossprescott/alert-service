package alerts

// "newtypes" for the relevant data
case class UserId(value: Int) extends AnyVal
case class HeartRate(value: Int) extends AnyVal
case class BloodPressure(systolic: Int, diastolic: Int)
case class AlertId(value: Int) extends AnyVal

sealed trait Alert                  
case class AbnormalHeartRateAlert(userId: UserId, hr: HeartRate) extends Alert
case class LowBloodPressureAlert(userId: UserId, bp: BloodPressure, hr: HeartRate) extends Alert

/** Packages an alert along with its generated primary key. */
case class IdentifiedAlert(id: AlertId, alert: Alert)
