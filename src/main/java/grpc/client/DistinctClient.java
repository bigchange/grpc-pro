package grpc.client;

import com.inmind.idmg.dedup.rpc.DedupReply;
import com.inmind.idmg.dedup.rpc.DedupRequest;
import com.inmind.idmg.dedup.rpc.Feature;
import com.inmind.idmg.dedup.rpc.ResumeDedupServiceGrpc;
import com.sangupta.murmur.Murmur2;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by devops on 2017/4/10.
 * 简历去重的grpc客户端
 */
public class DistinctClient {

  private final ManagedChannel channel;
  private final ResumeDedupServiceGrpc.ResumeDedupServiceBlockingStub blockingStub;
  private static Logger logger = LoggerFactory.getLogger(DistinctClient.class);

  public static Feature generateFeature(String one, String two, Feature.Type type) {
    // generate hash id
    String hashStringKey = convertString(one.trim(), two.trim(), "$$");
    long hashId = generateMurMurHashId(hashStringKey);
    return Feature.newBuilder().setType(type)
        .setValue(hashId).build();
  }
  private static String convertString(String one, String two, String separate) {
    return one + separate + two;
  }

  private static final long MURMUR_SEED = 0x7f3a21eaL;

  private static long generateMurMurHashId(String src) {

    byte[] bytes = src.getBytes();
    long murmurId = Murmur2.hash64(bytes, bytes.length, MURMUR_SEED);
    return murmurId;
  }

  /**
   * Construct client connecting to HelloWorld server at {@code host:port}.
   */
  public DistinctClient(String host, int port) {
    this(OkHttpChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * Construct client for accessing RouteGuide server using the existing channel.
   */
  DistinctClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = ResumeDedupServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public DedupReply doDistinct(ArrayList<Feature> features, String docId) {
    DedupRequest dedupRequest = DedupRequest.newBuilder().setDocid(docId).addAllFeatures(features).build();
    // addFeatures(new Feature.Builder()
    // .setValue(value).setTypeValue(type)).build();
    DedupReply reply = null;
    try {
      reply = blockingStub.doDedup(dedupRequest);
    } catch (Exception e) {
      logger.info(e);
      logger.error("DedupRequest is error ！" + e.getMessage());
    }
    return reply;
  }

  public static void main(String[] args) throws Exception {
    DistinctClient client = new DistinctClient("hg008", 20899);
    ArrayList<Feature> list = new ArrayList<>();
    String docId = "e1616e7fb042e81d087c2376135692d2";
    String name = "李文月";
    String mobile = "13917061939";
    list.add(generateFeature(mobile, name, Feature.Type.PHONE_AND_NAME));
    DedupReply reply = client.doDistinct(list, docId);
    logger.info("is dedup:" + reply.getIsDup() + ", doc id is :" + reply.getDupDocid());
    JsonObject json = new JsonObject();
    json.put("source", "ifc");
    Object value = json.getValue("source");
    logger.info("value is :" + value);
  }

}
