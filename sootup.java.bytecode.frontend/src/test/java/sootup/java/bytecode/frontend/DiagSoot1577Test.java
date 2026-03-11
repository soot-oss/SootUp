package sootup.java.bytecode.frontend;

import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

public class DiagSoot1577Test {

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("src/test/resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    MethodSignature sig = JavaIdentifierFactory.getInstance().getMethodSignature("cn.com.chinatelecom.account.api.c.g", "h", "int", Arrays.asList("android.content.Context"));
    Body body = view.getMethod(sig).get().getBody();
    System.out.println("=== BODY OF h METHOD ===");
    System.out.println(body);
    System.out.println("=== END ===");
  }
}
