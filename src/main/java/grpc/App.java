package grpc;

import io.vertx.core.json.JsonObject;

/**
 * Hello world!
 */
public class App {
  public static void main(String[] args) throws Exception {
    String str = "{\"time\":1,\"startedAt\":\"2014-07\"," +
        "\"company\":\"\\u4e0a\\u6d77\\u797a\\u9cb2\\u4fe1\\u606f\\u79d1\\u6280" +
        "\\u6709\\u9650\\u516c\\u53f8\",\"endedAt\":\"2015-06\"}";
    JsonObject json = new JsonObject(str);
    System.out.println(new String(json.getString("company", "-").getBytes(), "utf-8"));
    System.out.println(json.getString("company", "-"));
  }
}
