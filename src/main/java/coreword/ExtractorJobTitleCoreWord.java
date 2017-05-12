package coreword;

import com.hankcs.hanlp.HanLP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/12.
 * 职位核心词的抽取
 */
public class ExtractorJobTitleCoreWord {

  private static Logger logger = LoggerFactory.getLogger(ExtractorJobTitleCoreWord.class);
  private List<String> jobTitleList = new ArrayList<>();
  private List<String> jobTitleOriginList = new ArrayList<>();
  private Map<String, Integer> jobTitleMap = new HashMap<>();

  /**
   * 去除非标准text
   */
  private void clearNoFormalText(List<String> list) {
    List<String> needRemove = new ArrayList<>();
    for (String item : list) {
      if (item.contains("，") || item.contains("、") || item.contains(";") || item.contains("&") ||
          item.contains("/") || item.contains(" ") || item.length() <= 1) {
        needRemove.add(item);
      }
    }
    list.removeAll(needRemove);
  }

  private void splitText(String origin, List<String> list) {
    if (origin.contains("/")) {
      for (String item : origin.split("/")) {
        list.add(item);
      }
    }
    if (origin.contains(" ")) {
      for (String item : origin.split(" ")) {
        list.add(item);
      }
    }
    if (origin.contains("&")) {
      for (String item : origin.split("&")) {
        list.add(item);
      }
    }
    if (origin.contains(";")) {
      for (String item : origin.split(";")) {
        list.add(item);
      }
    }
    if (origin.contains("、")) {
      for (String item : origin.split("、")) {
        list.add(item);
      }
    }
    if (origin.contains("，")) {
      for (String item : origin.split("，")) {
        list.add(item);
      }
    }
  }

  /**
   * 读取单个文件内容
   */
  private void readContents(String filePath, List<String> list, List<String> listOrigin) throws
      Exception {
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
          jobTitleMap.put(origin, jobTitleMap.getOrDefault(origin, 0) + Integer.valueOf
              (tokens[1]) - 1);
          listOrigin.add(origin);
          splitText(origin, list);
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
  public void readFiles(String dir, List<String> list, List<String> listOrigin) throws Exception {

    Path path = Paths.get(dir);
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        String filePath = file.toString();
        logger.info("visit file name is :" + fileName + ", path is:" + filePath);
        try {
          readContents(filePath, list, listOrigin);
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

  public List<String> getKeyPhrase(String text, int size) {
    List<String> keyWords = HanLP.extractPhrase(text, size);
    clearNoFormalText(keyWords);
    return keyWords;

  }

  public List<String> getKeyWords(String text, int size) {
    List<String> keyWords = HanLP.extractKeyword(text, size);
    clearNoFormalText(keyWords);
    return keyWords;
  }

  public List<String> convertMapToList(Map<String, Integer> map) {
    List<String> list = new ArrayList<>();
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    Iterator iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
      String ret = entry.getKey() + "\t" + entry.getValue();
      list.add(ret);
    }
    return list;
  }

  public void saveFile(String fileSave, List<String> titles) throws Exception {
    File file = new File(fileSave);
    if (file.exists()) {
      file.delete();
    }
    PrintWriter writer = new PrintWriter(new FileOutputStream(file));
    for (String line : titles) {
      writer.println(line);
    }
    writer.flush();
    writer.close();
  }

  public List<String> sortByValue(List<String> set) {
    final Comparator CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);
    Collections.sort(set, CHINA_COMPARE);
    return set;
  }

  private void extractorCoreWords(List<String> list, Map<String, Integer> map) {
    List<String> doc = sortByValue(list);
    for (String item : doc) {
      map.put(item, map.getOrDefault(item, 0) + 1);
      List<String> keyPhrases = getKeyPhrase(item, 2);
      List<String> keyWords = getKeyWords(item, 2);
      if (keyPhrases.size() > 0) {
        for (String keyItem : keyPhrases) {
          map.put(keyItem, map.getOrDefault(keyItem, 0) + 1);
        }
      }
      if (keyWords.size() > 0) {
        for (String wordItem : keyWords) {
          map.put(wordItem, map.getOrDefault(wordItem, 0) + 1);
        }
      }
    }
  }

  public void run(String fileSave, String fileCmp) throws Exception {
    logger.info("into run....");
    Set<String> titles = new HashSet<>();
    // 去除分隔后列表中的一些杂词
    clearNoFormalText(jobTitleList);
    logger.info("clearNoFormalText over!! ");
    logger.info("clear jobTitleList size:" + jobTitleList.size());
    jobTitleList.addAll(jobTitleOriginList);
    logger.info("jobTitleOriginList size:" + jobTitleOriginList.size());
    extractorCoreWords(jobTitleList, jobTitleMap);
    List<String> maptoList = convertMapToList(jobTitleMap);
    logger.info("total title size:" + titles.size());
    logger.info("maptoList size : " + maptoList.size());
    saveFile(fileSave, sortByValue(maptoList));
  }

  public static void main(String[] args) throws Exception {
    ExtractorJobTitleCoreWord coreWord = new ExtractorJobTitleCoreWord();
    String file = "/Users/devops/workspace/shell/jobtitle/JobTitle/position_dict.txt";
    coreWord.readFiles(file, coreWord.jobTitleList, coreWord.jobTitleOriginList);
    String one = "position_dict.txt";
    String fileSave = "/Users/devops/workspace/shell/jobtitle/normal_" + one;
    String fileCmp = "/Users/devops/workspace/shell/jobtitle/cmp_" + one;
    coreWord.run(fileSave, fileCmp);
  }
}
