package grpc.impl;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.grpc.stub.StreamObserver;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import utils.FileContentUtil;

/**
 * Created by Jerry on 2017/5/12.
 */
public class JobTitleCoreWordsImpl extends CoreWordsGrpc.CoreWordsImplBase {

  private static Logger logger = LoggerFactory.getLogger(JobTitleCoreWordsImpl.class);

  private static List<String> splitList = new ArrayList<>();

  static {
    splitList.add("/");
    splitList.add("兼");
    splitList.add("和");
    splitList.add("及");
    splitList.add("且");
    splitList.add("&");
    splitList.add(" ");
  }

  private List<String> classDict = new ArrayList<>();

  private List<String> posDict = new ArrayList<>();

  public JobTitleCoreWordsImpl() {
    FileContentUtil.readContents("/Users/devops/workspace/gitlab/idmg/resume_extractor/resources" +
        "/job_mid_dic.txt", classDict);
    FileContentUtil.readContents("/Users/devops/workspace/gitlab/idmg/resume_extractor/resources" +
        "/job_last_dic.txt", posDict);
    logger.info("class level size -> " + classDict.size() + ", pos dict size :" + posDict.size());
  }

  // 通过词表去除一些行业性质词： 最大匹配
  private String findJobByDict(List<String> dict, String text) {
    String maxMatch = "";
    String minMath = "0xfffffffffffffffffffffff"; // 25 length
    for (String dic : dict) {
      if (text.contains(dic)) {
        if (dic.length() > maxMatch.length()) { // 最大字串匹配
          maxMatch = dic;
        } else if (dic.length() == maxMatch.length()) {
          if (text.indexOf(dic) > text.indexOf(maxMatch)) { // 靠后位置的有限
            maxMatch = dic;
          }
        }
        if (dic.length() <= minMath.length()) {
          minMath = dic;
        } else if (text.indexOf(dic) > text.indexOf(minMath)) {
          minMath = dic;
        }
      }
    }
    logger.info("findJobByDict -> " + text + ", min match -> " + minMath + ", match max -> " +
        maxMatch);
    if (maxMatch.isEmpty()) {
      return "";
    } else if (!maxMatch.isEmpty() && text.length() - maxMatch.length() >= 2) {
      return maxMatch;
    } else if (!minMath.isEmpty()) {
      return minMath;
    }
    return "";
  }

  /**
   * 将请求的text文本，按一定的格式处理一下（'/'，'兼'，'及'，'和'等转换为'、'）
   */
  public String formatTxt(String text) {
    String formatTxt = text;
    for (String sp : splitList) {
      if (formatTxt.contains(sp)) {
        formatTxt = formatTxt.replace(sp, "、");
      }
    }
    return formatTxt;
  }

  /**
   * Each key value is JsonArray
   * 处理得到的核心词中存在 特殊分隔符的情况，需拆开
   */
  public void formatCoreWord(String coreWord, JsonArray coreArray) {

    String[] formatTxt = coreWord.trim().split("、");
    for (String txt : formatTxt) {
      if (!"".equals(txt)) {
        coreArray.add(txt);
      }
    }
  }

  /**
   * 先处理核心词，存在分隔符 '、' 的核心词拆开
   */
  public JsonObject singleTest(String text) {
    JsonObject jsonObject = new JsonObject();
    JsonArray coreArray = new JsonArray();
    String coreWord = "";
    String jobClass = findJobByDict(classDict, text);
    String jobPos = findJobByDict(posDict, text);
    // 特殊情况的core 提取
    if (!"".equals(jobPos) && jobPos.contains(jobClass)) {
      String tempTxt = text.replace(jobPos, "").replace(jobClass, "");
      if (tempTxt.length() >= 4) {
        coreWord = text.replace(jobPos, "").replace(jobClass, "");
      } else {
        coreWord = text.replace(jobClass, "");
      }
    } else if (!"".equals(jobClass) && jobClass.length() > jobPos.length()) {
      coreWord = text.replace(jobClass, "").replace(jobPos, "");
    } else { // 默认都将职级，pos替换掉
      coreWord = text.replace(jobPos, "").replace(jobClass, "");
    }
    // 核心词格式处理
    formatCoreWord(coreWord, coreArray);

    logger.info("joblevel is:" + jobClass + ", job pos is :" + jobPos + ", core word :" +
        coreArray);
    jsonObject.put("origin", text).put("core", coreArray).put("level", jobClass).put("pos", jobPos);
    return jsonObject;
  }

  public List<String> sortByValue(List<String> set) {
    final Comparator CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);
    Collections.sort(set, CHINA_COMPARE);
    return set;
  }

  /**
   * 显示日志
   */
  public void showLog(WordsRequest request, WordsReply reply) {
    if (request == null) {
      return;
    }
    for (Word word : request.getWordList()) {
      // logger.info("request : number -> " + word.getNumber() + ", text -> " + word.getText());
    }
    if (reply == null) {
      return;
    }
    for (Result result : reply.getResultList()) {
      String number = result.getNumber();
      List<String> texts = new ArrayList<>();
      int counter = result.getTextCount();
      for (int i = 0; i < counter; i++) {
        texts.add(result.getText(i));
      }
      logger.info("reply: number -> " + number + ", text -> " + texts);
    }

  }

  @Override
  public void extractorCoreWords(WordsRequest request, StreamObserver<WordsReply>
      responseObserver) {
    List<Word> words = request.getWordList();
    List<Result> results = new ArrayList<>();
    for (Word word : words) {
      logger.info(" ----- ------  -----   ------- -----");
      String number = word.getNumber();
      String text = word.getText();
      // 职位格式处理
      String formatTxt = formatTxt(text);
      JsonObject jsonObject = singleTest(formatTxt);
      // JsonObject jsonObject = formatText(text);
      String rText = jsonObject.toString();
      results.add(Result.newBuilder().setNumber(number).addText(rText).build());
    }
    WordsReply reply = WordsReply.newBuilder().addAllResult(results).build();
    // showLog(request, reply);
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
