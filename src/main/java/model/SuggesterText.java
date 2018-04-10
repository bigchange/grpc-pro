package model;

import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.suggest.scorer.BaseScorer;
import com.hankcs.hanlp.suggest.scorer.editdistance.EditDistanceScorer;
import com.hankcs.hanlp.suggest.scorer.lexeme.IdVectorScorer;
import com.hankcs.hanlp.suggest.scorer.pinyin.PinyinScorer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/6/2.
 * 文本推荐
 */
public class SuggesterText {

  private static Logger logger = LoggerFactory.getLogger(SuggesterText.class);

  private String filePath;

  private Suggester suggester;

  private List<String> termList = new ArrayList();

  private static  List<BaseScorer> scorerList = new ArrayList<>();

  static {
    scorerList.add(new IdVectorScorer().setBoost(50.0));
    scorerList.add(new EditDistanceScorer().setBoost(100.0));
    scorerList.add(new PinyinScorer().setBoost(1.0));
  }

  public SuggesterText(String filePath) throws Exception {
    this.filePath = filePath;
    suggester = new Suggester(scorerList);
  }

  public SuggesterText feedData() {
    for (String term: termList) {
      suggester.addSentence(term);
    }
    return this;
  }

  public List<String> similarity(String key) {
    return suggester.suggest(key, 10);
  }

  public void readLines(List<String> termList, String path) throws Exception {
    BufferedReader bf = null;
    try {
      bf = new BufferedReader(new FileReader(path));
      String line = bf.readLine();
      while (line != null) {
        String[] lineSp = line.split("\t");
        termList.add(lineSp[0]);
        line = bf.readLine();
      }
    } finally {
      bf.close();
    }
  }

  public SuggesterText readFileTree() throws Exception {
    Path path = Paths.get(filePath);
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      @SuppressWarnings({"IllegalCatch", "NestedTryDepth"})
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
          throws IOException {
        try {
          readLines(termList, file.toString());
        } catch (Exception e) {
          e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException e)
          throws IOException {
        if (e != null) {
          System.err.println("found error after visit directory:" + e);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.err.println("skipped:" + file);
        return FileVisitResult.CONTINUE;
      }
    });

    logger.info("term list size:" + termList.size());
    return  this;
  }

}
