package grpc.serving;

import java.nio.file.Files;
import java.nio.file.Paths;

import grpc.impl.ResumeFingerPrintRpcServing;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/4/21.
 */
public class ResumeFingerPrintGrpcMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResumeFingerPrintGrpcMain.class);

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      System.out.println("Usage ResumeFingerPrintGrpcMain <conf>");
      return;
    }
    String configStr = new String(Files.readAllBytes(Paths.get(args[0])));
    JsonObject config = new JsonObject(configStr);

    String distintServer = config.getString("distinct.grpc.server", "hg005:20299");
    int port = config.getInteger("fingerprint.grpc.port", 17182);
    int level = config.getInteger("log.level", 0);
    ResumeFingerPrintRpcServing resumeFingerPrintRpcServing = new ResumeFingerPrintRpcServing(distintServer);
    LOGGER.info("listen port:" + port + " , log level:" + level + ", distintServer:" + distintServer);
    Server server = ServerBuilder.forPort(port)
        .addService(resumeFingerPrintRpcServing).build();
    server.start();
    server.awaitTermination();
  }

}
