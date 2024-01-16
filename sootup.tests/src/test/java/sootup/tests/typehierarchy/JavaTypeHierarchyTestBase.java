package sootup.tests.typehierarchy;

import java.util.Collections;
import org.junit.ClassRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

/** @author: Hasitha Rajapakse * */
public abstract class JavaTypeHierarchyTestBase {
  // Test Resource Folder Path
  static final String baseDir = "src/test/resources/javatypehierarchy/";

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @ClassRule public static CustomTestWatcher customTestWatcher = new CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {

    private String className = JavaTypeHierarchyTestBase.class.getSimpleName();
    private AnalysisInputLocation srcCode;
    private JavaView view;

    @Override
    protected void starting(Description description) {

      String prevClassName = getClassName();

      setClassName(extractClassName(description.getClassName()));

      if (!prevClassName.equals(getClassName())) {
        srcCode =
            new JavaSourcePathAnalysisInputLocation(
                Collections.singleton(baseDir + "/" + getClassName()));
        JavaView view = new JavaView(this.srcCode);
        setView(view);
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
