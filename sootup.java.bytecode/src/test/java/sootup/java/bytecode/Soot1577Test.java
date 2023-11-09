package sootup.java.bytecode;

import categories.Java8Test;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.EmptyClassLoadingOptions;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class Soot1577Test {
  final String directory = "../shared-test-resources/soot-1577/";

  @Test
  @Ignore("conversion fails - could be a dex2jar conversion problem")
  public void test() {
    AnalysisInputLocation<JavaSootClass> inputLocation =
        new JavaClassPathAnalysisInputLocation(directory);

    JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(inputLocation).build();

    JavaView view = project.createView();
    view.configBodyInterceptors((ail) -> EmptyClassLoadingOptions.Default);

    Assert.assertEquals(1, view.getClasses().size());

    view.getClasses().stream().findFirst().get().getMethods().forEach(SootMethod::getBody);
  }
}
