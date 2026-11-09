package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.Utils;
import sootup.interceptors.InvokeDynamicDesugarer;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/** Sources in resources/interceptors: IndyConcat (javac 11), IndyRecord (javac --release 17). */
public class InvokeDynamicDesugarerTest {

  private final JavaView view =
      new JavaView(
          new JavaClassPathAnalysisInputLocation(
              "src/test/resources/interceptors/", SourceType.Library, Collections.emptyList()));

  private Body desugared(MethodSignature method) {
    Body body = view.getMethod(method).get().getBody();
    assertTrue(new InvokeDynamicDesugarer().appliesTo(body.getStmts()));
    Body.BodyBuilder builder = Body.builder(body, Collections.emptySet());
    new InvokeDynamicDesugarer().interceptBody(builder, view);
    Body result = builder.build();
    assertFalse(
        result.getStmts().stream()
            .anyMatch(
                s ->
                    s.isInvokableStmt()
                        && s.asInvokableStmt().getInvokeExpr().orElse(null)
                            instanceof JDynamicInvokeExpr),
        result::toString);
    return result;
  }

  private MethodSignature sig(String cls, String name, String ret, String... params) {
    return view.getIdentifierFactory().getMethodSignature(cls, name, ret, List.of(params));
  }

  @Test
  public void stringConcatenationBecomesStringBuilderChain() {
    // "a" + o + i + s + cs + "b": objects via String.valueOf semantics, arrays as Object
    assertEquals(
        List.of(
            "char[] cs",
            "int i",
            "java.lang.Object o",
            "java.lang.String $stack4, r1, s",
            "java.lang.StringBuilder r0",
            "o := @parameter0: java.lang.Object",
            "i := @parameter1: int",
            "s := @parameter2: java.lang.String",
            "cs := @parameter3: char[]",
            "r0 = new java.lang.StringBuilder",
            "specialinvoke r0.<java.lang.StringBuilder: void <init>()>()",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"a\")",
            "if o != null goto label1",
            "r1 = \"null\"",
            "goto label2",
            "label1:",
            "r1 = virtualinvoke o.<java.lang.Object: java.lang.String toString()>()",
            "label2:",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(r1)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(int)>(i)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(s)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>(cs)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"b\")",
            "$stack4 = virtualinvoke r0.<java.lang.StringBuilder: java.lang.String toString()>()",
            "return $stack4"),
        Utils.filterJimple(
            desugared(
                    sig(
                        "IndyConcat",
                        "concat",
                        "java.lang.String",
                        "java.lang.Object",
                        "int",
                        "java.lang.String",
                        "char[]"))
                .toString()));
  }

  @Test
  public void recordToString() {
    // IndyRecord[a=..., b=..., c=..., d=...]
    assertEquals(
        List.of(
            "IndyRecord this",
            "double d0",
            "int i0",
            "java.lang.Object r1",
            "java.lang.String $stack1, r2",
            "java.lang.StringBuilder r0",
            "long l0",
            "this := @this: IndyRecord",
            "r0 = new java.lang.StringBuilder",
            "specialinvoke r0.<java.lang.StringBuilder: void <init>()>()",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"IndyRecord[a=\")",
            "r1 = this.<IndyRecord: java.lang.Object a>",
            "if r1 != null goto label1",
            "r2 = \"null\"",
            "goto label2",
            "label1:",
            "r2 = virtualinvoke r1.<java.lang.Object: java.lang.String toString()>()",
            "label2:",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(r2)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\", b=\")",
            "i0 = this.<IndyRecord: int b>",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(int)>(i0)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\", c=\")",
            "d0 = this.<IndyRecord: double c>",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(double)>(d0)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\", d=\")",
            "l0 = this.<IndyRecord: long d>",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(long)>(l0)",
            "virtualinvoke r0.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>(\"]\")",
            "$stack1 = virtualinvoke r0.<java.lang.StringBuilder: java.lang.String toString()>()",
            "return $stack1"),
        Utils.filterJimple(
            desugared(sig("IndyRecord", "toString", "java.lang.String")).toString()));
  }

