package vertx.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;
import java.net.URLEncoder;

import io.vertx.core.json.JsonObject;

/**
 * Created by Jerry on 2017/6/6.
 */
public class FingerPrintHttpClient {

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

    FingerPrintHttpClient fingerPrintHttpClient = new FingerPrintHttpClient();
    String body = "{\"data\":[{\"phone\":\"18618251981\",\"name\":\"闵令永\"," +
        "\"email\":\"191102988@qq.com\"," +
        "\"workExprs\":[{\"company\":\"\",\"endedAt\":\"\",\"startedAt\":\"\"}]" +
        ",\"eduExprs\":[{\"school\":\"\",\"startedAt\":\"\",\"endedAt\":\"\",\"major\":\"\"}]," +
        "\"number\":\"No.1\"}]}";

    String url = "http://127.0.0.1:20199/fingerprint?data=" + URLEncoder.encode(body);

    // body = "";
    fingerPrintHttpClient.postAndReturnString(fingerPrintHttpClient.getClient(), url, body);

  }

}
