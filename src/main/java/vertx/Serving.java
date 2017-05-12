package vertx;

import java.io.IOException;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by Jerry on 2017/5/11.
 */
public class Serving {

  private final static Vertx vertx = Vertx.vertx();

  public void getText(Router router) {
    router.get("/raw/:key").handler(routingContext -> {
      String key = routingContext.request().getParam("key");
      routingContext.response().setStatusCode(202).end("key");
    });
  }

  public static void main(String[] argv) throws IOException {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    vertx.createHttpServer().listen(8081);

  }
}