  @Test
  public void recordHashCode() {
    // h = 31 * h + hash(component), Objects.hashCode for objects
    assertEquals(
        List.of(
            "IndyRecord this",
            "double d0",
            "int $stack1, i0, i1, i2, i3, i4, i5, i6, i7, i8, i9",
            "java.lang.Object r0",
            "long l0",
            "this := @this: IndyRecord",
            "i0 = 0",
            "r0 = this.<IndyRecord: java.lang.Object a>",
            "i1 = 0",
            "if r0 == null goto label1",
            "i1 = virtualinvoke r0.<java.lang.Object: int hashCode()>()",
            "label1:",
            "i2 = i0 * 31",
            "i0 = i2 + i1",
            "i3 = this.<IndyRecord: int b>",
            "i4 = staticinvoke <java.lang.Integer: int hashCode(int)>(i3)",
            "i5 = i0 * 31",
            "i0 = i5 + i4",
            "d0 = this.<IndyRecord: double c>",
            "i6 = staticinvoke <java.lang.Double: int hashCode(double)>(d0)",
            "i7 = i0 * 31",
            "i0 = i7 + i6",
            "l0 = this.<IndyRecord: long d>",
            "i8 = staticinvoke <java.lang.Long: int hashCode(long)>(l0)",
            "i9 = i0 * 31",
            "i0 = i9 + i8",
            "$stack1 = i0",
            "return $stack1"),
        Utils.filterJimple(desugared(sig("IndyRecord", "hashCode", "int")).toString()));
  }

  @Test
  public void recordEquals() {
    // this == o, else instanceof and components last to first
    assertEquals(
        List.of(
            "IndyRecord r0, this",
            "boolean $stack2, z0, z1, z2",
            "double d0, d1",
            "int i0, i1, i2, i3",
            "java.lang.Object o, r1, r2",
            "long l0, l1",
            "this := @this: IndyRecord",
            "o := @parameter0: java.lang.Object",
            "z0 = 0",
            "if this == o goto label1",
            "z1 = o instanceof IndyRecord",
            "if z1 == 0 goto label2",
            "r0 = (IndyRecord) o",
            "l0 = this.<IndyRecord: long d>",
            "l1 = r0.<IndyRecord: long d>",
            "i0 = l0 cmp l1",
            "if i0 != 0 goto label2",
            "d0 = this.<IndyRecord: double c>",
            "d1 = r0.<IndyRecord: double c>",
            "i1 = staticinvoke <java.lang.Double: int compare(double,double)>(d0, d1)",
            "if i1 != 0 goto label2",
            "i2 = this.<IndyRecord: int b>",
            "i3 = r0.<IndyRecord: int b>",
            "if i2 != i3 goto label2",
            "r1 = this.<IndyRecord: java.lang.Object a>",
            "r2 = r0.<IndyRecord: java.lang.Object a>",
            "if r1 == r2 goto label1",
            "if r1 == null goto label2",
            "z2 = virtualinvoke r1.<java.lang.Object: boolean equals(java.lang.Object)>(r2)",
            "if z2 == 0 goto label2",
            "label1:",
            "z0 = 1",
            "label2:",
            "$stack2 = z0",
            "return $stack2"),
        Utils.filterJimple(
            desugared(sig("IndyRecord", "equals", "boolean", "java.lang.Object")).toString()));
  }

  @Test
  public void frontendInterceptorDesugarsBodiesOfTheView() {
    JavaView desugaringView =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                "src/test/resources/interceptors/",
                SourceType.Library,
                List.of(new InvokeDynamicDesugarer())));
    Body body =
        desugaringView
            .getMethod(sig("IndyRecord", "equals", "boolean", "java.lang.Object"))
            .get()
            .getBody();
    assertFalse(new InvokeDynamicDesugarer().appliesTo(body.getStmts()));
  }
}
