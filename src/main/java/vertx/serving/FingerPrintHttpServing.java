package vertx.serving;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import com.inmind.idmg.fingerprint.rpc.FingerPrintReply;
import com.inmind.idmg.fingerprint.rpc.ResumeFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import grpc.client.FingerPrintClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by Jerry on 2017/5/11.
 * http服务请求处理类
 */
public class FingerPrintHttpServing {

  // json转换成proto的解析器
  private static JsonFormat.Parser parser = JsonFormat.parser();

  private static JsonFormat.Printer printer = JsonFormat.printer();

  private final static Vertx vertx = Vertx.vertx();

  /*private FingerPrintClient fingerPrintClient;

  public FingerPrintHttpServingMain(String server) {
    String[] serverInfo = server.split(":");
    fingerPrintClient = new FingerPrintClient(serverInfo[0], Integer.parseInt(serverInfo[1]));
  }*/

  private FingerPrintClient fingerPrintClient = new FingerPrintClient("127.0.0.1", 20399);

  private static Logger logger = LoggerFactory.getLogger(FingerPrintHttpServing.class);

  /**
   * indexing
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
   */
  public void getText(Router router) {
    router.get("/raw/:key").handler(routingContext -> {
      String key = routingContext.request().getParam("key");
      routingContext.response().setStatusCode(202).end("key");
    });
  }

  /**
   * json format
   */
  private JsonObject formatResponse(int status, String msg, JsonObject data) {
    JsonObject resp = new JsonObject();
    resp.put("status", status);
    resp.put("msg", msg);
    resp.put("data", data);
    return resp;
  }

  private String dealingWithReply(FingerPrintReply reply) {
    String printerString = "";
    if (reply != null) {
      int replys = reply.getFingerPrintCount();
      for (int i = 0; i < replys; i++) {
        try {
          printerString = printer.print(reply);
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        logger.info("return reply type:" + reply.getFingerPrint(i).getTypeValue() + ", " +
            "finger print:" + reply.getFingerPrint(i).getFingerPrint());
      }
    } else {
      logger.info("reply is null!!");
    }

    return printerString;
  }

  /**
   * 获取简历的特征请求数据
   */
  private void getResumeFeatureList(JsonObject jsonObject, ArrayList<ResumeFeature> list) throws
      Exception {
    JsonArray jsonArray = jsonObject.getJsonArray("data", null);
    if (jsonArray != null) {
      int size = jsonArray.size();
      for (int i = 0; i < size; i++) {
        ResumeFeature.Builder resumeFeatureBuilder = ResumeFeature.newBuilder();
        JsonObject item = jsonArray.getJsonObject(i);
        parser.ignoringUnknownFields().merge(item.toString(), resumeFeatureBuilder);
        list.add(resumeFeatureBuilder.build());
      }
    }
  }

  /**
   * BODY POST
   * @param router
   * @throws Exception
   */
  private void getFingerPrintBody(Router router) {
    // post { data:[ ResumeFeature ] }
    router.post("/fingerprint").handler(routingContext -> {
      try {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json; charset=utf-8");
        Buffer buffer = routingContext.getBody();
        if (buffer.length() > 0) {
        JsonObject bodyAsJson = routingContext.getBodyAsJson();
        // logger.info("bodyAsJson is -> " + bodyAsJson);
        ArrayList<ResumeFeature> list = new ArrayList<>();
          try {
            getResumeFeatureList(bodyAsJson, list);
          } catch (Exception e) {
            e.printStackTrace();
          }
          String responseString = dealingWithReply(fingerPrintClient.doFingerPrint(list));
        logger.info("response string -> " + responseString);
        if (responseString == null || responseString.isEmpty()) {
          response.setStatusCode(404).end(this.formatResponse(404, "Not found", null).toString());
          return;
        }
        JsonObject respond = new JsonObject(responseString);
        response.setStatusCode(200).end(this.formatResponse(200, "success", respond).toString());
        } else {
          response.setStatusCode(404).end(this.formatResponse(404, "request body empty", null)
              .toString());
          return;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  /**
   * post json data resumeFeature
   * param post
   * URI:/fingerprint
   */
  private void getFingerPrint(Router router) {
    // post { data:[ ResumeFeature ] }
    router.post("/fingerprint").handler(routingContext -> {
      try {
        HttpServerRequest request = routingContext.request();
        String data = request.getParam("data");
        logger.info("data param is -> " + data);
        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json; charset=utf-8");
        if (data != null) {
          JsonObject bodyAsJson = new JsonObject(data);
          // logger.info("bodyAsJson is -> " + bodyAsJson);
          ArrayList<ResumeFeature> list = new ArrayList<>();
          try {
            getResumeFeatureList(bodyAsJson, list);
          } catch (Exception e) {
            e.printStackTrace();
          }
          String responseString = dealingWithReply(fingerPrintClient.doFingerPrint(list));
          logger.info("response string -> " + responseString);
          if (responseString == null || responseString.isEmpty()) {
            response.setStatusCode(404).end(this.formatResponse(404, "Not found", null).toString());
            return;
          }
          JsonObject respond = new JsonObject(responseString);
          response.setStatusCode(200).end(this.formatResponse(200, "success", respond).toString());
        } else {
          response.setStatusCode(404).end(this.formatResponse(404, "request body empty", null)
              .toString());
          return;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  public static void main(String[] argv) throws IOException {
    FingerPrintHttpServing serving = new FingerPrintHttpServing();
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    serving.index(router);
    serving.getFingerPrintBody(router);
    int port = 20199;
    // start server
    server.requestHandler(router::accept);
    server.listen(port);
    logger.info("serving at port:" + port);

  }
}
