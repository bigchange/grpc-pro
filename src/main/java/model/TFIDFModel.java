package model;

import org.apache.spark.Partition;
import org.apache.spark.TaskContext;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.rdd.RDD;

import scala.collection.Iterator;

/**
 * Created by Jerry on 2017/6/5.
 */
public class TFIDFModel {

  public static void main(String[] args) {

    HashingTF hashingTF = new HashingTF(Integer.parseInt(String.valueOf(Math.pow(2.0, 20.0))));

    // IDF idf = new IDF().fit();
  }
}
