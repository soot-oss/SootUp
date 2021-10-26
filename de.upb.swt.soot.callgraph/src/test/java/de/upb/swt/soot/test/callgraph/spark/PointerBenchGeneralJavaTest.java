package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.*;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.*;
import org.junit.Test;

public class PointerBenchGeneralJavaTest extends SparkTestBase {

  /**
   *
   *
   * <pre>
   * java code:
   * static A a;
   *
   * main() {
   *     a = new A();
   *     A b = a;
   *     A c = a;
   * }
   *
   * description:
   * - Alias to a static variable, allocation site at the static variable site
   *
   * expected:
   * - b and c point to same object
   * </pre>
   */
  @Test
  public void testStaticVariables1() {
    setUpPointerBench("generalJava.StaticVariables1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

    Local a = lineNumberToA.get(22);
    Local b = lineNumberToA.get(23);
    Local c = lineNumberToA.get(24);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);

    assertTrue(aPointsTo.size() == 1);
    // b and c must point to same set of objects
    assertTrue(bPointsTo.equals(cPointsTo));
    // points to sets of b and c are also empty in old spark
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A a = new A();
   *     A b = new A();
   *
   *     G g = new G();
   *     H h = new H();
   *     g.foo(a);
   *     A c = h.foo(b);
   * }
   *
   * description:
   * - Alias from method in interface
   *
   * expected:
   * - b and c point same object
   * - a,g,h do not point to same object
   * </pre>
   */
  @Test
  public void testInterface1() {
    setUpPointerBench("generalJava.Interface1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToG =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.G", new ArrayList<>());
    Map<Integer, Local> lineNumberToH =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.H", new ArrayList<>());

    Local a = lineNumberToA.get(22);
    Local b = lineNumberToA.get(24);
    Local g = lineNumberToG.get(26);
    Local h = lineNumberToH.get(27);
    Local c = lineNumberToA.get(29);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> cPointsTo = spark.getPointsToSet(c);
    Set<Node> gPointsTo = spark.getPointsToSet(g);
    Set<Node> hPointsTo = spark.getPointsToSet(h);

    // b and c must point to a common object
    assertFalse(Sets.intersection(bPointsTo, cPointsTo).isEmpty());
    // a and g must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, gPointsTo).isEmpty());
    // a and h must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, hPointsTo).isEmpty());
    // g and h must not point to a common object
    assertTrue(Sets.intersection(gPointsTo, hPointsTo).isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A a = new A();
   *     A b = new A();
   *
   *     P p = new P(a);
   *     p.alias(b);
   *     A h = p.getA();
   * }
   *
   * description:
   * - Alias from method in super class
   *
   * expected:
   * - b and h point to same object
   * - a and b point to same object
   * - a,p and h do not point to same object
   * </pre>
   */
  @Test
  public void testSuperClasses1() {
    setUpPointerBench("generalJava.SuperClasses1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToP =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.P", new ArrayList<>());

    Local a = lineNumberToA.get(21);
    Local b = lineNumberToA.get(22);
    Local p = lineNumberToP.get(24);
    Local h = lineNumberToA.get(26);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> pPointsTo = spark.getPointsToSet(p);
    Set<Node> hPointsTo = spark.getPointsToSet(h);

    // b and h must point to a common object
    assertFalse(Sets.intersection(bPointsTo, hPointsTo).isEmpty());
    // a and p must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, pPointsTo).isEmpty());
    // a and b must point to a common object
    assertFalse(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
    // p and h must not point to a common object
    assertTrue(Sets.intersection(pPointsTo, hPointsTo).isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * class InnerClass {
   *     private A a;
   *
   *     public InnerClass(A a) {
   *       this.a = a;
   *     }
   *
   *     public void alias(A x) {
   *       this.a = x;
   *     }
   * }
   *
   * test() {
   *     A a = new A();
   *     A b = new A();
   *
   *     InnerClass i = new InnerClass(a);
   *     i.alias(b);
   *     A h = i.a;
   *   }
   *
   * description:
   * - Alias from method in inner class
   *
   * expected:
   * - b and h point to same object
   * - a and b point to same object
   * - i,a,h do do not point to same object
   * </pre>
   */
  @Test
  public void testOuterClasses1() {
    setUpPointerBench("generalJava.OuterClass1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "test", mainClassSignature, "void", Collections.emptyList());
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToInnerClass =
        getLineNumberToLocalMap(
            targetMethod, "generalJava.OuterClass1$InnerClass", new ArrayList<>());

    Local a = lineNumberToA.get(34);
    Local b = lineNumberToA.get(35);
    Local i = lineNumberToInnerClass.get(37);
    Local h = lineNumberToA.get(39);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> iPointsTo = spark.getPointsToSet(i);
    Set<Node> hPointsTo = spark.getPointsToSet(h);

    // b and h must point to a common object
    assertFalse(Sets.intersection(bPointsTo, hPointsTo).isEmpty());
    // i and a must not point to a common object
    assertTrue(Sets.intersection(iPointsTo, aPointsTo).isEmpty());
    // a and b must point to a common object
    assertFalse(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
    // i and h must not point to a common object
    assertTrue(Sets.intersection(iPointsTo, hPointsTo).isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A h = new A();
   *     B a = h.getH();
   *     B b = a;
   * }
   *
   * description:
   * - Direct alias to null
   *
   * expected:
   * - a and b point to same object
   * - b and h do not point to same object
   * </pre>
   */
  @Test
  public void testNull1() {
    setUpPointerBench("generalJava.Null1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

    Local h = lineNumberToA.get(22);
    Local a = lineNumberToB.get(23);
    Local b = lineNumberToB.get(24);

    Set<Node> hPointsTo = spark.getPointsToSet(h);
    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);

    // a and b must point to the same set of objects
    assertTrue(aPointsTo.equals(bPointsTo));
    // b and h must not point to a common object
    assertTrue(Sets.intersection(bPointsTo, hPointsTo).isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   * 		A a = new A();
   * 		A b = a;
   * 		B x = b.h; // a.h is null
   * }
   *
   * description:
   * - Implicit alias to null
   *
   * expected:
   * - a and b point to same object
   * - x do not point to any object
   * </pre>
   */
  @Test
  public void testNull2() {
    setUpPointerBench("generalJava.Null2");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

    Local a = lineNumberToA.get(20);
    Local b = lineNumberToA.get(21);
    Local x = lineNumberToB.get(22);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);
    Set<Node> xPointsTo = spark.getPointsToSet(x);

    // a and b must point to the same set of objects
    assertTrue(aPointsTo.equals(bPointsTo));
    // x must not point to any object
    assertTrue(xPointsTo.isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main(String[] args) {
   *     A a = new A();
   *     A b = new A();
   *
   *     try {
   *       b = a;
   *       throw new RuntimeException();
   *     } catch (RuntimeException e) {
   *
   *     }
   * }
   *
   * description:
   * - Alias in exception
   *
   * expected:
   * - a and b point to same object
   * </pre>
   */
  @Test
  public void testException1() {
    setUpPointerBench("generalJava.Exception1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());

    Local a = lineNumberToA.get(21);
    Local b = lineNumberToA.get(22);

    Set<Node> aPointsTo = spark.getPointsToSet(a);
    Set<Node> bPointsTo = spark.getPointsToSet(b);

    // a and b must point to a common object
    assertFalse(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
  }
}
