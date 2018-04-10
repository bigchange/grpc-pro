package task;

/**
 * Created by Jerry on 2017/6/5.
 */
public class Message {
  private int index;
  private double[] vector;
  private double tfidf;
  public Message(int index, double[] vector) {
    this.index = index;
    this.vector = vector;
  }

  public Message(int index, double tfidf) {
    this.index = index;
    this.tfidf = tfidf;
  }

  public int getIndex() {
    return index;
  }

  public double[] getVector() {
    return vector;
  }

  public double getTfidf() {
    return tfidf;
  }
}
