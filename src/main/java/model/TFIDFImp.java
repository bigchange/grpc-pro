package model;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import pool.ThreadPool;
import task.Message;
import task.TaskSchedule;
import utils.FileContentUtil;

/**
 * Created by Jerry on 2017/5/23.
 * tf-idf model for extracting job core words
 */
public class TFIDFImp {

  private static Logger logger = LoggerFactory.getLogger(TFIDFImp.class);

  //This variable will hold all terms of each document in an array.
  private List<String[]> termsDocsArray = new ArrayList<>();
  //to hold all terms
  private List<String> allTerms = new ArrayList<>();
  private List<double[]> tfidfDocsVector = new ArrayList<>();

  private List<String> lineList = new ArrayList<>();

  public TFIDFImp(String path) {
    logger.info("init model...");
     parseFile(path);
     fit();
  }

  public List<String> getAllTerms() {
    return allTerms;
  }

  public List<String[]> getTermsDocsArray() {
    return termsDocsArray;
  }

  public void parseFile(String path) {
    logger.info("parseFile");
    try {
      FileContentUtil.readLines(lineList, path);
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (String line : lineList) {
      String[] doc = line.split(",");
      termsDocsArray.add(doc);

      for (String term : doc) {
        allTerms.add(term);
      }
    }

    lineList.clear();

  }

  /**
   * Method to create termVector according to its tfidf score.
   */
  public void fit() {
    logger.info("fit tf-idf model");
    double tf; //term frequency
    double idf; //inverse document frequency
    double tfidf; //term requency inverse document frequency
    logger.info("doc train size:" + termsDocsArray.size());
    logger.info("all term size:" + allTerms.size());
    int total = termsDocsArray.size();
    int epcho = (int) (total * 0.2);
    int index = 0;
    for (String[] docTermsArray : termsDocsArray) {

      ThreadPool.getEcs().submit(new TaskSchedule(this, index, docTermsArray));
      /*
      int count = 0;
      if (index % epcho == 0) {
        logger.info("train process -> " + index / (total * 1.0) + "%");
      }
      for (String terms : allTerms) {
        tf = new TfIdf().tfCalculator(docTermsArray, terms);
        idf = new TfIdf().idfCalculator(termsDocsArray, terms);
        tfidf = tf * idf;
        tfidfvectors[count] = tfidf;
        count++;
      }
      tfidfDocsVector.add(tfidfvectors);  //storing document vectors;*/

    }
    index = 0;
    for (String[] docTermsArray: termsDocsArray) {
      try {
        Message msg = (Message) ThreadPool.getEcs().take().get();
        tfidfDocsVector.add(msg.getIndex(), msg.getVector());
        logger.info("tfidfDocsVector size:" + tfidfDocsVector.size());

      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

    ThreadPool.getExecutorService().shutdown();
    ThreadPool.getExecutorServiceTwo().shutdown();

    logger.info("finish train ...");
  }

  /**
   * 职位分词
   */
  private String[] segment(String doc) {
    HashSet<String> set = new HashSet<>();
    List<String> keyWords = HanLP.extractKeyword(doc, 8);
    List<String> phrases = HanLP.extractPhrase(doc, 8);
    keyWords.addAll(phrases);
    for (String sting : keyWords) {
      set.add(sting);
    }

    List<Term> list = HanLP.segment(doc);
    for (Term term : list) {
      set.add(term.word);
    }
    String[] docTerms = new String[set.size()];
    set.toArray(docTerms);
    logger.info("doc term size:" + docTerms.length);
    return docTerms;
  }

  /**
   * 计算文档的tfidf值
   */
  public double[] tfIdfCalculator(String doc) {
    String[] docTerm = segment(doc);
    double tf; //term frequency
    double idf; //inverse document frequency
    double tfidf; //term requency inverse document frequency
    double[] tfidfvectors = new double[allTerms.size()];
    int count = 0;
    for (String terms : allTerms) {
      tf = new TfIdf().tfCalculator(docTerm, terms);
      idf = new TfIdf().idfCalculator(termsDocsArray, terms);
      tfidf = tf * idf;
      tfidfvectors[count] = tfidf;
      count++;
    }
    return tfidfvectors;  //storing document vectors;
  }

  /**
   * Method to calculate cosine similarity between all the documents.
   */
  public List<Map.Entry<Integer,Double>> getCosineSimilarity(String doc, int topSize) {

    Map<Integer,Double> cosMap = new HashMap<>();

    double[] vector = tfIdfCalculator(doc);

    for (int i = 0; i < tfidfDocsVector.size(); i++) {
      cosMap.put(i,new CosineSimilarity().cosineSimilarity(vector, tfidfDocsVector.get(i)));
    }

    List<Map.Entry<Integer,Double>> itemList = new ArrayList<>(cosMap.entrySet());

    Collections.sort(itemList, new Comparator<Map.Entry<Integer, Double>>() {
      @Override
      public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
        if (o1.getValue() > o2.getValue()) {
          return 1;
        } else {
          return -1;
        }
      }
    });

    return itemList.subList(0, topSize);
  }

}
