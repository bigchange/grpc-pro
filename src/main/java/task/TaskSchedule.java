package task;

import java.util.concurrent.Callable;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import model.TFIDFImp;
import pool.ThreadPool;

/**
 * Created by Jerry on 2017/6/5.
 */
public class TaskSchedule implements Callable<Message> {

  private static Logger logger = LoggerFactory.getLogger(TaskSchedule.class);
  private int index = -1;
  private String[] docTerms;
  private TFIDFImp model;

  public TaskSchedule(TFIDFImp model, int index, String[] docTerms) {
    this.index = index;
    this.docTerms = docTerms;
    this.model = model;
  }
  @Override
  public Message call() throws Exception {
    double tf; //term frequency
    double idf; //inverse document frequency
    double tfidf; //term requency inverse document frequency
    double[] tfidfvectors = new double[model.getAllTerms().size()];
    int count = 0;
    for (String terms : model.getAllTerms()) {
      /*tf = new TfIdf().tfCalculator(docTerms, terms);
      idf = new TfIdf().idfCalculator(model.getTermsDocsArray(), terms);
      tfidf = tf * idf;
      tfidfvectors[count] = tfidf;*/
      ThreadPool.getEcsTwo().submit(new SubTaskSchedule(this.model, count, terms, docTerms));

      count++;
    }
    count = 0;
    for (String terms : model.getAllTerms()) {
      if (count % 1000 == 0) {
        logger.info("finish terms -> " + count);
      }
      Message message = ThreadPool.getEcsTwo().take().get();
      tfidfvectors[message.getIndex()] = message.getTfidf();

      count ++;
    }
    return new Message(this.index, tfidfvectors);
  }
}
