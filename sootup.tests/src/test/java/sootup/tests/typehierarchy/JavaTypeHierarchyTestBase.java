package sootup.tests.typehierarchy;

import org.junit.jupiter.api.BeforeEach;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author: Hasitha Rajapakse *
 */
public abstract class JavaTypeHierarchyTestBase {
  // Test Resource Folder Path
  static final String baseDir = "src/test/resources/javatypehierarchy/";

  private String className = "";
  private JavaView view;

  @BeforeEach
  protected void setupTest() {
    String prevClassName = getClassName();

    setClassName(extractClassName(this.getClass().getSimpleName()));

    if (!prevClassName.equals(getClassName())) {
      AnalysisInputLocation srcCode =
          new JavaClassPathAnalysisInputLocation(baseDir + "/" + getClassName() + "/binary");
      JavaView view = new JavaView(srcCode);
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

  public JavaClassType getClassType(String className) {
    return view.getIdentifierFactory().getClassType(className);
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
