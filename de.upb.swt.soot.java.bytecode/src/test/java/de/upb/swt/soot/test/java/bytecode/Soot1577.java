package de.upb.swt.soot.test.java.bytecode;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
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
        project.createOnDemandView(analysisInputLocation -> BytecodeClassLoadingOptions.Default);

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
