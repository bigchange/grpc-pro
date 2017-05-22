package grpc.client;

import com.bgfurfeature.hello.rpc.GreeterGrpc;
import com.bgfurfeature.hello.rpc.HelloReply;
import com.bgfurfeature.hello.rpc.HelloRequest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/10.
 * grpc client
 */
public class GreeterClient {

  private static Logger logger = LoggerFactory.getLogger(GreeterClient.class);
  private final ManagedChannel channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  GreeterClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public GreeterClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * sayHello
   */
  public HelloReply greeter(String message) {
    HelloRequest request = HelloRequest.newBuilder().setName(message).build();
    HelloReply reply;
    try {
      reply = blockingStub.sayHello(request);
    } catch (Exception e) {
      logger.info(Level.WARNING, "RPC failed: {0}", e.getMessage());
      return null;
    }
    logger.info("Greeting: " + reply.getMessage());
    return reply;
  }

  /**
   * sayHelloAgain
   */
  public HelloReply greeterAgain(String message) {
    HelloRequest request = HelloRequest.newBuilder().setName(message).build();
    HelloReply reply;
    try {
      reply = blockingStub.sayHelloAgain(request);
    } catch (Exception e) {
      logger.info(Level.WARNING, "RPC failed: {0}", e.getMessage());
      return null;
    }
    logger.info("Greeting: " + reply.getMessage());
    return reply;
  }

  public static void main(String[] args) throws Exception {

    GreeterClient client = new GreeterClient("localhost", 30299);
    try {
      client.greeter("asp.net高级开发工程师");
      // client.greeterAgain("i am grpc, thanks god it works!!");
    } finally {
      client.shutdown();
    }
  }
}
