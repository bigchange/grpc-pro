package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/15.
 * resume json util
 */
public class JsonUtil {

  private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

  public JsonUtil() {
  }

  public static Pattern pattern = Pattern.compile("[a-zA-Z]*");

  /**
   * 去掉所有的空格
   * 首先必须是中文
   * 中文四个字（复姓之内）的中间空格都需要去掉
   * 专业，学校，公司暂不做特殊处理
   */
  public static String whiteSpaceAbandon(String src) {

    // 中/英文 区分
    if (pattern.matcher(src).matches()) { // english
      return src;
    } else { // chinese
      String formatSrc = src.replaceAll("\\s*", "");
      if (formatSrc.length() > 5) { // 中文去掉空格可能存在异常，空格保留
        return src;
      } else if (formatSrc.length() >= 1) { // 去掉中间空格
        logger.info("whiteSpaceAbandon: src -> " + src + ", format src -> " + formatSrc);
        return formatSrc;
      } else {
        return "";
      }
    }
  }

  /**
   * isNull object
   */
  public static Boolean isNull(Object obj) {
    return obj == null;
  }

  /**
   * 检查json的key-value
   */
  public static void checkJsonKeyValue(JsonObject infoObject, List<String> keys) {

    for (String key : keys) {
      Object name = infoObject.getValue(key, null);
      if (isNull(name)) {
        logger.info("checkJsonValue key: " + key + ", is null, json key remove!!");
        infoObject.remove(key);
      }
    }
  }

  /**
   * sort jsonObject in json array
   * by sortKey
   */
  public static JsonArray jsonArraySortBySortKey(JsonArray jsonArray, String sortKey) {
    List<JsonObject> jsonValues = new ArrayList<JsonObject>();
    JsonArray sortedJsonArray = new JsonArray();
    for (int i = 0; i < jsonArray.size(); i++) {
      jsonValues.add(jsonArray.getJsonObject(i));
    }
    Collections.sort(jsonValues, new Comparator<JsonObject>() {
      private final String startedAt = sortKey;

      @Override
      public int compare(JsonObject a, JsonObject b) {
        String aStartedAt = a.getString(startedAt, "");
        if (aStartedAt == null) {
          aStartedAt = "";
        } else {
          aStartedAt = aStartedAt.trim();
        }
        String bStartedAt = b.getString(startedAt, "");
        if (bStartedAt == null) {
          bStartedAt = "";
        } else {
          bStartedAt = bStartedAt.trim();
        }
        int s = aStartedAt.compareTo(bStartedAt);
        return -s;
      }
    });
    for (int i = 0; i < jsonValues.size(); i++) {
      sortedJsonArray.add(jsonValues.get(i));
    }
    return sortedJsonArray;
  }


}
