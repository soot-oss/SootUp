package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.Utils;
import sootup.interceptors.typeresolving.AugEvalFunction;
import sootup.interceptors.typeresolving.BytecodeHierarchy;
import sootup.interceptors.typeresolving.CastCounter;
import sootup.interceptors.typeresolving.Typing;
import sootup.interceptors.typeresolving.types.AugmentIntegerTypes;

public class CastCounterTest extends TypeAssignerTestSuite {

  AugEvalFunction function;
  BytecodeHierarchy hierarchy;

  ClassType super1, super2, sub1, sub2, object;

  @BeforeEach
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    String className = "CastCounterDemos";
    buildView(baseDir, className);
    function = new AugEvalFunction(view);
    hierarchy = new BytecodeHierarchy(view);

    super1 = view.getIdentifierFactory().getClassType("Super1");
    super2 = view.getIdentifierFactory().getClassType("Super2");
    sub1 = view.getIdentifierFactory().getClassType("Sub1");
    sub2 = view.getIdentifierFactory().getClassType("Sub2");
    object = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  @Test
  public void testInvokeStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("invokeStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("this", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getInt());
    map.put("l3", sub2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy, typing);
    int count = counter.getCastCount();
    assertEquals(0, count);

    map.replace("l3", super2);
    typing = createTyping(builder.getLocals(), map);
    count = new CastCounter(builder, function, hierarchy, typing).getCastCount();
    assertEquals(1, count);

    map.replace("l2", PrimitiveType.getLong());
    typing = createTyping(builder.getLocals(), map);
    count = new CastCounter(builder, function, hierarchy, typing).getCastCount();
    assertEquals(3, count);

    map.replace("l2", AugmentIntegerTypes.getInteger127());
    typing = createTyping(builder.getLocals(), map);
    count = new CastCounter(builder, function, hierarchy, typing).getCastCount();
    assertEquals(1, count);
  }

  @Test
  public void testAssignStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("this", classType);
    map.put("l1", Type.createArrayType(super1, 1));
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy, typing);
    int count = counter.getCastCount();
    assertEquals(0, count);

    map.replace("l1", object);
    typing = createTyping(builder.getLocals(), map);
    count = new CastCounter(builder, function, hierarchy, typing).getCastCount();
    new CastCounter(builder, function, hierarchy, typing).insertCastStmts();
    assertEquals(2, count);
  }

  @Test
  public void testInvokeStmtWithNewCasts() {
    final Body.BodyBuilder builder = createMethodsBuilder("invokeStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("this", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getLong());
    map.put("l3", super2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);

    final Set<Local> locals = builder.getLocals();

    Typing typing = createTyping(locals, map);
    CastCounter counter = new CastCounter(builder, function, hierarchy, typing);
    assertEquals(3, counter.getCastCount());
    counter.insertCastStmts();
    List<String> actualStmts = Utils.filterJimple(builder.build().toString());

    assertEquals("l2 = (long) 1", actualStmts.get(8));
    Set<String> expected = new HashSet<>();
    expected.add("#l0 = (int) l2");
    expected.add("#l1 = (Sub2) l3");
    Set<String> actual = new HashSet<>();
    actual.add(actualStmts.get(12));
    actual.add(actualStmts.get(13));
    assertEquals(expected, actual);
  }

  @Test
  public void testAssignStmtWithNewCasts() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("this", classType);
    map.put("l1", object);
    map.put("l2", super1);
    map.put("$stack3", sub1);

    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy, typing);
    counter.insertCastStmts();
    assertEquals(2, counter.getCastCount());

    final Body body = builder.build();
    List<String> actualStmts = Utils.filterJimple(body.toString());

    List<String> variant1 =
        Stream.of(
                "CastCounterDemos this",
                "Super1[] #l0, #l1",
                "unknown $stack3, l1, l2",
                "this := @this: CastCounterDemos",
                "l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "#l0 = (Super1[]) l1",
                "#l0[0] = $stack3",
                "#l1 = (Super1[]) l1",
                "l2 = #l1[2]",
                "return")
            .collect(Collectors.toList());
    List<String> variant2 =
        Stream.of(
                "CastCounterDemos this",
                "Super1[] #l0, #l1",
                "unknown $stack3, l1, l2",
                "this := @this: CastCounterDemos",
                "l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "#l1 = (Super1[]) l1",
                "#l1[0] = $stack3",
                "#l0 = (Super1[]) l1",
                "l2 = #l0[2]",
                "return")
            .collect(Collectors.toList());

    assertTrue(actualStmts.equals(variant1) || actualStmts.equals(variant2));
  }
}
