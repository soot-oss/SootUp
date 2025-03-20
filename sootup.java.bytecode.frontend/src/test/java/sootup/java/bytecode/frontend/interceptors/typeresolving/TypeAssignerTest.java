package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.Body;
import sootup.core.util.Utils;
import sootup.interceptors.TypeAssigner;

public class TypeAssignerTest extends TypeAssignerTestSuite {

  @BeforeEach
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    String className = "CastCounterDemos";
    buildView(baseDir, className);
  }

  @Test
  public void testInvokeStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("invokeStmt", "void");
    new TypeAssigner().interceptBody(builder, view);

    List<String> actualStmts = Utils.filterJimple(builder.build().toString());

    assertEquals(
        Stream.of(
                "CastCounterDemos this",
                "Sub1 $stack4, l1",
                "Sub2 $stack5, l3",
                "byte l2",
                "this := @this: CastCounterDemos",
                "$stack4 = new Sub1",
                "specialinvoke $stack4.<Sub1: void <init>()>()",
                "l1 = $stack4",
                "l2 = 1",
                "$stack5 = new Sub2",
                "specialinvoke $stack5.<Sub2: void <init>()>()",
                "l3 = $stack5",
                "virtualinvoke l1.<Super1: void m(int,Sub2)>(l2, l3)",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }

  @Test
  public void testAssignStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");
    new TypeAssigner().interceptBody(builder, view);

    List<String> actualStmts = Utils.filterJimple(builder.build().toString());

    assertEquals(
        Stream.of(
                "CastCounterDemos this",
                "Sub1 $stack3",
                "Super1 l2",
                "Super1[] l1",
                "this := @this: CastCounterDemos",
                "l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "l1[0] = $stack3",
                "l2 = l1[2]",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }
}
