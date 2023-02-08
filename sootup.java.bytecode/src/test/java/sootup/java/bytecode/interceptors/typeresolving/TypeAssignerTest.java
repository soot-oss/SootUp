package sootup.java.bytecode.interceptors.typeresolving;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sootup.java.bytecode.interceptors.TypeAssigner;

public class TypeAssignerTest extends TypeAssignerTestSuite {

  @Before
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    String className = "CastCounterDemos";
    buildView(baseDir, className);
  }

  @Test
  public void testInvokeStmt() {
    setMethodBody("invokeStmt", "void", Collections.emptyList());
    TypeAssigner assigner = new TypeAssigner();
    assigner.interceptBody(builder, this.view);

    Assert.assertEquals(
        "{\n"
            + "    CastCounterDemos r0;\n"
            + "    Sub1 $r1, r2;\n"
            + "    Sub2 $r3, r4;\n"
            + "    byte b0;\n"
            + "\n"
            + "\n"
            + "    r0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    $r1 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $r1.<Sub1: void <init>()>();\n"
            + "\n"
            + "    r2 = $r1;\n"
            + "\n"
            + "    b0 = 1;\n"
            + "\n"
            + "    $r3 = new Sub2;\n"
            + "\n"
            + "    specialinvoke $r3.<Sub2: void <init>()>();\n"
            + "\n"
            + "    r4 = $r3;\n"
            + "\n"
            + "    virtualinvoke r2.<Super1: void m(int,Sub2)>(b0, r4);\n"
            + "\n"
            + "    return;\n"
            + "}\n",
        builder.build().toString());
  }

  @Test
  public void testAssignStmt() {
    setMethodBody("assignStmt", "void", Collections.emptyList());
    TypeAssigner assigner = new TypeAssigner();
    assigner.interceptBody(builder, this.view);
    Assert.assertEquals(
        "{\n"
            + "    CastCounterDemos r0;\n"
            + "    Sub1 $r1;\n"
            + "    Super1 r2;\n"
            + "    Super1[] r3;\n"
            + "\n"
            + "\n"
            + "    r0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    r3 = newarray (Super1)[10];\n"
            + "\n"
            + "    $r1 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $r1.<Sub1: void <init>()>();\n"
            + "\n"
            + "    r3[0] = $r1;\n"
            + "\n"
            + "    r2 = r3[2];\n"
            + "\n"
            + "    return;\n"
            + "}\n",
        builder.build().toString());
  }
}
