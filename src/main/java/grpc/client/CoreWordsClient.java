package grpc.client;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;
import com.bgfurfeature.hello.rpc.GreeterGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/12.
 */
public class CoreWordsClient {

  private static Logger logger = LoggerFactory.getLogger(CoreWordsClient.class);
  private final ManagedChannel channel;
  private final CoreWordsGrpc.CoreWordsBlockingStub blockingStub;

  CoreWordsClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = CoreWordsGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public CoreWordsClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  private void doExtractor(List<Word> words) {
    WordsRequest request = WordsRequest.newBuilder().addAllWord(words).build();
    WordsReply reply = null;
    try {
      reply = blockingStub.extractorCoreWords(request);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    } finally {
    }

    for (Result result: reply.getResultList()) {
      String number = result.getNumber();
      logger.info("reply -> number:" + number);
      List<String> texts = new ArrayList<>();
      int counter = result.getTextCount();
      for (int i =0; i < counter; i++) {
        texts.add(result.getText(i));
      }
      logger.info("reply -> text:" + texts);
    }
  }

  public static void main(String[] args) throws Exception {
    CoreWordsClient coreWordsClient = new CoreWordsClient("localhost", 20299);
    List<Word> words = new ArrayList<>();
    words.add(Word.newBuilder().setNumber("1").setText("市场及销售部经理").build());
    coreWordsClient.doExtractor(words);
  }

}
