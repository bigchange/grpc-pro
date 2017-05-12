package grpc.impl;

import com.bgfurfeature.coreword.rpc.CoreWordsGrpc;
import com.bgfurfeature.coreword.rpc.Result;
import com.bgfurfeature.coreword.rpc.Word;
import com.bgfurfeature.coreword.rpc.WordsReply;
import com.bgfurfeature.coreword.rpc.WordsRequest;
import com.hankcs.hanlp.HanLP;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.grpc.stub.StreamObserver;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/12.
 */
public class CoreWordsImpl extends CoreWordsGrpc.CoreWordsImplBase {

  private static Logger logger =  LoggerFactory.getLogger(CoreWordsImpl.class);

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
    // 将本身添加进去
    list.add(origin);
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

  public List<String> sortByValue(List<String> set) {
    final Comparator CHINA_COMPARE = Collator.getInstance(java.util.Locale.CHINA);
    Collections.sort(set, CHINA_COMPARE);
    return set;
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


  private List<String> extractorCoreWords(List<String> doc) {
    Set<String> titles = new HashSet<>();
    for (String item : doc) {
      titles.add(item);
      List<String> keyPhrases = getKeyPhrase(item, 2);
      List<String> keyWords = getKeyWords(item, 2);
      if (keyPhrases.size() > 0) {
        for (String keyItem : keyPhrases) {
          titles.add(keyItem);
        }
      }
      if (keyWords.size() > 0) {
        for (String wordItem : keyWords) {
          titles.add(wordItem);
        }
      }
    }
    return new ArrayList<>(titles);
  }
  @Override
  public void extractorCoreWords(WordsRequest request, StreamObserver<WordsReply>
  responseObserver) {
    List<Word> words = request.getWordList();
    List<Result> results = new ArrayList<>();
    logger.info("get request:" + words);
    for (Word word: words) {
      List<String> list = new ArrayList<>();
      String number = word.getNumber();
      String text = word.getText();
      splitText(text, list);
      clearNoFormalText(list);
      List<String> titles = extractorCoreWords(list);
      results.add(Result.newBuilder().setNumber(number).addAllText(titles).build());
    }
    WordsReply reply = WordsReply.newBuilder().addAllResult(results).build();
    logger.info("reply:" + reply);
    responseObserver.onNext(reply);
    responseObserver.onCompleted();
  }
}
