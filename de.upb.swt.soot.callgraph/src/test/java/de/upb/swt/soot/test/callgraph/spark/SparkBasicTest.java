package de.upb.swt.soot.test.callgraph.spark;

import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static junit.framework.TestCase.*;

public class SparkBasicTest {

    protected String testDirectory, className;
    protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    protected JavaClassType mainClassSignature;
    protected MethodSignature mainMethodSignature;

    private void setup(String testDirectory, String className) {
        String walaClassPath = "src/test/resources/callgraph/" + testDirectory;

        double version = Double.parseDouble(System.getProperty("java.specification.version"));
        if (version > 1.8) {
            fail("The rt.jar is not available after Java 8. You are using version " + version);
        }

        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addClassPath(
                                new JavaClassPathAnalysisInputLocation(
                                        System.getProperty("java.home") + "/lib/rt.jar"))
                        .addClassPath(new JavaSourcePathAnalysisInputLocation(walaClassPath))
                        .build();

        View view = javaProject.createOnDemandView();

        mainClassSignature = identifierFactory.getClassType(className);
        mainMethodSignature =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

        SootClass sc = (SootClass) view.getClass(mainClassSignature).get();
        Optional<SootMethod> m = sc.getMethod(mainMethodSignature);
        assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

        final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
        algorithm = createAlgorithm(view, typeHierarchy);
        CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

        assertTrue(
                mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
        assertNotNull(cg);
        return cg;
    }

    @Test
    public void simplePointsToAnalysis(){

    }
}
