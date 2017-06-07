package grpc.serving;

import java.io.IOException;

import grpc.impl.GreeterServerImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;

/**
 * Created by Jerry on 2017/5/10.
 * start a specific grpc service
 */
public class GreeterGrpcMain {

  private static Logger logger = LoggerFactory.getLogger(GreeterGrpcMain.class);

  private Server server;

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  private void start(String file) throws IOException {

    /* The port on which the server should run */

    int port = 30299;

    // add grpc monitor - not work  (io.netty.handler.codec.http2.Http2Exception:
    // HTTP/2 client preface string missing or corrupt.
    // Hex dump for received bytes: 1603010085010000810303871fcf0abeefc81a72f2ee73c6)

    /*MonitoringServerInterceptor monitoringInterceptor =
        MonitoringServerInterceptor.create(Configuration.cheapMetricsOnly().withCollectorRegistry
        (new CollectorRegistry()));
    server = ServerBuilder.forPort(port)
        .addService(ServerInterceptors.intercept((new GreeterServerImpl(file)), monitoringInterceptor))
        .build()
        .start();*/

   server = ServerBuilder.forPort(port)
        .addService(new GreeterServerImpl(file))
        .build()
        .start();

    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  public static void main(String[] args) throws Exception {

    String formatPath = "/Users/devops/workspace/shell/jd/formatResult/part-00000";
    String filePath = "/Users/devops/workspace/shell/jd/result-map/part-00000";
    GreeterGrpcMain server = new GreeterGrpcMain();
    server.start(formatPath);
    server.blockUntilShutdown();
  }
}
