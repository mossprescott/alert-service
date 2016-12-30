package alerts.rpc

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import _root_.alerts._
import _root_.alerts.rpc.events1._

object EventServer {
  private class Events1Impl(handler: EventService) extends EventsGrpc.Events {
    private def receive(evt: Event): Unit =
      handler.receive(UserId(evt.userId), HeartRate(evt.heartRate), BloodPressure(evt.systolic, evt.diastolic))
    
    
    def receiveOne(evt: Event): Future[EEmpty] = {
      receive(evt)
      Future.successful(EEmpty())
    }
      
    def receiveStream(responseObserver: StreamObserver[EEmpty]): StreamObserver[Event] =
      new StreamObserver[Event] {
        def onNext(evt: Event): Unit = {
          receive(evt)
          // responseObserver.onNext(EEmpty())
        }
        def onCompleted(): Unit = responseObserver.onCompleted()
        def onError(t: Throwable): Unit = ()
      }
  }
  
  def apply(port: Int, handler: EventService): GrpcServer = 
    new GrpcServer(port, ctx => EventsGrpc.bindService(new EventServer.Events1Impl(handler), ctx))
}
