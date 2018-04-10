package task;

import java.util.concurrent.Callable;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import model.TFIDFImp;
import model.TfIdf;

/**
 * Created by Jerry on 2017/6/5.
 */
public class SubTaskSchedule implements Callable<Message> {

  private static Logger logger = LoggerFactory.getLogger(SubTaskSchedule.class);
  private String term;
  private String[] docTerms;
  private TFIDFImp model;
  private int index = -1;

  public SubTaskSchedule(TFIDFImp model, int index, String term, String[] docTerms) {
    this.term = term;
    this.docTerms = docTerms;
    this.model = model;
    this.index = index;
  }
  @Override
  public Message call() throws Exception {
    double tf; //term frequency
    double idf; //inverse document frequency
    double tfidf; //term requency inverse document frequency
    tf = new TfIdf().tfCalculator(docTerms, term);
    idf = new TfIdf().idfCalculator(model.getTermsDocsArray(), term);
    tfidf = tf * idf;
    // logger.info("index:"  + index + ", tfidf is:" + tfidf);
    return new Message(this.index, tfidf);
  }
}
