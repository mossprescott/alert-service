package alerts.rpc

import io.grpc.{Channel, ManagedChannelBuilder}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/** Utilities for REPL testing. See the README. */
object clients {  
  def makeChannel(host: String, port: Int): Channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .build
    
  def events1(channel: Channel = makeChannel("localhost", 9000)) = _root_.alerts.rpc.events1.EventsGrpc.blockingStub(channel)
  def alerts(channel: Channel = makeChannel("localhost", 9001)) = _root_.alerts.rpc.alerts.AlertsGrpc.blockingStub(channel)
}

