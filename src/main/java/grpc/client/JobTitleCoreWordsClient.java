package grpc.client;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
public class JobTitleCoreWordsClient {

  private static Logger logger = LoggerFactory.getLogger(JobTitleCoreWordsClient.class);
  private final ManagedChannel channel;
  private final CoreWordsGrpc.CoreWordsBlockingStub blockingStub;

  JobTitleCoreWordsClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = CoreWordsGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public JobTitleCoreWordsClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * grpc 请求
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

  /**
   * 遍历目录下的文件
   */
  public void readFiles(String dir, List<String> list) throws Exception {

    Path path = Paths.get(dir);
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        String filePath = file.toString();
        logger.info("visit file name is :" + fileName + ", path is:" + filePath);
        try {
          readContents(filePath, list);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("skipped:" + file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
        if (e != null) {
          System.err.println("found error after visit directory:" + e);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * 文件内容批量处理
   */
  public void run(String previous) throws Exception {
    String file = "/Users/devops/workspace/shell/jobtitle/JobTitle/" + previous;
    List<String> originWords = new ArrayList<>();
    String fileNormal = "/Users/devops/workspace/shell/jobtitle/normal_" + previous;
    readFiles(file, originWords);
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

  /**
   * 单元测试用例使用
   */
  public List<String> testUnit(List<String> originWords, List<String> result) {
    int i = 0;
    for (String word : originWords) {
      List<Word> words = new ArrayList<>();
      words.add(Word.newBuilder().setNumber(String.valueOf(i)).setText(word).build());
      // increase number
      i++;
      List extractors = doExtractor(words);
      result.addAll(extractors);
      logger.info("reply:" + extractors);
    }
    return result;

  }


  public static void main(String[] args) throws Exception {
    JobTitleCoreWordsClient coreWordsClient = new JobTitleCoreWordsClient("localhost", 20299);

    // coreWordsClient.run("src");

    /*coreWordsClient.testUnit(
        Arrays.asList("java/c++开发软件工程师",
            "校对/录入",
            "信息管理部主管",
            "技术总监/产品总监",
            "首席信息官",
            "商务经理商务专员",
            "行政人事财务经理",
            "高级经理",
            "销售高级经理",
            "中国区运营副总监",
            "教育研究院院长",
            "财经主持人",
            "公共关系高级经理",
            "销售部副主管",
            "人力资源经理"
        ), new ArrayList<String>());*/
    coreWordsClient.testUnit(Arrays.asList("商务总经理", "人力资源", "量化投资", "软件工程师"), new ArrayList<>());

  }

}
