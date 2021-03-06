package grpc.serving;

import java.io.IOException;

import grpc.impl.JobTitleCoreWordsImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/12.
 */
public class JobTitleCoreWordsGrpcMain {

  private static Logger logger = LoggerFactory.getLogger(JobTitleCoreWordsGrpcMain.class);

  private Server server;
  private int port = 20299;

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public JobTitleCoreWordsGrpcMain(int port) {
    this.port = port;
  }

  private void start() throws IOException {
    /* The port on which the server should run */
    server = ServerBuilder.forPort(port)
        .addService(new JobTitleCoreWordsImpl()) // diff service pls new another service
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
    JobTitleCoreWordsGrpcMain server = new JobTitleCoreWordsGrpcMain(20299);
    server.start();
    server.blockUntilShutdown();
  }
}
