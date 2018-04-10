package model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.FileContentUtil;

/**
 * Created by Jerry on 2017/6/2.
 */
public class Simhash {

  private JiebaSegmenter segmenter = new JiebaSegmenter(); // 分词
  private List<Map<String, Map<Long, String>>> storage = new ArrayList<>();
  // 按照分段存储simhash，查找更快速
  private int fracCount = 4; // 默认按照4段进行simhash存储
  private int hammingThresh = 3;// 汉明距离的衡量标准

  List<String> ls = new ArrayList<>(); // content list
  String filePath = "/Users/devops/workspace/shell/jd/result-map/part-00000";

  public Simhash() {
    for (int i = 0; i < fracCount; i++)
      storage.add(new HashMap<>());
  }

  public Simhash(int fracCount, int hammingThresh) {
    this.fracCount = fracCount;
    this.hammingThresh = hammingThresh;
    for (int i = 0; i < fracCount; i++)
      storage.add(new HashMap<>());

    // init train
    System.out.println("content init train ");

    try {
      FileContentUtil.readLines(ls, filePath);
    } catch (Exception e) {
      e.printStackTrace();
    }
    for (String content : ls) {
      Long simhashVal = calSimhash(content);
      store(simhashVal, content);
    }

    System.out.println("storage list size:" + storage.size());
  }

  /**
   * 指定文本计算simhash值
   *
   * @param content
   * @return Long
   */
  public Long calSimhash(String content) {
    /*String filterContent = content.trim().replaceAll("\\p{Punct}|\\p{Space}", "");
    // 切词
    List<SegToken> lsegStr = segmenter.process(filterContent, JiebaSegmenter.SegMode.SEARCH);

    // 按照词语的hash值，计算simHashWeight(低位对齐)
    Integer[] weight = new Integer[64];
    Arrays.fill(weight, 0);
    for (SegToken st : lsegStr) {
      long wordHash = Murmur3.hash64(st.word.getBytes());
      for (int i = 0; i < 64; i++) {
        if (((wordHash >> i) & 1) == 1) weight[i] += 1;
        else weight[i] -= 1;
      }
    }*/

    List<Term> segments = HanLP.segment(content);
    Integer[] weight = new Integer[64];
    Arrays.fill(weight, 0);
    for (Term st : segments) {
      long wordHash = Murmur3.hash64(st.word.getBytes());
      for (int i = 0; i < 64; i++) {
        if (((wordHash >> i) & 1) == 1) weight[i] += 1;
        else weight[i] -= 1;
      }
    }

    // 计算得到Simhash值
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      if (weight[i] > 0) sb.append(1);
      else sb.append(0);
    }

    return new BigInteger(sb.toString(), 2).longValue();
  }

  /**
   * 判断文本是否重复
   *
   * @param content
   * @return
   */
  public boolean isDuplicate(String content) {
    Map<Integer, String> distMap = new HashMap<>();
    Long simhash = calSimhash(content);
    System.out.println("isDuplicate content:" + content);
    List<String> lFrac = splitSimhash(simhash, fracCount);
    for (int i = 0; i < fracCount; i++) {
      String frac = lFrac.get(i);
      Map<String, Map<Long, String>> fracMap = storage.get(i);
      if (fracMap.containsKey(frac)) {
        System.out.println("i -> " + i +", frac -> " + frac);
        return foreachMap(simhash, fracMap.get(frac), distMap);
      }
    }
    return false;
  }

  /**
   *
   * @param simhash
   * @param fracMapSubMap
   * @param distMap
   */
  public Boolean foreachMap (Long simhash, Map<Long, String> fracMapSubMap, Map<Integer, String>
  distMap) {
    Set<Map.Entry<Long, String>> entrySet = fracMapSubMap.entrySet();
    Iterator<Map.Entry<Long, String>> iterator = entrySet.iterator();
    while(iterator.hasNext()) {
      Map.Entry entry = iterator.next();
      long simhash2 = (long)entry.getKey();
      String value = entry.getValue().toString();
      int dist = hamming(simhash, simhash2);
      distMap.put(dist, value);
      if (dist < hammingThresh) {
        return true;
      }
    }
    return false;
  }

  public Map<Integer, String> hammingDistRank(String content, int topSize) {
    Map<Integer, String> distMap = new HashMap<>();
    Long simhash = calSimhash(content);
    List<String> lFrac = splitSimhash(simhash, fracCount);
    for (int i = 0; i < fracCount; i++) {
      String frac = lFrac.get(i);
      Map<String, Map<Long, String>> fracMap = storage.get(i);
      if (fracMap.containsKey(frac)) {
        foreachMap(simhash, fracMap.get(frac), distMap);
      }
    }

    Set<Map.Entry<Integer, String>> entrySet = distMap.entrySet();
    ArrayList<Map.Entry<Integer, String>> list = new ArrayList(entrySet);
    Collections.sort(list, (o1, o2) -> (o1.getKey() - o2.getKey()));

    if (distMap.size() < topSize) {
      topSize = distMap.size();
    }
    for (int i = 0; i < topSize; i++) {
      Map.Entry entry = list.get(i);
      String key = entry.getKey().toString();
      String value = entry.getValue().toString();
      System.out.println("dist -> " + key + ", content -> " + value);
    }
    return  distMap;
  }

  /**
   * 按照(frac, <simhash, content>)索引进行存储
   * @param simhash
   * @param content
   */
  public void store(Long simhash, String content) {
    List<String> lFrac = splitSimhash(simhash, fracCount);
    for (int i = 0; i < fracCount; i++) {
      String frac = lFrac.get(i);
      Map<String, Map<Long, String>> fracMap = storage.get(i);
      if (fracMap.containsKey(frac)) fracMap.get(frac).put(simhash, content);
      else {
        Map<Long, String> ls = new HashMap<>();
        ls.put(simhash, content);
        fracMap.put(frac, ls);
      }
    }

  }

  // 计算汉明距离
  private int hamming(Long s1, Long s2) {
    int dis = 0;
    for (int i = 0; i < 64; i++) {
      if ((s1 >> i & 1) != (s2 >> i & 1)) dis++;
    }
    return dis;
  }

  private int hamming(String s1, String s2) {
    if (s1.length() != s2.length()) return 0;
    int dis = 0;
    for (int i = 0; i < s1.length(); i++) {
      if (s1.charAt(i) != s2.charAt(i)) dis++;
    }
    return dis;
  }

  // 将simhash分成n段
  private List<String> splitSimhash(Long simhash, int n) {
    List<String> ls = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 64; i++) {
      sb.append(simhash >> i & 1);
      if ((i + 1) % n == 0) {
        ls.add(sb.toString());
        sb.setLength(0);
      }
    }
    return ls;
  }

  public void calSimilarity(String content) {
    Long simhashVal = calSimhash(content);
    System.out.println(simhashVal);
    System.out.println(Long.toBinaryString(simhashVal));
    System.out.println(hammingDistRank(content, 10));
  }


  public static void main(String[] args) throws Exception {
    Simhash simhash = new Simhash(4, 20);

  }

}
