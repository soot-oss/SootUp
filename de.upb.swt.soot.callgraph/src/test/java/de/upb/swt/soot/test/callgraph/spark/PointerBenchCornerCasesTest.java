package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.*;

import com.google.common.collect.Sets;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;

public class PointerBenchCornerCasesTest extends SparkTestBase {

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A a = new A();
   *     A b = new A();
   *     a.f = b.f;
   * }
   *
   * description:
   * - field assignment
   *
   * expected:
   * - a and b do not point to same object
   * - a.f and b.f point to same object
   * </pre>
   */
  @Test
  public void testAccessPath1() {
    setUpPointerBench("cornerCases.AccessPath1");
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

    JavaClassType type = identifierFactory.getClassType("benchmark.objects.A");
    SootClass sc = (SootClass) view.getClass(type).get();
    SootField field = (SootField) sc.getField("f").get();

    Set<Node> aFieldPointsTo = spark.getPointsToSet(a, field);
    Set<Node> bFieldPointsTo = spark.getPointsToSet(b, field);

    // a and b must not point to a common object
    assertTrue(Sets.intersection(aPointsTo, bPointsTo).isEmpty());
    // a.f and b.f must point to same set of objects
    assertTrue(aFieldPointsTo.equals(bFieldPointsTo));
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     B b1 = new B();
   *     B b2 = new B();
   *
   *     A a1 = new A(b1);
   *     A a2 = new A(b2);
   *
   *     B b3 = a1.getF();
   *     B b4 = a2.getF();
   * }
   *
   * description:
   * -  field of objects are assigned via their constructors, and accessed via getter method
   *
   * expected:
   * - b2 and b4 point to same object
   * - b2 and a1,a2 do not point to same object
   * </pre>
   */
  @Test
  public void testObjectSensitivity1() {
    setUpPointerBench("cornerCases.ObjectSensitivity1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

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
    // assertTrue(Sets.intersection(b2PointsTo, b1PointsTo).isEmpty());
    // assertTrue(Sets.intersection(b2PointsTo, b3PointsTo).isEmpty());
  }

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     B b1 = new B();
   *     B b2 = new B();
   *
   *     A a = new A();
   *
   *     B b3 = a.id(b1);
   *     B b4 = a.id(b2);
   * }
   *
   * description:
   * - assignment of return value of an id method
   *
   * expected:
   * - b2 and b4 point to same object
   * - b2 and a do not point to same object
   * </pre>
   */
  @Test
  public void testObjectSensitivity2() {
    setUpPointerBench("cornerCases.ObjectSensitivity2");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

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

  /**
   *
   *
   * <pre>
   * java code:
   * main(String[] args) {
   *     B b = new B();
   *     A a = new A(b);
   *     A c = new A();
   *     assign(a, c);
   *     B d = c.f;
   * }
   *
   * static assign(A x, A y) {
   *     y.f = x.f;
   * }
   *
   * description:
   * - field assignment inside static method
   *
   * expected:
   * - d and b point to same object
   * - b, a and c do not point to same object
   * </pre>
   */
  @Test
  public void testFieldSensitivity1() {
    setUpPointerBench("cornerCases.FieldSensitivity1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

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

  /**
   *
   *
   * <pre>
   * java code:
   * test() {
   *     B b = new B();
   *     A a = new A(b);
   *     A c = new A();
   *     assign(a, c);
   *     B d = c.f;
   * }
   *
   * assign(A x, A y) {
   *     y.f = x.f;
   * }
   *
   * description:
   * - field assignment inside instance method
   *
   * expected:
   * - d and b point to same object
   * - b, a and c do not point to same object
   * </pre>
   */
  @Test
  public void testFieldSensitivity2() {
    setUpPointerBench("cornerCases.FieldSensitivity2");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "test", mainClassSignature, "void", Collections.emptyList());
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

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

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A a = new A();
   *     A b = a;
   *
   *     a.f = new B();
   *     B y = a.f;
   *     B x = b.f;
   * }
   *
   * description:
   * - Indirect alias of a.f and b.f through alias of a and b
   *
   * expected:
   * - x and y must point to same object
   * </pre>
   */
  @Test
  public void testStrongUpdate1() {
    setUpPointerBench("cornerCases.StrongUpdate1");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

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

  /**
   *
   *
   * <pre>
   * java code:
   * main() {
   *     A a = new A();
   *     A b = a;
   *     B x = b.f;
   *     a.f = new B();
   *     B y = b.f;
   * }
   *
   * description:
   * - Indirect alias of a.f and b.f through alias of a and b
   *
   * expected:
   * - a.f and y must point to same
   * </pre>
   */
  @Test
  public void testStrongUpdate2() {
    setUpPointerBench("cornerCases.StrongUpdate2");
    MethodSignature targetMethodSig =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));
    SootMethod targetMethod = getTargetMethod(targetMethodSig);
    Map<Integer, Local> lineNumberToA =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.A", new ArrayList<>());
    Map<Integer, Local> lineNumberToB =
        getLineNumberToLocalMap(targetMethod, "benchmark.objects.B", new ArrayList<>());

    Local x = lineNumberToB.get(23);
    Local aDotF = lineNumberToB.get(25);
    Local y = lineNumberToB.get(26);

    Set<Node> aDotFPointsTo = spark.getPointsToSet(aDotF);
    Set<Node> yPointsTo = spark.getPointsToSet(y);

    // a.f and y must point to  common object
    assertTrue(yPointsTo.containsAll(aDotFPointsTo));
  }
}
