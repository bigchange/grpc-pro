package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jerry on 2017/5/14.
 * 文本处理工具类
 */
public class TextUtil {

  public TextUtil() {

  }

  private static  void clearNoFormalText(List<String> list) {
    List<String> needRemove = new ArrayList<>();
    for (String item : list) {
      if (item.contains("，") || item.contains("、") || item.contains(";") || item.contains("&") ||
          item.contains("/") || item.contains(" ") || item.length() <= 1) {
        needRemove.add(item);
      }
    }
    list.removeAll(needRemove);
  }

  private static void splitText(String origin, List<String> list) {
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

}
