package crawler;

import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Created by Jerry on 2017/5/19.
 */
public class ExtractorJobTitleProcessor implements PageProcessor {

  private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(500).setTimeOut(3 * 60 * 1000)
      .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
      .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
      .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
      .setCharset("UTF-8");


  @Override
  public void process(Page page) {

    List<String> jobtitle = page.getHtml()
    .xpath("//div[@id='search_right_demo']//div[@class='clearfixed']/a/text()").all();
    page.putField("job_title", jobtitle);
    System.out.println("jobtitle:" + jobtitle.size());

  }

  @Override
  public Site getSite() {
    return site;
  }

  public static void main(String[] args) {
    Spider.create(new ExtractorJobTitleProcessor())
    .addUrl("http://sou.zhaopin.com/")
    // .addPipeline(new ConsolePipeline())
    .addPipeline(new FilePipeline("/Users/devops/workspace/shell/webmagic/jobTitle"))
    .thread(5).run();
  }

}
