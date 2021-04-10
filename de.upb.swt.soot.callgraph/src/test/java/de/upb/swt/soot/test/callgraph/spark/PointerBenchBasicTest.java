package de.upb.swt.soot.test.callgraph.spark;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
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
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.*;

public class PointerBenchBasicTest {

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
        spark = new Spark(view, callGraph);
        spark.analyze();
    }

    private SootMethod getTargetMethod(MethodSignature targetMethodSig){
        SootClass mainClass = (SootClass) view.getClass(mainClassSignature).get();
        Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig);
        assertTrue(targetOpt.isPresent());
        return targetOpt.get();
    }

    @Test
    public void testSimpleAlias1() {
        setUp("basic.SimpleAlias1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

        Local a = lineNumberToA.get(21);
        Local b = lineNumberToA.get(23);

        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        // a must point to 1 object
        assertTrue(aPointsTo.size()==1);
        // b must point to 1 object
        assertTrue(bPointsTo.size()==1);
        // a and b must point to same set of objects
        assertTrue(aPointsTo.equals(bPointsTo));
    }

    @Test
    public void testBranching1() {
        setUp("basic.Branching1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToInt = getLineNumberToLocalMap(targetMethod, "int", new ArrayList<>());
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

        Local i = lineNumberToInt.get(19);

        Local a = lineNumberToA.get(22);
        Local b = lineNumberToA.get(24);

        Set<Node> iPointsTo = spark.getPointsToSet(i);
        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        // i and a never point to a common object
        assertTrue(Sets.intersection(iPointsTo, bPointsTo).isEmpty());
        // i and b never point to a common object
        assertTrue(Sets.intersection(iPointsTo, aPointsTo).isEmpty());
        // a may point to 2 objects
        assertTrue(aPointsTo.size()==2);
        // b may point to 1 object
        assertTrue(bPointsTo.size()==1);
        // a and b may point to a common object
        assertFalse(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
        // a and b must not point to same set of objects
        assertFalse(aPointsTo.equals(bPointsTo));

    }

    @Test
    public void testParameter1() {
        setUp("basic.Parameter1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.singletonList("benchmark.objects.A"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        List<Local> params = new ArrayList<>();
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", params);

        Local x = params.get(0);
        Local b = lineNumberToA.get(19);

        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        // x must point to 1 object
        assertTrue(xPointsTo.size()==1);
        // b must point to 1 object
        assertTrue(bPointsTo.size()==1);
        // x and b must point to same set of objects
        assertTrue(xPointsTo.equals(bPointsTo));
    }

    @Test
    public void testParameter2() {
        setUp("basic.Parameter2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.singletonList("benchmark.objects.A"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        List<Local> params = new ArrayList<>();
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", params);

        Local x = params.get(0);
        Local b = lineNumberToA.get(21);

        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        // x must point to 1 object
        assertTrue(xPointsTo.size()==1);
        // b must point to 1 object
        assertTrue(bPointsTo.size()==1);
        // x and b must point to same set of objects
        assertTrue(xPointsTo.equals(bPointsTo));
    }

    private Map<Integer, Local> getLineNumberToLocalMap(SootMethod sootMethod, String typeName, List<Local> params) {
        final ImmutableStmtGraph stmtGraph = sootMethod.getBody().getStmtGraph();
        Map<Integer, Local> res = new HashMap<>();
        for (Stmt stmt : stmtGraph) {
            int line = stmt.getPositionInfo().getStmtPosition().getFirstLine();
            List<Value> defs = stmt.getDefs();
            List<Value> uses = stmt.getUses();
            for (Value def : defs) {
                if (def.getType().toString().equals(typeName) && def instanceof Local) {
                    for(Value use: uses){
                        // parameter mapping to local
                        if(use instanceof JParameterRef && use.getType().toString().equals(typeName)){
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
