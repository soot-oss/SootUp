package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.*;

import categories.Java8Test;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class PointsToSetIntersectionTest extends SparkTestBase {

  @Test
  public void testLocalsIntersect() {
    setUpBasicTest("Test1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "go", mainClassSignature, "void", Collections.emptyList());
    SootMethod targetMethod = getTargetMethod(targetMethodSig);

    Map<Integer, Local> lineNumberToContainer = getLineNumberToLocalMap(targetMethod, "Container", new ArrayList<>());

    Local c1 = lineNumberToContainer.get(4);
    Local c2 = lineNumberToContainer.get(8);
    Local c3 = lineNumberToContainer.get(12);

    Set<Node> c1PointsTo = spark.getPointsToSet(c1);
    Set<Node> c2PointsTo = spark.getPointsToSet(c2);
    Set<Node> c3PointsTo = spark.getPointsToSet(c3);

    assertTrue(Sets.intersection(c1PointsTo, c2PointsTo).isEmpty());
    assertTrue(Sets.intersection(c1PointsTo, c3PointsTo).isEmpty());
    assertFalse(Sets.intersection(c2PointsTo, c3PointsTo).isEmpty());
  }

  @Test
  public void testFieldsIntersect() {
    setUpBasicTest("Test1");
    MethodSignature targetMethodSig =
            identifierFactory.getMethodSignature(
                    "go", mainClassSignature, "void", Collections.emptyList());
    SootMethod targetMethod = getTargetMethod(targetMethodSig);

    Map<Integer, Local> lineNumberToContainer = getLineNumberToLocalMap(targetMethod, "Container", new ArrayList<>());

    Local c1 = lineNumberToContainer.get(4);
    Local c2 = lineNumberToContainer.get(8);
    Local c3 = lineNumberToContainer.get(12);

    JavaClassType containerClassSig = identifierFactory.getClassType("Container");
    SootClass containerSC = (SootClass) view.getClass(containerClassSig).get();
    SootField containerItem = (SootField) containerSC.getField("item").get();

    Set<Node> c1ItemPointsTo = spark.getPointsToSet(c1, containerItem);
    Set<Node> c2ItemPointsTo = spark.getPointsToSet(c2, containerItem);
    Set<Node> c3ItemPointsTo = spark.getPointsToSet(c3, containerItem);

    assertTrue(c1ItemPointsTo.size() == 2);
    assertTrue(c2ItemPointsTo.size() == 2);
    assertTrue(c3ItemPointsTo.size() == 2);

    assertFalse(Sets.intersection(c1ItemPointsTo, c2ItemPointsTo).isEmpty());
    assertFalse(Sets.intersection(c1ItemPointsTo, c3ItemPointsTo).isEmpty());
    assertFalse(Sets.intersection(c2ItemPointsTo, c3ItemPointsTo).isEmpty());
  }

}
