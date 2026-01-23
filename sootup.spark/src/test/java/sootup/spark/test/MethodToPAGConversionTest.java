package sootup.spark.test;


import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodToPAGConversionTest {

    @Test
    public void methodToPAG() {
        AnalysisInputLocation input = new JavaClassPathAnalysisInputLocation("target/test-classes");
        View view = new JavaView(input);
        ClassType classSig = JavaIdentifierFactory.getInstance().getClassType("sootup.spark.target.Basic");
        Optional<? extends SootClass> classOpt = view.getClass(classSig);
        assertTrue(classOpt.isPresent());

        SootClass sootClass = classOpt.get();
        MethodSignature methodSig = JavaIdentifierFactory.getInstance()
                .getMethodSignature("sootup.spark.target.Basic",
                        "main",
                        "void",
                        Collections.singletonList("java.lang.String[]"));
        sootClass.getMethod(methodSubSig);

    }
}
