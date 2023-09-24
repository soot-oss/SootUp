package sootup.java.bytecode;

import org.junit.Assert;
import org.junit.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class Soot1577 {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation(directory);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(7)).addInputLocation(inputLocation).build();

    JavaView view = project.createView();

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
