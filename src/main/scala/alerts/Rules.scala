package alerts

/** Application logic for alerts. */
object rules {
  def heartRateAlert(userId: UserId, hr: HeartRate): Option[AbnormalHeartRateAlert] = 
    if (hr.value > 190) Some(AbnormalHeartRateAlert(userId, hr))
    else None
    
  def lowBloodPressureAlert(userId: UserId, hr: HeartRate, bp: BloodPressure): Option[LowBloodPressureAlert] =
    if (bp.systolic < 100 && hr.value > 100) Some(LowBloodPressureAlert(userId, bp, hr))
    else None
}