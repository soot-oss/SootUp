package de.upb.sse.sootup.test.java.bytecode;

import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import de.upb.sse.sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.views.JavaView;
import org.junit.Assert;
import org.junit.Test;

public class Soot1577 {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation(directory);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(7)).addInputLocation(inputLocation).build();

    JavaView view =
        project.createView(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
