package grpc.client;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import utils.FileContentUtil;

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

  /**
   * grpc 请求
   * @param words
   * @return
   */
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
        // logger.info("read line is :" + line);
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

  // read file content
  public  void run() throws  Exception {
    String file = "/Users/devops/workspace/shell/jobtitle/JobTitle/position_dict.txt";
    List<String> originWords = new ArrayList<>();
    String one = "position_dict.txt";
    String fileNormal = "/Users/devops/workspace/shell/jobtitle/normal_" + one;
    readContents(file, originWords);
    Set<String> normalResult = new HashSet<>();
    int i = 0;
    for (String word : originWords) {
      List<Word> words = new ArrayList<>();
      words.add(Word.newBuilder().setNumber(String.valueOf(i)).setText(word).build());
      // increase number
      i++;
      List extractors = doExtractor(words);
      normalResult.addAll(extractors);
    }
    FileContentUtil.saveFile(fileNormal, new ArrayList<>(normalResult));
  }

  public List<String> sortByValue(List<String> set) {
    final Comparator CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);
    Collections.sort(set, CHINA_COMPARE);
    return set;
  }
  private List<String> extractorCoreWords(List<String> doc) {
    Set<String> titles = new HashSet<>();
    for (String item : doc) {
      logger.info("item -> " + item);
      titles.add(item);
    }
    return new ArrayList<>(titles);
  }

  public void testUnit(List<String> originWords) {
    int i = 0;
    for (String word : originWords) {
      List<Word> words = new ArrayList<>();
      words.add(Word.newBuilder().setNumber(String.valueOf(i)).setText(word).build());
      // increase number
      i++;
      List extractors = doExtractor(words);
      logger.info("reply:" + extractors);
    }

  }


  public static void main(String[] args) throws Exception {
    CoreWordsClient coreWordsClient = new CoreWordsClient("localhost", 20299);
    coreWordsClient.run();
    // coreWordsClient.testUnit(Arrays.asList("java高级工程师"));
    // coreWordsClient.testUnit(Arrays.asList("商务总经理", "人力资源", "量化投资", "软件工程师"));

  }

}
