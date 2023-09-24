package sootup.java.bytecode;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class Soot1580 {
  final Path jar = Paths.get("../shared-test-resources/soot-1580/jpush-android_v3.0.5.jar");

  @Test
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        PathBasedAnalysisInputLocation.create(jar, null);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(7)).addInputLocation(inputLocation).build();

    JavaView view =
        project.createView(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    Assert.assertEquals(91, view.getClasses().size());

    ClassType clazzType =
        JavaIdentifierFactory.getInstance().getClassType("cn.jpush.android.data.f");

    Assert.assertTrue(view.getClass(clazzType).isPresent());

    view.getClass(clazzType).get().getMethods().forEach(SootMethod::getBody);
  }
}
