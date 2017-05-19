package grpc.test;

import com.google.protobuf.util.JsonFormat;

import com.bgfurfeature.coreword.rpc.Word;

/**
 * Created by Jerry on 2017/5/19.
 * json -> proto对应message测试
 */
public class JsonFormatProtoTest {

  private static String srcJson = "{\"number\": \"No.1\", \"text\": \"c++开发工程师\"}";

  // json转换成proto的解析器
  private static JsonFormat.Parser parser = JsonFormat.parser();

  public static void println(String s) {
    System.out.println(s);
  }

  public static void main(String[] args)  throws  Exception {
    Word.Builder wordBuilder = Word.newBuilder();
    parser.ignoringUnknownFields().merge(srcJson, wordBuilder);
    String number = wordBuilder.getNumber();
    String text = wordBuilder.getText();
    println("number:" + number + ", text:" + text);
    wordBuilder.setNumber("No.2");
    println("number:" + wordBuilder.getNumber() + ", text" + wordBuilder.getText());
    wordBuilder.build();
    wordBuilder.clear();
  }

}
