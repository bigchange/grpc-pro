package crawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by Jerry on 2017/5/19.
 */
public class SchoolExtractor implements PageProcessor {

  private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(500).setTimeOut(3 * 60 * 1000)
      .setCharset("gbk");


  @Override
  public void process(Page page) {

    List<String> school = page.getHtml().xpath("//tr[@class='l1']/td/a/@ref/text()").all();

    Set<String> dedupSchool = new HashSet<>(school);

    page.putField("school", dedupSchool);

  }

  @Override
  public Site getSite() {
    return site;
  }

  public static void main(String[] args) {

    Spider.create(new SchoolExtractor())
    .addUrl("http://t3.zsedu.net/ulink/main.html")
    /*.addUrl("http://www.tesoon.com/a_new/htm/31/151352.htm",
    "http://www.tesoon.com/a_new/htm/31/151352_2.htm",
     "http://www.tesoon.com/a_new/htm/31/151352_3.htm",
     "http://www.tesoon.com/a_new/htm/31/151352_4.htm"
     ,"http://www.tesoon.com/a_new/htm/31/151352_5.htm",
     "http://www.tesoon.com/a_new/htm/31/151352_6.htm",
     "http://www.tesoon.com/a_new/htm/31/151352_7.htm")*/
    // .addPipeline(new ConsolePipeline())
    .addPipeline(new FilePipeline("/Users/devops/workspace/shell/webmagic/school"))
    .thread(5).run();
  }

}
