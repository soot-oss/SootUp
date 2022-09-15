package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import org.junit.Before;

public class TypeResolverTest {

    JavaView view;
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    @Before
    public void setup() {
        String baseDir = "../shared-test-resources/TypeResolverTestSuite/";
        JavaClassPathAnalysisInputLocation analysisInputLocation =
                new JavaClassPathAnalysisInputLocation(baseDir);
        JavaClassPathAnalysisInputLocation rtJar =
                new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/rt.jar");
        JavaProject project =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(analysisInputLocation)
                        .addInputLocation(rtJar)
                        .build();
        view = project.createOnDemandView();

        ClassType classType = factory.getClassType("");
        SootClass clazz = view.getClass(classType).get();

    }
}
