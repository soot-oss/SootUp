package sootup.java.bytecode;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class Soot1580Test {
  final String jar = "../shared-test-resources/soot-1580/jpush-android_v3.0.5.jar";

  @Test
  @Ignore("Localsplitter fails; bytecode itself is somehow strange")
  public void test() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            jar); // TODO: maybe you need to add add interceptors (should be there when they are
    // enabled by default)

    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    Assert.assertEquals(91, view.getClasses().size());

    ClassType clazzType =
        JavaIdentifierFactory.getInstance().getClassType("cn.jpush.android.data.f");

    Assert.assertTrue(view.getClass(clazzType).isPresent());

    view.getClass(clazzType).get().getMethods().forEach(SootMethod::getBody);
  }
}
