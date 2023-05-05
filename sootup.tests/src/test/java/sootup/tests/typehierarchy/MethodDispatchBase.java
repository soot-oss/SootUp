package sootup.tests.typehierarchy;

import categories.Java8Test;
import java.util.Collections;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/** @author Hasitha Rajapakse */
@Category(Java8Test.class)
public class MethodDispatchBase {
  static final String baseDir = "src/test/resources/methoddispatchresolver/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @ClassRule
  public static MethodDispatchBase.CustomTestWatcher customTestWatcher =
      new MethodDispatchBase.CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {
    private String className = MethodDispatchBase.class.getSimpleName();
    private JavaView view;

    @Override
    protected void starting(Description description) {
      String prevClassName = getClassName();
      setClassName(extractClassName(description.getClassName()));
      if (!prevClassName.equals(getClassName())) {
        JavaProject project =
            JavaProject.builder(new JavaLanguage(8))
                .addInputLocation(
                    new JavaSourcePathAnalysisInputLocation(
                        Collections.singleton(baseDir + "/" + getClassName())))
                .addInputLocation(
                    new JavaClassPathAnalysisInputLocation(
                        System.getProperty("java.home") + "/lib/rt.jar"))
                .build();
        setView(project.createView());
      }
    }

    public String getClassName() {
      return className;
    }

    private void setClassName(String className) {
      this.className = className;
    }

    private void setView(JavaView view) {
      this.view = view;
    }

    public JavaView getView() {
      return view;
    }
  }

  public JavaClassType getClassType(String className) {
    return identifierFactory.getClassType(className);
  }

  public static String extractClassName(String classPath) {
    String classPathArray = classPath.substring(classPath.lastIndexOf(".") + 1);
    String testDirectoryName = "";
    if (!classPathArray.isEmpty()) {
      testDirectoryName = classPathArray.substring(0, classPathArray.length() - 4);
    }
    return testDirectoryName;
  }
}
