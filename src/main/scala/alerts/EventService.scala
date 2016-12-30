package alerts

import scala.concurrent.Future

/** Abstract Events service; decouples the implementation of event handling
  * from its clients (e.g. an RPC server). */
trait EventService {
  /** Accepts a new event and returns nothing, since the provider of events does
    * not need to be concerned with what is done with the event once it's accepted. */
  def receive(user: UserId, hr: HeartRate, bp: BloodPressure): Unit
}
