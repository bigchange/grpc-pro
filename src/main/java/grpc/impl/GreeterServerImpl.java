package grpc.impl;

import com.bgfurfeature.hello.rpc.GreeterGrpc;
import com.bgfurfeature.hello.rpc.HelloReply;
import com.bgfurfeature.hello.rpc.HelloRequest;

import java.util.List;

import io.grpc.stub.StreamObserver;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import model.Simhash;
import model.SuggesterText;

/**
 * Created by Jerry on 2017/5/10.
 * Greeter grpc service implement here
 */
public class GreeterServerImpl extends GreeterGrpc.GreeterImplBase {

  private String filePath;

  private  SuggesterText suggesterText;

  private  Simhash simhash;

  public GreeterServerImpl(String path) {
    this.filePath = path;
    /*try {
      suggesterText = new SuggesterText(filePath);
      suggesterText.readFileTree().feedData();
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    // simhash = new Simhash(4, 20);

  }

  private static Logger logger = LoggerFactory.getLogger(GreeterServerImpl.class);

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    String respondMessage = "";
    logger.info("message:" + request.getName());
    try {
      // simhash.calSimilarity(request.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    responseObserver.onNext(HelloReply.newBuilder().setMessage("hello," + respondMessage).build());
    responseObserver.onCompleted();
  }

  @Override
  public void sayHelloAgain(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    logger.info("message:" + request.getName());
    responseObserver.onNext(HelloReply.newBuilder().setMessage("helloAgain:" + request.getName()).build());
    responseObserver.onCompleted();
  }
}
