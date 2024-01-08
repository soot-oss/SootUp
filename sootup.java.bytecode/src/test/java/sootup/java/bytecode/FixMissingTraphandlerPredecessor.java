package sootup.java.bytecode;

import categories.Java8Test;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootMethod;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class FixMissingTraphandlerPredecessor {
    final String directory = "../shared-test-resources/bugfixes/";

    @Test
    public void testMissingTrapHandlerPredecessor() {
        AnalysisInputLocation<JavaSootClass> inputLocation = new JavaClassPathAnalysisInputLocation(directory);
        JavaView view = new JavaView(inputLocation);
        view.getClass( view.getIdentifierFactory().getClassType("MissingTrapHandlerAssignment")).get().getMethods().forEach(SootMethod::getBody);

    }

}
