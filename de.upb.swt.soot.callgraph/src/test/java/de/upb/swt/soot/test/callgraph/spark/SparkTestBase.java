package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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
import java.util.*;

public abstract class SparkTestBase {

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected View view;
  protected Spark spark;
  protected static final String pointerBenchClassPath = "src/test/resources/spark/PointerBench";
  protected static final String sparkBasicTestClassPath = "src/test/resources/spark/basic";

  protected void setUpPointerBench(String className) {
    setUp(pointerBenchClassPath, className);
  }

  protected void setUpBasicTest(String className) {
    setUp(sparkBasicTestClassPath, className);
  }

  private void setUp(String classPath, String className) {

    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();

    view = javaProject.createOnDemandView();

    mainClassSignature = identifierFactory.getClassType(className);
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph callGraph = algorithm.initialize(Collections.singletonList(mainMethodSignature));
    spark = new Spark.Builder(view, callGraph, Collections.emptyList()).build();
    spark.analyze();
  }

  protected SootMethod getTargetMethod(MethodSignature targetMethodSig) {
    SootClass mainClass = (SootClass) view.getClass(mainClassSignature).get();
    Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig.getSubSignature());
    assertTrue(targetOpt.isPresent());
    return targetOpt.get();
  }

  protected SootMethod getTargetMethodFromClass(
      MethodSignature targetMethodSig, JavaClassType classSig) {
    SootClass mainClass = (SootClass) view.getClass(classSig).get();
    Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig.getSubSignature());
    assertTrue(targetOpt.isPresent());
    return targetOpt.get();
  }

  protected Map<Integer, Local> getLineNumberToLocalMap(
      SootMethod sootMethod, String typeName, List<Local> params) {
    final ImmutableStmtGraph stmtGraph = sootMethod.getBody().getStmtGraph();
    Map<Integer, Local> res = new HashMap<>();
    for (Stmt stmt : stmtGraph) {
      int line = stmt.getPositionInfo().getStmtPosition().getFirstLine();
      List<Value> defs = stmt.getDefs();
      List<Value> uses = stmt.getUses();
      for (Value def : defs) {
        if (def.getType().toString().equals(typeName) && def instanceof Local) {
          for (Value use : uses) {
            // parameter mapping to local
            if (use instanceof JParameterRef && use.getType().toString().equals(typeName)) {
              params.add((Local) def);
            }
          }
          res.put(line, (Local) def);
        }
      }
    }
    return res;
  }
}
