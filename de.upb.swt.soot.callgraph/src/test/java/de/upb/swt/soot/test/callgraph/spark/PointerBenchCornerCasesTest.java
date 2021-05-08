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
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.*;

public class PointerBenchCornerCasesTest {

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
    public void testAccessPath1() {
        setUp("cornerCases.AccessPath1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

        Local a = lineNumberToA.get(21);
        Local b = lineNumberToA.get(22);

        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> bPointsTo = spark.getPointsToSet(b);

        JavaClassType type = identifierFactory.getClassType("benchmark.objects.A");
        SootClass sc = (SootClass) view.getClass(type).get();
        SootField field = sc.getField("f").get();

        Set<Node> aFieldPointsTo = spark.getPointsToSet(a, field);
        Set<Node> bFieldPointsTo = spark.getPointsToSet(b, field);

        // a and b must not point to a common object
        assertTrue(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
        // a.f and b.f must point to same set of objects
        assertTrue(aFieldPointsTo.equals(bFieldPointsTo));
    }

    @Test
    public void testObjectSensitivity1() {
        setUp("cornerCases.ObjectSensitivity1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local b1 = lineNumberToB.get(21);
        Local b2 = lineNumberToB.get(23);

        Local a1 = lineNumberToA.get(25);
        Local a2 = lineNumberToA.get(26);

        Local b3 = lineNumberToB.get(28);
        Local b4 = lineNumberToB.get(29);


        Set<Node> b1PointsTo = spark.getPointsToSet(b1);
        Set<Node> b2PointsTo = spark.getPointsToSet(b2);
        Set<Node> a1PointsTo = spark.getPointsToSet(a1);
        Set<Node> a2PointsTo = spark.getPointsToSet(a2);
        Set<Node> b3PointsTo = spark.getPointsToSet(b3);
        Set<Node> b4PointsTo = spark.getPointsToSet(b4);


        // b2 and b4 must point to  common object
        assertTrue(b4PointsTo.containsAll(b2PointsTo));
        // b2 and a1,a2,b1,b3 must not point to a common object
        assertTrue(Sets.intersection(b2PointsTo, a1PointsTo).isEmpty());
        assertTrue(Sets.intersection(b2PointsTo, a2PointsTo).isEmpty());
        // TODO: spark is object insensitive?
        //assertTrue(Sets.intersection(b2PointsTo, b1PointsTo).isEmpty());
        //assertTrue(Sets.intersection(b2PointsTo, b3PointsTo).isEmpty());
    }

    @Test
    public void testObjectSensitivity2() {
        setUp("cornerCases.ObjectSensitivity2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local b1 = lineNumberToB.get(21);
        Local b2 = lineNumberToB.get(23);

        Local a = lineNumberToA.get(25);

        Local b3 = lineNumberToB.get(27);
        Local b4 = lineNumberToB.get(28);

        Set<Node> b1PointsTo = spark.getPointsToSet(b1);
        Set<Node> b2PointsTo = spark.getPointsToSet(b2);
        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> b3PointsTo = spark.getPointsToSet(b3);
        Set<Node> b4PointsTo = spark.getPointsToSet(b4);


        // b2 and b4 must point to  common object
        assertTrue(b4PointsTo.containsAll(b2PointsTo));
        // b2 and a,b1,b3 must not point to a common object
        assertTrue(Sets.intersection(b2PointsTo, aPointsTo).isEmpty());
    }

    @Test
    public void testFieldSensitivity1() {
        setUp("cornerCases.FieldSensitivity1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local b = lineNumberToB.get(26);
        Local a = lineNumberToA.get(27);
        Local c = lineNumberToA.get(28);
        Local d = lineNumberToB.get(30);

        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> cPointsTo = spark.getPointsToSet(c);
        Set<Node> dPointsTo = spark.getPointsToSet(d);

        // d and b must point to  common object
        assertTrue(dPointsTo.containsAll(bPointsTo));
        // b, a and c must not point to a common object
        assertTrue(Sets.intersection(bPointsTo, Sets.intersection(aPointsTo, cPointsTo)).isEmpty());
    }

    @Test
    public void testFieldSensitivity2() {
        setUp("cornerCases.FieldSensitivity2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "test", mainClassSignature, "void", Collections.emptyList());
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local b = lineNumberToB.get(27);
        Local a = lineNumberToA.get(28);
        Local c = lineNumberToA.get(29);
        Local d = lineNumberToB.get(31);

        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> cPointsTo = spark.getPointsToSet(c);
        Set<Node> dPointsTo = spark.getPointsToSet(d);

        // d and b must point to  common object
        assertTrue(dPointsTo.containsAll(bPointsTo));
        // b, a and c must not point to a common object
        assertTrue(Sets.intersection(bPointsTo, Sets.intersection(aPointsTo, cPointsTo)).isEmpty());
    }

    @Test
    public void testStrongUpdate1() {
        setUp("cornerCases.StrongUpdate1");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local a = lineNumberToA.get(21);
        Local b = lineNumberToA.get(22);
        Local y = lineNumberToB.get(25);
        Local x = lineNumberToB.get(26);

        Set<Node> bPointsTo = spark.getPointsToSet(b);
        Set<Node> aPointsTo = spark.getPointsToSet(a);
        Set<Node> xPointsTo = spark.getPointsToSet(x);
        Set<Node> yPointsTo = spark.getPointsToSet(y);

        // x and y must point to  common object
        assertTrue(xPointsTo.equals(yPointsTo));
    }

    @Test
    public void testStrongUpdate2() {
        setUp("cornerCases.StrongUpdate2");
        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
        SootMethod targetMethod = getTargetMethod(targetMethodSig);
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
        Map<Integer, Local> lineNumberToB = getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

        Local x = lineNumberToB.get(23);
        Local aDotF = lineNumberToB.get(25);
        Local y = lineNumberToB.get(26);

        Set<Node> aDotFPointsTo = spark.getPointsToSet(aDotF);
        Set<Node> yPointsTo = spark.getPointsToSet(y);

        // a.f and y must point to  common object
        assertTrue(yPointsTo.containsAll(aDotFPointsTo));
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
