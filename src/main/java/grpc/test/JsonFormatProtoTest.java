package grpc.test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import com.bgfurfeature.coreword.rpc.Word;
import com.inmind.idmg.fingerprint.rpc.ResumeFeature;

import org.apache.avro.TestAnnotation;
import org.junit.Test;

import scala.tools.cmd.gen.AnyVals;

/**
 * Created by Jerry on 2017/5/19.
 * json -> proto对应message测试
 */
public class JsonFormatProtoTest {

  // json转换成proto的解析器
  private static JsonFormat.Parser parser = JsonFormat.parser();

  public static void println(String s) {
    System.out.println(s);
  }

  @Test
  public void formatTest() {

    String srcJson = "{\"number\": \"No.1\", \"text\": \"c++开发工程师\"}";

    Word.Builder wordBuilder = Word.newBuilder();
    try {
      parser.ignoringUnknownFields().merge(srcJson, wordBuilder);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    String number = wordBuilder.getNumber();
    String text = wordBuilder.getText();
    println("number:" + number + ", text:" + text);
    wordBuilder.setNumber("No.2");
    println("number:" + wordBuilder.getNumber() + ", text" + wordBuilder.getText());
    wordBuilder.build();
    wordBuilder.clear();
  }

  /**
   * finger print feature json format
   * @throws Exception
   */
  @Test
  public void formatReusmeFeature() throws Exception {
    String json = "{\"phone\":\"188XXX\",\"name\":\"\",\"email\":\"\"," +
        "\"workExprs\":[{\"company\":\"\",\"endedAt\":\"\",\"startedAt\":\"\"}]" +
         ",\"eduExprs\":[{\"school\":\"tsinghua\",\"startedAt\":\"\",\"endedAt\":\"\",\"major\":\"\"}]," +
         "\"number\":\"\"}";

    ResumeFeature.Builder resumeFeatureBuilder = ResumeFeature.newBuilder();
    parser.merge(json, resumeFeatureBuilder);

    String phone = resumeFeatureBuilder.getPhone(); // "188xxx"

    println(resumeFeatureBuilder.toString());
  }

public static void main(String[] args)  throws  Exception {
    println("Hello java!!");
  }

}
