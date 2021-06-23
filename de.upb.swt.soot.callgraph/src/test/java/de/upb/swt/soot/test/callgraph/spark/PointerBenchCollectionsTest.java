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

  /**
   * java code:
   * main() {
   *     A[] array = new A[] {};
   *     A a = new A();
   *     A b = new A();
   *     array[0] = a;
   *     array[1] = b;
   *     A c = array[1];
   * }
   *
   * description:
   * - assignment using different indexes of an Array
   *
   * expected:
   * - a and array do not point to same object
   * - b and c point to same object
   */
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

  /**
   * java code:
   * main() {
   *     ArrayList<A> list = new ArrayList<A>();
   *     A a = new A();
   *     A b = new A();
   *     list.add(a);
   *     list.add(b);
   *     A c = list.get(1);
   * }
   *
   * description:
   * - assignment using different indexes of an ArrayList
   *
   * expected:
   * - a and list do not point to same object
   * - b and c point to same object
   */
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
    Local c = lineNumberToA.get(28);

    Set<Node> listPointsTo = spark.getPointsToSet(list);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    // a and list must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, listPointsTo).isEmpty());
    // c and b must point to a common object
    assertFalse(Sets.intersection(bPointsTo, cPointsTo).isEmpty());
  }

  /**
   * java code:
   * main() {
   *     LinkedList<A> list = new LinkedList<A>();
   *     A a = new A();
   *     A b = new A();
   *     list.add(a);
   *     list.add(b);
   *     A c = list.get(1);
   *   }
   *
   * description:
   * - assignment using different indexes of a LinkedList
   *
   * expected:
   * - a and list do not point to same object
   * - b and c point to same object
   */
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
    // c and b must point to a common object
    assertFalse(Sets.intersection(bPointsTo, cPointsTo).isEmpty());
  }

  /**
   * java code:
   * main() {
   *     HashMap<String, A> map = new HashMap<String, A>();
   *     A a = new A();
   *     A b = new A();
   *     map.put("first", a);
   *     map.put("second", b);
   *     A c = map.get("second");
   * }
   *
   * description:
   * - assignment using values from a HashMap
   *
   * expected:
   * - a and map do not point to same object
   * - b and c point to same object
   */
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

    // a and map must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, mapPointsTo).isEmpty());
    // c and b must point to a common object
    assertFalse(Sets.intersection(bPointsTo, cPointsTo).isEmpty());
  }

  /**
   * java code:
   * main() {
   *     HashSet<A> set = new HashSet<A>();
   *     A a = new A();
   *     A c = null;
   *     A b = new A();
   *     set.add(a);
   *     set.add(b);
   *     for (A i : set) {
   *       c = i;
   *       break;
   *     }
   *     a = null;
   * }
   *
   * description:
   * - assignment using values from HashSet
   *
   * expected:
   * - a,b and set do not point to same object
   */
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

    Set<Node> setPointsTo = spark.getPointsToSet(set);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);

    // a and set must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, setPointsTo).isEmpty());
    // b and set must not point to a common object
    assertTrue(Sets.intersection(bPointsTo, setPointsTo).isEmpty());
  }
}
