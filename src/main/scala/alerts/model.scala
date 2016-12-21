package alerts

// "newtypes" for the relevant data
case class UserId(value: Int) extends AnyVal
case class HeartRate(value: Int) extends AnyVal
case class BloodPressure(systolic: Int, diastolic: Int)

case class PairedEvent(id: UserId, hr: HeartRate, bp: BloodPressure)

sealed trait Alert                  
case class AbnormalHeartRateAlert(userId: UserId, hr: HeartRate) extends Alert
case class LowBloodPressureAlert(userId: UserId, bp: BloodPressure, hr: HeartRate) extends Alert
