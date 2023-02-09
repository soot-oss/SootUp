package sootup.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.model.Body;
import sootup.core.util.Utils;

@Category(Java8Test.class)
public class TypeResolverTest extends TypeAssignerTestSuite {

  @Before
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    String className = "CastCounterDemos";
    buildView(baseDir, className);
  }

  @Test
  public void testInvokeStmt() {
    setMethodBody("invokeStmt", "void", Collections.emptyList());
    TypeResolver resolver = new TypeResolver(view);
    resolver.resolveBuilder(builder);
    Body newbody = builder.build();

    System.out.println(Utils.generateJimpleForTest(newbody));

    List<String> actualStmts = Utils.bodyStmtsAsStrings(newbody);
    Assert.assertEquals(
        Stream.of(
                "l0 := @this: CastCounterDemos",
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
    setMethodBody("assignStmt", "void", Collections.emptyList());
    TypeResolver resolver = new TypeResolver(view);
    resolver.resolveBuilder(builder);
    Body newbody = builder.build();
    List<String> actualStmts = Utils.bodyStmtsAsStrings(newbody);

    Assert.assertEquals(
        Stream.of(
                "l0 := @this: CastCounterDemos",
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
