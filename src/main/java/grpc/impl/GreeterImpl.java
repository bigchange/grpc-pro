package grpc.impl;

import com.bgfurfeature.hello.rpc.GreeterGrpc;
import com.bgfurfeature.hello.rpc.HelloReply;
import com.bgfurfeature.hello.rpc.HelloRequest;
import io.grpc.stub.StreamObserver;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/10.
 * Greeter grpc service implement here
 */
public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

  private static Logger logger =  LoggerFactory.getLogger(GreeterImpl.class);

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    logger.info("message:" + request.getName());
    responseObserver.onNext(HelloReply.newBuilder().setMessage("hello," + request.getName()).build());
    responseObserver.onCompleted();
  }

  @Override
  public void sayHelloAgain(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    logger.info("message:" + request.getName());
    responseObserver.onNext(HelloReply.newBuilder().setMessage("helloAgain:" + request.getName()).build());
    responseObserver.onCompleted();
  }
}
