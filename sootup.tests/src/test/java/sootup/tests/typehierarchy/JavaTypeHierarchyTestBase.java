package sootup.tests.typehierarchy;

import org.junit.jupiter.api.BeforeEach;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

import java.util.Collections;

/** @author: Hasitha Rajapakse * */
public abstract class JavaTypeHierarchyTestBase {
  // Test Resource Folder Path
  static final String baseDir = "src/test/resources/javatypehierarchy/";

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    private String className = JavaTypeHierarchyTestBase.class.getSimpleName();
    private AnalysisInputLocation srcCode;
    private JavaView view;

    @BeforeEach
    protected void starting() {

      String prevClassName = getClassName();

      setClassName(extractClassName(this.getClassName()));

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
