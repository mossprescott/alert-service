package alerts

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver

object EventServer {
  /** Accepts a new event and returns nothing, since the provider of events does
    * not need to be concerned with what is done with the event once it's accepted. */
  type EventHandler = PairedEvent => Unit
  
  private def fromMessage(msg: events1.Event): PairedEvent = 
    PairedEvent(UserId(msg.userId), HeartRate(msg.heartRate), BloodPressure(msg.systolic, msg.diastolic))
  
  private class Events1Service(handler: EventHandler) extends events1.EventsGrpc.Events {
    def receiveOne(evt: events1.Event): Future[events1.Empty] = {
      handler(fromMessage(evt))
      Future.successful(events1.Empty())
    }
      
    def receiveStream(responseObserver: StreamObserver[events1.Empty]): StreamObserver[events1.Event] = 
      new StreamObserver[events1.Event] {
        def onNext(evt: alerts.events1.Event): Unit = {
          handler(fromMessage(evt))
          responseObserver.onNext(events1.Empty())
        }
        def onCompleted(): Unit = responseObserver.onCompleted()
        def onError(t: Throwable): Unit = ()
      }
  }
  
  def apply(port: Int, handler: EventHandler): GrpcServer = 
    new GrpcServer(port, ctx => events1.EventsGrpc.bindService(new EventServer.Events1Service(handler), ctx))
}
