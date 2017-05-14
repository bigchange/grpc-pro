package grpc.client;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

  private List<String> doExtractor(List<Word> words) {
    WordsRequest request = WordsRequest.newBuilder().addAllWord(words).build();
    WordsReply reply = null;
    try {
      reply = blockingStub.extractorCoreWords(request);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
    }

    for (Result result : reply.getResultList()) {
      String number = result.getNumber();
      List<String> texts = new ArrayList<>();
      int counter = result.getTextCount();
      for (int i = 0; i < counter; i++) {
        texts.add(result.getText(i));
      }
      logger.info("reply: number -> " + number + ", text -> " + texts);
      return texts;
    }
    return null;
  }

  /**
   * 读取单个文件内容
   */
  public static void readContents(String filePath, List<String> listOrigin)
      throws Exception {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(filePath));
      String line;
      line = reader.readLine();
      while (line != null) {
        if (line == null) {
          return;
        }
        logger.info("read line is :" + line);
        String[] tokens = line.split("\t");
        if (tokens.length == 2) {
          String origin = tokens[0];
          listOrigin.add(origin);
        } else {
          logger.info("length error:");
        }
        line = reader.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    CoreWordsClient coreWordsClient = new CoreWordsClient("localhost", 20299);
    String file = "/Users/devops/workspace/shell/jobtitle/JobTitle/position_dict.txt";
    List<String> originWords = new ArrayList<>();
    String one = "position_dict.txt";
    String fileSave = "/Users/devops/workspace/shell/jobtitle/normal_" + one;
    readContents(file, originWords);
    List<String> result = new ArrayList<>();
    int i = 0;
    for (String word : originWords) {
      List<Word> words = new ArrayList<>();
      words.add(Word.newBuilder().setNumber(String.valueOf(i)).setText(word).build());
      // increase number
      i++;
      List extractors = coreWordsClient.doExtractor(words);
      result.add(word + "\t" + extractors.toString());
    }
    // FileContentUtil.saveFile(fileSave, result);
  }

}
