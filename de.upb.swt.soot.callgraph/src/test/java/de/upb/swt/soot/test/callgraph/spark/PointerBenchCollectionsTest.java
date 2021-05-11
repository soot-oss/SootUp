package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationDotField;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
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
import org.junit.Test;

public class PointerBenchCollectionsTest {

  private JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  private JavaClassType mainClassSignature;
  private View view;
  private Spark spark;

  public void setUp(String className) {
    String walaClassPath = "src/test/resources/spark/PointerBench";

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

    view = javaProject.createOnDemandView();

    mainClassSignature = identifierFactory.getClassType(className);
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph callGraph = algorithm.initialize(Collections.singletonList(mainMethodSignature));
    spark = new Spark.Builder(view, callGraph).build();
    spark.analyze();
  }

  private SootMethod getTargetMethod(MethodSignature targetMethodSig) {
    SootClass mainClass = (SootClass) view.getClass(mainClassSignature).get();
    Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig);
    assertTrue(targetOpt.isPresent());
    return targetOpt.get();
  }

  @Test
  public void testArray1() {
    setUp("collections.Array1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToArray =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A[]", new ArrayList<>());

    Local array = lineNumberToArray.get(20);
    Local a = lineNumberToA.get(21);
    Local b = lineNumberToA.get(23);
    Local c = lineNumberToArray.get(26);

    Set<Node> arrayPointsTo = spark.getPointsToSet(array);
    Set<Node> arrayFieldPointsTo = new HashSet<>();
    for (Node node : arrayPointsTo) {
      AllocationNode alloc = (AllocationNode) node;
      Map<Field, AllocationDotField> fields = alloc.getFields();
      for (AllocationDotField field : fields.values()) {
        if(field.getPointsToSet()!=null){
          arrayFieldPointsTo.addAll(field.getPointsToSet());
        }
      }
    }
    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    // a and array must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, arrayPointsTo).isEmpty());
    // c and b must point to a common object
    assertFalse(Sets.intersection(bPointsTo, cPointsTo).isEmpty());
  }


  private Map<Integer, Local> getLineNumberToLocalMap(
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

  private Map<Integer, JArrayRef> getLineNumberToArrayRefMap(
      SootMethod sootMethod, String typeName) {
    final ImmutableStmtGraph stmtGraph = sootMethod.getBody().getStmtGraph();
    Map<Integer, JArrayRef> res = new HashMap<>();
    for (Stmt stmt : stmtGraph) {
      int line = stmt.getPositionInfo().getStmtPosition().getFirstLine();
      List<Value> defs = stmt.getDefs();
      for (Value def : defs) {
        if (def.getType().toString().equals(typeName) && def instanceof JArrayRef) {
          res.put(line, (JArrayRef) def);
        }
      }
    }
    return res;
  }
}
