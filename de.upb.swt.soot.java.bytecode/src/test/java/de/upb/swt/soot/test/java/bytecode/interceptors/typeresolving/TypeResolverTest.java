package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.TypeResolver;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    Assert.assertEquals(
        newbody.toString(),
        "{\n"
            + "    CastCounterDemos l0;\n"
            + "    Sub1 l1, $stack4;\n"
            + "    byte l2;\n"
            + "    Sub2 l3, $stack5;\n"
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
            + "    l2 = 1;\n"
            + "\n"
            + "    $stack5 = new Sub2;\n"
            + "\n"
            + "    specialinvoke $stack5.<Sub2: void <init>()>();\n"
            + "\n"
            + "    l3 = $stack5;\n"
            + "\n"
            + "    virtualinvoke l1.<Super1: void m(int,Sub2)>(l2, l3);\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }

  @Test
  public void testAssignStmt() {
    setMethodBody("assignStmt", "void", Collections.emptyList());
    TypeResolver resolver = new TypeResolver(view);
    resolver.resolveBuilder(builder);
    Body newbody = builder.build();
    Assert.assertEquals(
        newbody.toString(),
        "{\n"
            + "    CastCounterDemos l0;\n"
            + "    Super1[] l1;\n"
            + "    Super1 l2;\n"
            + "    Sub1 $stack3;\n"
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
            + "    l1[0] = $stack3;\n"
            + "\n"
            + "    l2 = l1[2];\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }
}
