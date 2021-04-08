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
    private MethodSignature mainMethodSignature;
    private View view;
    private Spark spark;
    private SootMethod targetMethod;

    @Before
    public void setUp() {
        String className = "basic.Branching1";
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
        mainMethodSignature =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

        SootClass mainClass = (SootClass) view.getClass(mainClassSignature).get();
        Optional<SootMethod> mainMethod = mainClass.getMethod(mainMethodSignature);
        assertTrue(mainMethodSignature + " not found in classloader", mainMethod.isPresent());

        final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
        CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
        CallGraph callGraph = algorithm.initialize(Collections.singletonList(mainMethodSignature));
        spark = new Spark(view, callGraph);
        spark.analyze();

        MethodSignature targetMethodSig =
                identifierFactory.getMethodSignature(
                        "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

        Optional<SootMethod> targetOpt = mainClass.getMethod(targetMethodSig);
        assertTrue(targetOpt.isPresent());
        targetMethod = targetOpt.get();
    }

    @Test
    public void testBranching1() {
        Map<Integer, Local> lineNumberToInt = getLineNumberToLocalMap(targetMethod, "int");
        Map<Integer, Local> lineNumberToA = getLineNumberToLocalMap(targetMethod, "benchmark.objects.A");

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

    private Map<Integer, Local> getLineNumberToLocalMap(SootMethod sootMethod, String typeName) {
        final ImmutableStmtGraph stmtGraph = sootMethod.getBody().getStmtGraph();
        Map<Integer, Local> res = new HashMap<>();
        for (Stmt stmt : stmtGraph) {
            int line = stmt.getPositionInfo().getStmtPosition().getFirstLine();
            List<Value> defs = stmt.getDefs();
            for (Value def : defs) {
                if (def.getType().toString().equals(typeName) && def instanceof Local) {
                    res.put(line, (Local) def);
                }
            }
        }
        return res;
    }
}
