package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

public class Soot1580Test {
  final String jar = "../shared-test-resources/soot-1580/jpush-android_v3.0.5.jar";

  @Test
  @Disabled("Localsplitter fails; bytecode itself is somehow strange")
  public void test() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            jar); // TODO: maybe you need to add add interceptors (should be there when they are
    // enabled by default)

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    assertEquals(91, view.getClasses().count());

    ClassType clazzType = view.getIdentifierFactory().getClassType("cn.jpush.android.data.f");

    assertTrue(view.getClass(clazzType).isPresent());

    view.getClass(clazzType).get().getMethods().forEach(SootMethod::getBody);
  }
}
