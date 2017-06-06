package grpc.client;

import com.inmind.idmg.text_sim.normalize.rpc.NormalizeServiceGrpc;
import com.inmind.idmg.text_sim.normalize.rpc.NormalizeServiceGrpc.NormalizeServiceBlockingStub;
import com.inmind.idmg.text_sim.normalize.rpc.NormalizeServiceOuterClass;
import com.inmind.idmg.text_sim.normalize.rpc.NormalizeServiceOuterClass.NormReply;
import com.inmind.idmg.text_sim.normalize.rpc.NormalizeServiceOuterClass.NormRequest;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by higgs on 2017/4/12.
 */
public class NormalizeClient {
  private static Logger logger = LoggerFactory.getLogger(NormalizeClient.class);
  private final ManagedChannel channel;
  private final NormalizeServiceBlockingStub blockingStub;

  public NormalizeClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  public NormalizeClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = NormalizeServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public NormReply normalize(NormRequest req) {
    NormReply rep = null;
    try {
      rep = blockingStub.normalize(req);
    } catch (StatusRuntimeException e) {
      logger.error("Normalize rpc failed: {0}", e.getStatus());
    }
    return rep;
  }

  public NormalizeServiceOuterClass.IdentifyReply identify(NormalizeServiceOuterClass
                                                               .IdentifyRequest req) {
    NormalizeServiceOuterClass.IdentifyReply rep = null;
    try {
      rep = blockingStub.identify(req);
    } catch (StatusRuntimeException e) {
      logger.error("identify rpc failed: {0}", e.getStatus());
      return null;
    }
    return rep;
  }
}
