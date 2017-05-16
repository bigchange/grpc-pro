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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.grpc.stub.StreamObserver;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import utils.FileContentUtil;

/**
 * Created by Jerry on 2017/5/12.
 */
public class CoreWordsImpl extends CoreWordsGrpc.CoreWordsImplBase {

  private static Logger logger =  LoggerFactory.getLogger(CoreWordsImpl.class);

  private List<String> classDict = new ArrayList<>();

  private List<String> posDict = new ArrayList<>();

  public CoreWordsImpl () {
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
    for (String dic: dict) {
      if (text.contains(dic)) {
        if (dic.length() > maxMatch.length()) {
          maxMatch = dic;
        } else if (dic.length() == maxMatch.length()) {
          if (text.indexOf(dic) > text.indexOf(maxMatch)) {
            maxMatch  = dic;
          }
        }
        if (dic.length() <= minMath.length()) {
          minMath = dic;
        } else if (text.indexOf(dic) > text.indexOf(minMath)) {
          minMath  = dic;
        }
      }
    }
    logger.info("findJobByDict -> " + text + ", min match -> " + minMath + ", match max -> " + maxMatch);
    if (maxMatch.isEmpty()) {
      return "";
    } else if (!maxMatch.isEmpty() && text.length() - maxMatch.length() >= 2) {
      return maxMatch;
    } else if (!minMath.isEmpty()) {
      return minMath;
    }
    return "";
  }

  public List<String> sortByValue(List<String> set) {
    final Comparator CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);
    Collections.sort(set, CHINA_COMPARE);
    return set;
  }

  /**
   * 显示日志
   * @param request
   * @param reply
   */
  public void showLog(WordsRequest request, WordsReply reply) {
    if (request == null) {
      return;
    }
    for (Word word: request.getWordList()) {
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
    for (Word word: words) {
      logger.info(" ----- ------  -----   ------- -----" );
      List<String> list = new ArrayList<>();
      String number = word.getNumber();
      String text = word.getText();
      String jobClass = findJobByDict(classDict, text);
      String jobPos = findJobByDict(posDict, text);
      String coreWord = text.replace(jobPos, "").replace(jobClass,"");
      logger.info("joblevel is:" + jobClass + ", job pos is :" + jobPos + ", core word :" + coreWord);
      JsonObject jsonObject = new JsonObject();
      jsonObject.put("origin", text).put("core", coreWord).put("level", jobClass).put("pos", jobPos);
      String rText = jsonObject.toString();
      results.add(Result.newBuilder().setNumber(number).addText(rText).build());
    }
    WordsReply reply = WordsReply.newBuilder().addAllResult(results).build();
    // showLog(request, reply);
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
