package de.upb.sse.sootup.test.java.bytecode;

import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import de.upb.sse.sootup.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class Soot1580 {
  final Path jar = Paths.get("../shared-test-resources/soot-1580/jpush-android_v3.0.5.jar");

  @Test
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new PathBasedAnalysisInputLocation(jar, null);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(7)).addInputLocation(inputLocation).build();

    JavaView view =
        project.createOnDemandView(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    Assert.assertEquals(91, view.getClasses().size());

    ClassType clazzType =
        JavaIdentifierFactory.getInstance().getClassType("cn.jpush.android.data.f");

    Assert.assertTrue(view.getClass(clazzType).isPresent());

    view.getClass(clazzType).get().getMethods().forEach(SootMethod::getBody);
  }
}
