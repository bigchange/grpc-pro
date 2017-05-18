package vertx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import grpc.client.JobTitleCoreWordsClient;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by Jerry on 2017/5/11.
 * http服务请求处理类
 */
public class HttpServing {

  private final static Vertx vertx = Vertx.vertx();

  private  JobTitleCoreWordsClient coreWordsClient = new JobTitleCoreWordsClient("localhost", 20299);

  private static Logger logger = LoggerFactory.getLogger(HttpServing.class);

  /**
   * indexing
   * @param router
   */
  private void index(Router router) {
    router.get("/").handler(routingContext -> {
      try {
        HttpServerResponse response = routingContext.response();
        response.end("Hi, this is a data server");
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  /**
   * 自定义服务
   * @param router
   */
  public void getText(Router router) {
    router.get("/raw/:key").handler(routingContext -> {
      String key = routingContext.request().getParam("key");
      routingContext.response().setStatusCode(202).end("key");
    });
  }

  /**
   * json format
   * @param status
   * @param msg
   * @param data
   * @return
   */
  private JsonObject formatResponse(int status, String msg, JsonObject data) {
    JsonObject resp = new JsonObject();
    resp.put("status", status);
    resp.put("msg", msg);
    resp.put("data", data);
    return resp;
  }

  /**
   * GET json jobTitle core Extractor
   * URI:/json/:key
   * @param router
   */
  private void getJsonJobTitleCores(Router router) {
    router.get("/json/:key").handler(routingContext -> {
      try {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json; charset=utf-8");
        String key = request.getParam("key");
        List<String> result = new ArrayList<>();
        coreWordsClient.testUnit(Arrays.asList(key), result);
        if (result == null || result.isEmpty()) {
          response.setStatusCode(404).end(this.formatResponse(404, "Not found", null).toString());
          return;
        }
        JsonObject data = new JsonObject(result.get(0));
        response.setStatusCode(200).end(this.formatResponse(200, "success", data).toString());
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  public static void main(String[] argv) throws IOException {
    HttpServing serving = new HttpServing();
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    serving.index(router);
    serving.getJsonJobTitleCores(router);
    int port = 8081;
    // start server
    server.requestHandler(router::accept);
    server.listen(port);
    logger.info("serving at port:" + port);

  }
}
