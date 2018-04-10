package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.List;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/14.
 * 文件处理工具类
 */
public class FileContentUtil {
  /**
   * 工具类，不需要生成实例, 方法static修饰
   */
  public FileContentUtil() {

  }

  private static Logger logger = LoggerFactory.getLogger(FileContentUtil.class);


  public static void readLines(List<String> termList, String path) throws Exception {
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

  /**
   * 读取单个文件内容
   */
  public static void readContents(String filePath, List<String> listOrigin) {
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
        if (tokens.length >= 1) {
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
   * @param dir
   * @param list
   * @throws Exception
   */
  public static void readFiles(String dir, List<String> list) {

    Path path = Paths.get(dir);
    try {
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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 保存文件
   * @param fileSave
   * @param lines
   * @throws Exception
   */
  public static void saveFile(String fileSave, List<String> lines) {
    File file = new File(fileSave);
    if (file.exists()) {
      file.delete();
    }
    PrintWriter writer = null;
    try {
       writer = new PrintWriter(new FileOutputStream(file));
      for (String line : lines) {
        writer.println(line);
      }
      writer.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      writer.close();
    }
  }
}
