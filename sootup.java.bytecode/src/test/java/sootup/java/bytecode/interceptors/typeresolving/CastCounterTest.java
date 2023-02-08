package sootup.java.bytecode.interceptors.typeresolving;

import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sootup.core.types.*;

public class CastCounterTest extends TypeAssignerTestSuite {

  AugEvalFunction function;
  BytecodeHierarchy hierarchy;

  ClassType super1 = identifierFactory.getClassType("Super1");
  ClassType super2 = identifierFactory.getClassType("Super2");
  ClassType sub1 = identifierFactory.getClassType("Sub1");
  ClassType sub2 = identifierFactory.getClassType("Sub2");
  ClassType object = identifierFactory.getClassType("java.lang.Object");

  @Before
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    String className = "CastCounterDemos";
    buildView(baseDir, className);
    function = new AugEvalFunction(view);
    hierarchy = new BytecodeHierarchy(view);
  }

  @Test
  public void testInvokeStmt() {
    setMethodBody("invokeStmt", "void", Collections.emptyList());
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getInt());
    map.put("l3", sub2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    int count = counter.getCastCount(typing);
    Assert.assertTrue(count == 0);

    map.replace("l3", super2);
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 1);

    map.replace("l2", PrimitiveType.getLong());
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 3);

    map.replace("l2", PrimitiveType.getInteger127());
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 1);
  }

  @Test
  public void testAssignStmt() {
    setMethodBody("assignStmt", "void", Collections.emptyList());
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", Type.makeArrayType(super1, 1));
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    int count = counter.getCastCount(typing);
    Assert.assertTrue(count == 0);

    map.replace("l1", object);
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 5);
  }

  @Test
  public void testInvokeStmtWithNewCasts() {
    setMethodBody("invokeStmt", "void", Collections.emptyList());
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getLong());
    map.put("l3", super2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    Assert.assertTrue(counter.getCastCount(typing) == 3);
    counter.insertCastStmts(typing);
    Assert.assertEquals(
        builder.build().toString(),
        "{\n"
            + "    unknown l0, l1, l2, l3, $stack4, $stack5;\n"
            + "    integer1 #l0;\n"
            + "    long #l1;\n"
            + "    int #l2;\n"
            + "    Sub2 #l3;\n"
            + "\n"
            + "\n"
            + "    l0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    $stack4 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $stack4.<Sub1: void <init>()>();\n"
            + "\n"
            + "    l1 = $stack4;\n"
            + "\n"
            + "    #l0 = 1;\n"
            + "\n"
            + "    #l1 = (long) #l0;\n"
            + "\n"
            + "    l2 = #l1;\n"
            + "\n"
            + "    $stack5 = new Sub2;\n"
            + "\n"
            + "    specialinvoke $stack5.<Sub2: void <init>()>();\n"
            + "\n"
            + "    l3 = $stack5;\n"
            + "\n"
            + "    #l2 = (int) l2;\n"
            + "\n"
            + "    #l3 = (Sub2) l3;\n"
            + "\n"
            + "    virtualinvoke l1.<Super1: void m(int,Sub2)>(#l2, #l3);\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }

  @Test
  public void testAssignStmtWithNewCasts() {
    setMethodBody("assignStmt", "void", Collections.emptyList());
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", object);
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    counter.insertCastStmts(typing);
    Assert.assertTrue(counter.getCastCount() == 2);
    Assert.assertEquals(
        builder.build().toString(),
        "{\n"
            + "    unknown l0, l1, l2, $stack3;\n"
            + "    Super1[] #l0, #l1;\n"
            + "\n"
            + "\n"
            + "    l0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    l1 = newarray (Super1)[10];\n"
            + "\n"
            + "    $stack3 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $stack3.<Sub1: void <init>()>();\n"
            + "\n"
            + "    #l0 = (Super1[]) l1;\n"
            + "\n"
            + "    #l0[0] = $stack3;\n"
            + "\n"
            + "    #l1 = (Super1[]) l1;\n"
            + "\n"
            + "    l2 = #l1[2];\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }
}
