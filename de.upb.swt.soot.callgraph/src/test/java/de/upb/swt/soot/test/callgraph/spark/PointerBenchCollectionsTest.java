package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationDotField;
import de.upb.swt.soot.callgraph.spark.pag.nodes.AllocationNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;

public class PointerBenchCollectionsTest extends SparkTestBase {

  @Test
  public void testArray1() {
    setUpPointerBench("collections.Array1");
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
        if (field.getPointsToSet() != null) {
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

  @Ignore
  public void testList1() {
    // TODO: fix stack underrun
    setUpPointerBench("collections.List1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToArrayList =
        getLineNumberToLocalMap(targetMethod, "java.util.ArrayList", new ArrayList<>());

    Local list = lineNumberToArrayList.get(22);
    Local a = lineNumberToA.get(23);
    Local b = lineNumberToA.get(25);

    Set<Node> listPointsTo = spark.getPointsToSet(list);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);

    // a and list must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, listPointsTo).isEmpty());
    // c and b must point to a common object
    assertFalse(Sets.intersection(bPointsTo, aPointsTo).isEmpty());
  }

  @Ignore
  public void testList2() {
    // TODO: fix type mismatch
    setUpPointerBench("collections.List2");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToLinkedList =
        getLineNumberToLocalMap(targetMethod, "java.util.LinkedList", new ArrayList<>());

    Local list = lineNumberToLinkedList.get(22);
    Local a = lineNumberToA.get(23);
    Local b = lineNumberToA.get(25);
    Local c = lineNumberToA.get(28);

    Set<Node> listPointsTo = spark.getPointsToSet(list);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    // a and list must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, listPointsTo).isEmpty());
  }

  @Ignore
  public void testMap1() {
    // TODO: fix stack underrun
    setUpPointerBench("collections.Map1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToHashMap =
        getLineNumberToLocalMap(targetMethod, "java.util.HashMap", new ArrayList<>());

    Local map = lineNumberToHashMap.get(22);
    Local a = lineNumberToA.get(23);
    Local b = lineNumberToA.get(25);
    Local c = lineNumberToA.get(28);

    Set<Node> mapPointsTo = spark.getPointsToSet(map);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    // a and list must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, mapPointsTo).isEmpty());
  }

  @Ignore
  public void testSet1() {
    // TODO: fix Multiple un-equal stacks
    setUpPointerBench("collections.Set1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToHashSet =
        getLineNumberToLocalMap(targetMethod, "java.util.HashSet", new ArrayList<>());

    Local set = lineNumberToHashSet.get(22);
    Local a = lineNumberToA.get(23);
    Local b = lineNumberToA.get(26);
    Local c = lineNumberToA.get(24);

    Set<Node> setPointsTo = spark.getPointsToSet(set);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    // a and list must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, setPointsTo).isEmpty());
  }
}
