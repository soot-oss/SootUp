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
        spark = new Spark.Builder(view, callGraph).build();
        spark.analyze();
    }

    private SootMethod getTargetMethod(MethodSignature targetMethodSig) {
        SootClass mainClass = (SootClass) view.getClass(mainClassSignature).get();
        Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig);
        assertTrue(targetOpt.isPresent());
        return targetOpt.get();
    }

    private SootMethod getTargetMethodFromClass(MethodSignature targetMethodSig, JavaClassType classSig) {
        SootClass mainClass = (SootClass) view.getClass(classSig).get();
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
        assertTrue(aPointsTo.size() == 1);
        // b must point to 1 object
        assertTrue(bPointsTo.size() == 1);
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
        assertTrue(aPointsTo.size() == 2);
        // b may point to 1 object
        assertTrue(bPointsTo.size() == 1);
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
        assertTrue(xPointsTo.size() == 1);
        // b must point to 1 object
        assertTrue(bPointsTo.size() == 1);
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
        assertTrue(xPointsTo.size() == 1);
        // b must point to 1 object
        assertTrue(bPointsTo.size() == 1);
        // x and b must point to same set of objects
        assertTrue(xPointsTo.equals(bPointsTo));
    }

    @Test
    public void testInterprocedural1() {
        setUp("basic.Interprocedural1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local a = lineNumberToA.get(25);
        Local b = lineNumberToA.get(26);
        Local bDotF = lineNumberToB.get(29);
        Local x = lineNumberToB.get(32);
        Local y = lineNumberToB.get(33);

        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> bDotFPointsTo = spark.getPointsToSet(bDotF);
        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> yPointsTo = spark.getPointsToSet(y);

        // b.f and a must not point to a common object
        assertTrue(Sets.intersection(bDotFPointsTo, aPointsTo).isEmpty());
        // b.f and b must not point to a common object
        assertTrue(Sets.intersection(bDotFPointsTo, bPointsTo).isEmpty());
        // x and y must point to same set of objects
        assertTrue(xPointsTo.equals(yPointsTo));

    }

    @Test
    public void testInterprocedural2() {
        setUp("basic.Interprocedural2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());
        Map<Integer, Local> lineNumberToInterprocedural2 = getLineNumberToLocalMap(targetMethod, "basic.Interprocedural2", new ArrayList<>());

        Local a = lineNumberToA.get(27);
        Local b = lineNumberToA.get(28);
        Local bDotF = lineNumberToB.get(31);
        Local m2 = lineNumberToInterprocedural2.get(32);
        Local x = lineNumberToB.get(35);
        Local y = lineNumberToB.get(36);


        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> bDotFPointsTo = spark.getPointsToSet(bDotF);
        Set<Node> m2PointsTo = spark.getPointsToSet(m2);
        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> yPointsTo = spark.getPointsToSet(y);

        // b.f and a must not point to a common object
        assertTrue(Sets.intersection(bDotFPointsTo, aPointsTo).isEmpty());
        // b.f and b must not point to a common object
        assertTrue(Sets.intersection(bDotFPointsTo, bPointsTo).isEmpty());
        // b.f and m2 must not point to a common object
        assertTrue(Sets.intersection(bDotFPointsTo, m2PointsTo).isEmpty());
        // x and y must point to same set of objects
        assertTrue(xPointsTo.equals(yPointsTo));

    }

    @Test
    public void testReturnValue1() {
        setUp("basic.ReturnValue1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

        Local a = lineNumberToA.get(25);
        Local b = lineNumberToA.get(26);


        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        // a and b must point to same set of objects
        assertTrue(aPointsTo.equals(bPointsTo));

    }

    @Test
    public void testReturnValue2() {
        setUp("basic.ReturnValue2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToReturnValue2 = getLineNumberToLocalMap(targetMethod, "basic.ReturnValue2", new ArrayList<>());

        Local a = lineNumberToA.get(27);
        Local rv2 = lineNumberToReturnValue2.get(28);
        Local b = lineNumberToA.get(29);


        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> rv2PointsTo = spark.getPointsToSet(rv2);

        // a and b must point to same set of objects
        assertTrue(aPointsTo.equals(bPointsTo));
        // a and rv2 must not have a common object
        assertTrue(Sets.intersection(aPointsTo, rv2PointsTo).isEmpty());
    }

    @Test
    public void testReturnValue3() {
        setUp("basic.ReturnValue3");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        MethodSignature idMethodSig =
                identifierFactory.getMethodSignature(
                        "id", mainClassSignature, "benchmark.objects.A", Collections.singletonList("benchmark.objects.A"));
        SootMethod idMethod = getTargetMethod(idMethodSig);
        Map<Integer, Local> lineNumberToBInId = getLineNumberToLocalMap(idMethod, "benchmark.objects.B", new ArrayList<>());

        Local a = lineNumberToA.get(28);
        Local b = lineNumberToA.get(29);
        Local x = lineNumberToB.get(30);
        Local y = lineNumberToB.get(31);

        Local yDotF = lineNumberToBInId.get(22);

        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> yPointsTo = spark.getPointsToSet(y);
        Set<Node> yDotFPointsTo = spark.getPointsToSet(yDotF);


        // a, b and y must not have a common object
        assertTrue(Sets.intersection(Sets.intersection(aPointsTo, bPointsTo),
                                Sets.intersection(bPointsTo, yPointsTo)).isEmpty());
        // x and y.f in id() must point to a common object
        assertFalse(Sets.intersection(xPointsTo, yDotFPointsTo).isEmpty());
    }

    @Test
    public void testLoops1() {
        setUp("basic.Loops1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.emptyList());
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToN = getLineNumberToLocalMap(targetMethod, "basic.Loops1$N", new ArrayList<>());
        Map<Integer, Local> lineNumberToInt = getLineNumberToLocalMap(targetMethod, "int", new ArrayList<>());

        Local node = lineNumberToN.get(28);
        Local i = lineNumberToInt.get(30);
        Local o = lineNumberToN.get(36);
        Local p = lineNumberToN.get(37);
        Local q = lineNumberToN.get(38);


        Set<Node> nodePointsTo = spark.getPointsToSet(node);
        Set<Node> iPointsTo = spark.getPointsToSet(i);
        Set<Node> oPointsTo = spark.getPointsToSet(o);
        Set<Node> pPointsTo = spark.getPointsToSet(p);
        Set<Node> qPointsTo = spark.getPointsToSet(q);

        // node and o must not point to a common object
        assertTrue(Sets.intersection(nodePointsTo, oPointsTo).isEmpty());
        // node and p must not point to a common object
        assertTrue(Sets.intersection(nodePointsTo, pPointsTo).isEmpty());
        // node and q must not point to a common object
        assertTrue(Sets.intersection(nodePointsTo, qPointsTo).isEmpty());
        // node and i must not point to a common object
        assertTrue(Sets.intersection(nodePointsTo, iPointsTo).isEmpty());
    }

    @Test
    public void testLoops2() {
        setUp("basic.Loops2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.emptyList());
        SootMethod targetMethod = getTargetMethod(targetMethodSig);

        JavaClassType NClassSignature = identifierFactory.getClassType("basic.Loops2$N");
        MethodSignature NMethodSig =
                identifierFactory.getMethodSignature(
                        "<init>", NClassSignature, "void", Collections.emptyList());
        SootMethod NMethod = getTargetMethodFromClass(NMethodSig, NClassSignature);

        Map<Integer, Local> lineNumberToN = getLineNumberToLocalMap(targetMethod, "basic.Loops2$N", new ArrayList<>());
        Map<Integer, Local> lineNumberToInt = getLineNumberToLocalMap(targetMethod, "int", new ArrayList<>());
        Map<Integer, Local> lineNumberToNinN = getLineNumberToLocalMap(NMethod, "basic.Loops2$N", new ArrayList<>());

        Local nextInN = lineNumberToNinN.get(23);
        Local node = lineNumberToN.get(29);
        Local i = lineNumberToInt.get(31);
        Local o = lineNumberToN.get(37);
        Local p = lineNumberToN.get(38);

        Set<Node> nextInNPointsTo = spark.getPointsToSet(nextInN);
        Set<Node> nodePointsTo = spark.getPointsToSet(node);
        Set<Node> iPointsTo = spark.getPointsToSet(i);
        Set<Node> oPointsTo = spark.getPointsToSet(o);
        Set<Node> pPointsTo = spark.getPointsToSet(p);

        // node and o must not point to same set of objects
        assertFalse(nodePointsTo.equals(oPointsTo));
        // node and p must not point to same set of objects
        assertFalse(nodePointsTo.equals(pPointsTo));
        // node and i must not point to a common object
        assertTrue(Sets.intersection(nodePointsTo, iPointsTo).isEmpty());
        // node.next and o must point to the same set of objects
        assertTrue(oPointsTo.equals(nextInNPointsTo));
    }

    @Test
    public void testRecursion1() {
        setUp("basic.Recursion1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.emptyList());
        SootMethod targetMethod = getTargetMethod(targetMethodSig);


        Map<Integer, Local> lineNumberToN = getLineNumberToLocalMap(targetMethod, "basic.Recursion1$N", new ArrayList<>());

        Local node = lineNumberToN.get(39);
        Local n = lineNumberToN.get(42);
        Local o = lineNumberToN.get(44);
        Local p = lineNumberToN.get(45);
        Local q = lineNumberToN.get(46);

        Set<Node> nodePointsTo = spark.getPointsToSet(node);
        Set<Node> nPointsTo = spark.getPointsToSet(n);
        Set<Node> oPointsTo = spark.getPointsToSet(o);
        Set<Node> pPointsTo = spark.getPointsToSet(p);
        Set<Node> qPointsTo = spark.getPointsToSet(q);

        // node and n must point to same set of objects
        assertTrue(nodePointsTo.equals(nPointsTo));
        // node and o must not point to same set of objects
        assertTrue(Sets.intersection(nodePointsTo, oPointsTo).isEmpty());
        // node and p must not point to same set of objects
        assertTrue(Sets.intersection(nodePointsTo, pPointsTo).isEmpty());
        // node and q must not point to same set of objects
        assertTrue(Sets.intersection(nodePointsTo, qPointsTo).isEmpty());
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
