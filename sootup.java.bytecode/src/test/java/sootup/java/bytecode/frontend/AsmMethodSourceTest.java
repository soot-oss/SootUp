package sootup.java.bytecode.frontend;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;

import categories.Java8Test;
import java.util.Arrays;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class AsmMethodSourceTest {

  @Test
  public void testFix_StackUnderrun_convertPutFieldInsn_init() {

    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaView view = new JavaView(new DefaultRTJarAnalysisInputLocation());

    final JavaIdentifierFactory idf = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature =
        idf.getClassType("javax.management.NotificationBroadcasterSupport");
    MethodSignature mainMethodSignature =
        idf.getMethodSignature(
            mainClassSignature,
            "<init>",
            "void",
            Arrays.asList(
                "java.util.concurrent.Executor", "javax.management.MBeanNotificationInfo[]"));

    assertTrue(idf.isConstructorSubSignature(mainMethodSignature.getSubSignature()));

    final SootClass abstractClass = view.getClass(mainClassSignature).get();

    final SootMethod method = abstractClass.getMethod(mainMethodSignature.getSubSignature()).get();
    method.getBody().getStmts();
  }
}
