package alerts.rpc

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder}
import io.grpc.stub.StreamObserver
import _root_.alerts._
import _root_.alerts.rpc.{alerts => msg}

object AlertServer {
  private def toAlertMessage(a: IdentifiedAlert): msg.Alert =
    a.alert match {
      case AbnormalHeartRateAlert(user, hr) => 
        msg.Alert(
          a.id.value,
          user.value, 
          msg.Alert.Alert.AbnormalHeartRate(msg.AbnormalHeartRateAlert(
            hr.value)))
        case LowBloodPressureAlert(user, bp, hr) => 
          msg.Alert(
            a.id.value,
            user.value, 
            msg.Alert.Alert.LowBloodPressure(msg.LowBloodPressureAlert(
              hr.value,
              bp.systolic,
              bp.diastolic)))
    }
  
  private class AlertsImpl(handler: AlertService)(implicit ctx: ExecutionContext) extends msg.AlertsGrpc.Alerts {
    def getActiveAlerts(req: msg.AEmpty): Future[msg.ManyAlerts] =
      handler.activeAlerts
        .map(as => msg.ManyAlerts(as.map(toAlertMessage)))
    
    def getActiveAlertsByUser(req: msg.UserId): Future[msg.ManyAlerts] =
      handler.activeAlertsByUser(UserId(req.userId))
        .map(as => msg.ManyAlerts(as.map(toAlertMessage)))
    
    def acknowledgeAlert(req: msg.AlertId): Future[msg.AEmpty] =
      handler.acknowledgeAlert(UserId(req.userId), AlertId(req.alertId)).map(_ => msg.AEmpty())
  }
  
  def apply(port: Int, handler: AlertService): GrpcServer = 
    new GrpcServer(port, ctx => msg.AlertsGrpc.bindService(new AlertServer.AlertsImpl(handler)(ctx), ctx))
}
