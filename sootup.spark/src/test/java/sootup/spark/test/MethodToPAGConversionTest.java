package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;
import sootup.spark.PAG;
import sootup.spark.Solver;

public class MethodToPAGConversionTest {

  @Test
  public void methodToPAG() {
    AnalysisInputLocation input = new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary");
    View view = new JavaView(input);
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    ClassType classSig = idFactory.getClassType("Basic");
    Optional<? extends SootClass> classOpt = view.getClass(classSig);
    assertTrue(classOpt.isPresent());

    MethodSignature mainMethodSig =
        idFactory.getMethodSignature(classSig, idFactory.getMainSubSignature());

    Optional<? extends SootMethod> method = view.getMethod(mainMethodSig);
    assertTrue(method.isPresent());

    Solver solver =
        Solver.builder().view(view).entryPoints(Collections.singletonList(mainMethodSig)).build();
    solver.solve();

    PAG pag = solver.getPag();
    //SparkTestUtil.vizualizeMehodPAG(pag.getDelegate());
  }
}
