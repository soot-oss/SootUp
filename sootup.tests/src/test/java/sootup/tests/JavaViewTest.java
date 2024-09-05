package sootup.tests;

import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class JavaViewTest {

  @Test
  public void testGetClassByClassType() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "C:/Users/Stefan/Desktop/Hektor/achilles-benchmark/unmodified/httpclient-4.1.3.jar");
    JavaView view = new JavaView(inputLocation);
    //        System.out.println(view.getClasses());

    JavaClassType classType =
        view.getIdentifierFactory()
            .getClassType("org.apache.http.impl.client.DefaultHttpRequestRetryHandler");
    Optional<JavaSootClass> classOpt = view.getClass(classType);
    System.out.println(classOpt);

    //        System.out.println(view.getClasses());
  }
}
