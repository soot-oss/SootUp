package sootup.java.bytecode.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.util.Utils;
import sootup.java.bytecode.interceptors.typeresolving.types.AugmentIntegerTypes;

@Tag(TestCategories.JAVA_8_CATEGORY)
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
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getInt());
    map.put("l3", sub2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    int count = counter.getCastCount(typing);
    assertEquals(0, count);

    map.replace("l3", super2);
    typing = createTyping(builder.getLocals(), map);
    count = counter.getCastCount(typing);
    assertEquals(1, count);

    map.replace("l2", PrimitiveType.getLong());
    typing = createTyping(builder.getLocals(), map);
    count = counter.getCastCount(typing);
    assertEquals(3, count);

    map.replace("l2", AugmentIntegerTypes.getInteger127());
    typing = createTyping(builder.getLocals(), map);
    count = counter.getCastCount(typing);
    assertEquals(1, count);
  }

  @Test
  public void testAssignStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", Type.createArrayType(super1, 1));
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    int count = counter.getCastCount(typing);
    assertEquals(0, count);

    map.replace("l1", object);
    typing = createTyping(builder.getLocals(), map);
    count = counter.getCastCount(typing);
    assertEquals(5, count);
  }

  @Test
  public void testInvokeStmtWithNewCasts() {
    final Body.BodyBuilder builder = createMethodsBuilder("invokeStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getLong());
    map.put("l3", super2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);

    final Set<Local> locals = builder.getLocals();

    Typing typing = createTyping(locals, map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    assertEquals(3, counter.getCastCount(typing));
    counter.insertCastStmts(typing);
    List<String> actualStmts = Utils.filterJimple(builder.build().toString());
    assertEquals(
        Stream.of(
                "CastCounterDemos l0",
                "Sub2 #l3",
                "int #l2",
                "integer1 #l0",
                "long #l1",
                "unknown $stack4, $stack5, l1, l2, l3",
                "l0 := @this: CastCounterDemos",
                "$stack4 = new Sub1",
                "specialinvoke $stack4.<Sub1: void <init>()>()",
                "l1 = $stack4",
                "#l0 = 1",
                "#l1 = (long) #l0",
                "l2 = #l1",
                "$stack5 = new Sub2",
                "specialinvoke $stack5.<Sub2: void <init>()>()",
                "l3 = $stack5",
                "#l2 = (int) l2",
                "#l3 = (Sub2) l3",
                "virtualinvoke l1.<Super1: void m(int,Sub2)>(#l2, #l3)",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }

  @Test
  public void testAssignStmtWithNewCasts() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");
    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", object);
    map.put("l2", super1);
    map.put("$stack3", sub1);

    Typing typing = createTyping(builder.getLocals(), map);
    CastCounter counter = new CastCounter(builder, function, hierarchy);
    counter.insertCastStmts(typing);
    assertEquals(2, counter.getCastCount());

    final Body body = builder.build();
    List<String> actualStmts = Utils.filterJimple(body.toString());

    assertEquals(
        Stream.of(
                "CastCounterDemos l0",
                "Super1[] #l0, #l1",
                "unknown $stack3, l1, l2",
                "l0 := @this: CastCounterDemos",
                "l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "#l0 = (Super1[]) l1",
                "#l0[0] = $stack3",
                "#l1 = (Super1[]) l1",
                "l2 = #l1[2]",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }
}
