package usercase;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/6/13.
 */
public class SchoolNormalize {

  private Logger logger = LoggerFactory.getLogger(SchoolNormalize.class);
  private String rootDir = "/Users/devops/workspace/shell/school";
  private String locationDict = rootDir + "/location_dict.txt";
  private String orgLevelDict = rootDir + "/school_org_level_dict.txt";
  private String normalSchoolDict = rootDir + "/normal_school_dict.txt";
  private String abbSchoolDict = rootDir + "/abbreviation_dict.txt";

  private HashMap<String, String> locationAbb = new HashMap<>();
  private HashMap<String, String> location = new HashMap<>();
  private HashMap<String, String> orgLevel = new HashMap<>();
  private HashMap<String, String> school = new HashMap<>();
  private HashMap<String, String> abbSchool = new HashMap<>();

  public SchoolNormalize() {
    // init dict
    init();
  }

  /**
   * read normal key value dict
   */
  private static void readMap(String file, HashMap<String, String> map) {

    BufferedReader bf = null;
    try {
      bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String lines = bf.readLine();
      while (lines != null) {
        String[] line = lines.split("\t");
        if (line.length == 2) {
          map.put(line[0], line[1]);
        }
        if (line.length == 3) {
          map.put(line[2], line[1]);
        }
        lines = bf.readLine();
      }

    } catch (Exception e) {

    } finally {
      try {
        if (bf != null) {
          bf.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * read location dict
   */
  private static void readOtherMap(String file, HashMap<String, String> map, HashMap<String,
      String> abbMap) {

    BufferedReader bf = null;

    try {
      bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      String lines = bf.readLine();
      while (lines != null) {
        String[] line = lines.split("\t");
        if (line.length == 3) {
          map.put(line[1], line[0]);
          abbMap.put(line[2], line[0]);
        }
        lines = bf.readLine();
      }

    } catch (Exception e) {

    } finally {
      try {
        bf.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * school name length sort
   * @param suggests
   */
  public void orderSuggest(List<String> suggests) {
    Collections.sort(suggests, Comparator.comparingInt(String::length));
  }

  public void matchSchool(List<String> locationSegs, List<String> result, List<String> suggests) {
    // 根据关键字过滤后过滤地区
    if (locationSegs.size() > 0) {
      // 地区优先级匹配
      int size = locationSegs.size();
      for (int i = size - 1; i >=0 ; i--) {
        if (suggests.size() > 0) {
          break;
        }
        String location = locationSegs.get(i);
        for (String suggest: result) {
          if (suggest.contains(location)) {
            suggests.add(suggest);
          }
        }
      }

    } else {
      suggests.addAll(result);
    }

    //  山东省鲁东大学，武汉交通大学（不存在该院校）
    if (suggests.size() == 0 && result.size() <= 2) {
      suggests.addAll(result);
    }
  }

  public void checkMapKeyContainItem(HashMap<String, String> map, String request, List<String>
      result) {
    // 学校名关键字
    Set<Map.Entry<String, String>> entrySet = map.entrySet();
    Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> mapItem = iterator.next();
      String key = mapItem.getKey();
      if (key.contains(request)) {
        result.add(key);
      }
    }
  }

  /**
   * 必须全部相等
   * @param map
   * @param request
   * @param result
   */
  public void checkMapKeyIsItem(HashMap<String, String> map, String request, List<String>
      result) {
    // 学校名关键字
    Set<Map.Entry<String, String>> entrySet = map.entrySet();
    Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> mapItem = iterator.next();
      String key = mapItem.getKey();
      if (key.equals(request)) {
        result.add(key);
      }
    }
  }

  /**
   * 学校简称处理
   * @param map
   * @param request
   * @param result
   */
  public void abbSchoolCheck(HashMap<String, String> map, String request, List<String>
      result) {
    Set<Map.Entry<String, String>> entrySet = map.entrySet();
    Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> mapItem = iterator.next();
      String key = mapItem.getKey();
      String value = mapItem.getValue();
      if (key.equals(request)) {
        result.add(value);
      }
    }
  }
  public void checkItemContainMapKey(HashMap<String, String> map, String request, List<String>
      result) {
    // 学校后缀词判断
    Set<Map.Entry<String, String>> entrySet = map.entrySet();
    Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> mapItem = iterator.next();
      String key = mapItem.getKey();
      if (request.contains(key)) {
        result.add(key);
      }
    }
  }

  public void init() {

    logger.info("init .....");
    readMap(orgLevelDict, orgLevel);
    readMap(normalSchoolDict, school);
    readMap(abbSchoolDict, abbSchool);
    // readOtherMap(locationDict, location, locationAbb);
    if (orgLevel.size() == 0 || school.size() == 0) {
      System.err.println("check init....");
      System.exit(-1);
    }
    logger.info("load global dict finished .....");
  }

  private String getLongestOrg(List<String> orgSegs) {
    String ret = "";
    for (String org: orgSegs) {
      if (org.length() > ret.length()) {
        ret = org;
      }
    }
    return ret;
  }

  /**
   * 请求分词操作
   */
  private void segment(String request, List<String> keyWordSegs, List<String> locationSegs,
                       List<String> orgSegs) {

    // 是否需要增加自定义的地区词典到分词系统中，因情况而定
    Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
    // 学校的后缀处理
    checkItemContainMapKey(orgLevel, request, orgSegs);
    String longestOrg = getLongestOrg(orgSegs);
    request = request.replace(longestOrg, "");
    List<Term> termList = segment.seg(request);
    for (Term item : termList) {
      String locationFlag = item.nature.name();
      if ("ns".equals(locationFlag) || "nsf".equals(locationFlag)) {
        locationSegs.add(item.word);
      } else {
        keyWordSegs.add(item.word);
      }
    }
  }

  public List<String> normalize(String requestMesage, Boolean enableLog) {
    List<String> keyWordSegs = new ArrayList<>();
    List<String> locationSegs = new ArrayList<>();
    List<String> orgSegs = new ArrayList<>();
    List<String> suggests = new ArrayList<>();

    String request = HanLP.convertToSimplifiedChinese(requestMesage);
    // 分词 通过核心词去匹配
    segment(request, keyWordSegs, locationSegs, orgSegs);
    if (enableLog) {
      System.out.println(locationSegs + " -> " + keyWordSegs + " -> " + orgSegs);
    }

    // 学校简称匹配 (简称需要将可能的简化版叫法都涵盖)
    abbSchoolCheck(abbSchool, request, suggests);


    if (suggests.size() > 0) {
      orderSuggest(suggests);
      // 简称匹配到
      if (enableLog) {
        logger.info("学校简称匹配到....");
        logger.info("suggests:" + suggests);
      }
      return suggests;
    }

    // 直接全名在字典里匹配（查到直接返回）
    checkMapKeyContainItem(school, request, suggests);

    String org = getLongestOrg(orgSegs);

    if (suggests.size() > 0) {
      orderSuggest(suggests);
      // 全名匹配到
      if (enableLog) {
        logger.info("学校全名匹配到....");
        logger.info("suggests:" + suggests);
      }
      return suggests;
    } else {
      // 分词 通过核心词去匹配
      // segment(request, keyWordSegs, locationSegs, orgSegs);
      // 通过keyWord 匹配标准学校
      List<String> tempSuggest = new ArrayList<>();
      if (keyWordSegs.size() > 0) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String keyWord : keyWordSegs) {
          stringBuilder.append(keyWord);
        }
        if ("大学".equals(org)) {
          // 多个关键字需要连接在一起匹配
          checkMapKeyContainItem(school, stringBuilder.toString() + org, tempSuggest);
        } else {
          // 多个关键字需要连接在一起匹配
          checkMapKeyContainItem(school, stringBuilder.toString(), tempSuggest);
        }

      } else if (locationSegs.size() > 1) { // 江西南昌大学
        String schoolPos = locationSegs.get(1) + org;
        if (!"".equals(school.getOrDefault(schoolPos , ""))) {
          suggests.add(schoolPos);
        }
      } else  {
        if (!"".equals(school.getOrDefault(request, ""))) {
          suggests.add(request);
        }
      }


      matchSchool(locationSegs, tempSuggest, suggests);
      orderSuggest(suggests);
      if (enableLog) {
        logger.info("分词通过核心词去匹配....");
        logger.info("tempSuggests:" + tempSuggest);
        logger.info("suggests:" + suggests);
      }
      return suggests;
    }
  }

  public static void main(String[] args) {

    SchoolNormalize schoolNormalize  = new SchoolNormalize();
    schoolNormalize.normalize("地大", true);

  }

}
