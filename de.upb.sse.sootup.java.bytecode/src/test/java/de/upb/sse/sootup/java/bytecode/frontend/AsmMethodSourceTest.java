package de.upb.sse.sootup.java.bytecode.frontend;

import static junit.framework.TestCase.fail;

import categories.Java8Test;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class AsmMethodSourceTest {

  @Test
  @Ignore("FIXME")
  public void testFix_StackUnderrun_convertPutFieldInsn_init() {

    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .build();

    JavaView view = javaProject.createOnDemandView();

    JavaClassType mainClassSignature =
        JavaIdentifierFactory.getInstance()
            .getClassType("javax.management.NotificationBroadcasterSupport");
    MethodSignature mainMethodSignature =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                mainClassSignature,
                "<init>",
                "void",
                Arrays.asList(
                    "java.util.concurrent.Executor", "javax.management.MBeanNotificationInfo[]"));

    final SootClass<?> abstractClass = view.getClass(mainClassSignature).get();

    final SootMethod method = abstractClass.getMethod(mainMethodSignature.getSubSignature()).get();
    method.getBody().getStmts();
  }
}
