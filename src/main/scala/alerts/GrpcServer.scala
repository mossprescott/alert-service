package alerts

import scala.concurrent.{ExecutionContext, Future}
import io.grpc.{Server, ServerBuilder, ServerServiceDefinition}
import io.grpc.stub.StreamObserver

import io.grpc.netty.NettyServerBuilder
import java.net.InetSocketAddress

// Note: this is largely cribbed from the example at 
// https://github.com/xuwei-k/grpc-scala-sample/blob/master/grpc-scala/src/main/scala/io/grpc/examples/routeguide/RouteGuideServer.scala
// There 
class GrpcServer(port: Int, service: ExecutionContext => ServerServiceDefinition) { self =>
  private[this] var server: Option[Server] = None
  
  def start(executionContext: ExecutionContext): Unit = {
    // val builder = ServerBuilder.forPort(port)
    val builder = NettyServerBuilder.forAddress(new InetSocketAddress("localhost", port))
    val server0 = builder.addService(service(executionContext)).build
    server0.start()
    server = Some(server0)
    println(s"server started on port $port")
    Runtime.getRuntime.addShutdownHook(new Thread() {
      self.stop()
    })
  }
  
  def stop(): Unit = {
    server.map(_.shutdown())
    server = None
  }

  def blockUntilShutdown(): Unit = {
    server.map(_.awaitTermination())
    server = None
  }
}