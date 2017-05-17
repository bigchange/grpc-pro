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
   * Each key value is JsonArray
   * 先分隔 '/'后每个词在处理核心词
   */
  public JsonObject formatText(String text) {
    String[] formatTxt = text.split("/");
    JsonArray levelArray = new JsonArray();
    JsonArray coreArray = new JsonArray();
    JsonArray posArray = new JsonArray();
    JsonArray originArray = new JsonArray();
    JsonObject jsonObject = new JsonObject();
    for (String txt : formatTxt) {
      originArray.add(txt);
      String jobClass = findJobByDict(classDict, txt);
      levelArray.add(jobClass);
      String jobPos = findJobByDict(posDict, txt);
      posArray.add(jobPos);
      String coreWord = txt.replace(jobPos, "").replace(jobClass, "");
      coreArray.add(coreWord);
      jsonObject = new JsonObject();
      jsonObject.put("origin", originArray).put("core", coreArray).put("level", levelArray)
          .put("pos", posArray);
    }
    return jsonObject;
  }

  /**
   * 先处理核心词，存在分隔符 '/' 的核心词拆开
   * 未处理可能存在的分隔符（'&' '、' ';'）
   */
  public JsonObject singleTest(String text) {
    JsonObject jsonObject = new JsonObject();
    JsonArray coreArray = new JsonArray();
    String coreWord = "";
    String jobClass = findJobByDict(classDict, text);
    String jobPos = findJobByDict(posDict, text);
    // 技术总监/产品总监 - 优先选择level匹配到的（如果pos中有和level中相同部分）
    if (!"".equals(jobClass) && jobPos.contains(jobClass) &&
        jobPos.length() - jobClass.length() >= 2) {
      coreWord = text.replace(jobClass, "");
      jobPos = jobClass;
    } else {
      coreWord = text.replace(jobPos, "").replace(jobClass, "");
    }
    String[] formatTxt = coreWord.trim().split("/");
    for (String txt : formatTxt) {
      if (!"".equals(txt)) {
        coreArray.add(txt);
      }
    }
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
      JsonObject jsonObject = singleTest(text);
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
