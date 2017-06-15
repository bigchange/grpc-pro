package vertx.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.IOException;

/**
 * Created by Jerry on 2017/6/7.
 */
public class SearchHttpClient {

  private HttpClientParams params = new HttpClientParams();
  private HttpClient client = new HttpClient(params);

  public HttpClient getClient() {
    return client;
  }

  public String postAndReturnString(HttpClient client, String url, String body) {
    PostMethod httpPost = null;
    try {
      httpPost = new PostMethod(url);
      httpPost.setRequestEntity(new StringRequestEntity(body, "application/json", "utf8"));
      int code = client.executeMethod(httpPost);
      if (code != 200) {
        return null;
      }
      String ret = httpPost.getResponseBodyAsString();
      return ret;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        httpPost.releaseConnection();
      } catch (Exception ex) {
      }
    }
  }

  public static void main(String args[]) {

    SearchHttpClient searchHttpClient = new SearchHttpClient();

    String body = "{\"query\":\"北京大学 java工程师\",\"pageSize\":10,\"page\":0}";
    String url =  "http://xxx:20202/search/api";
     // "http://xxx:20202/search/api"; // online
    String string = searchHttpClient.postAndReturnString(searchHttpClient.getClient(), url, body);
    System.out.println(string);
  }
}
