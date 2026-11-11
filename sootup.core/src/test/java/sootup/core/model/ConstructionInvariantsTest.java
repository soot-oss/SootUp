package sootup.core.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;

/** Illegal IR states rejected at construction, so no validator has to find them later. */
@Tag("Java8")
class ConstructionInvariantsTest {

  private static MethodSignature sig(String name, Type ret, List<Type> params) {
    return SignatureInterner.getMethodSignature(TestUtil.createDummyClassType(), name, params, ret);
  }

  private static MethodSignature sig(String name) {
    return sig(name, VoidType.getInstance(), Collections.emptyList());
  }

  // --- modifiers ---

  @Test
  void fieldModifiers() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            FieldModifier.checkWellFormed(EnumSet.of(FieldModifier.PUBLIC, FieldModifier.PRIVATE)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            FieldModifier.checkWellFormed(EnumSet.of(FieldModifier.FINAL, FieldModifier.VOLATILE)));
    assertDoesNotThrow(
        () ->
            FieldModifier.checkWellFormed(
                EnumSet.of(FieldModifier.PROTECTED, FieldModifier.STATIC, FieldModifier.FINAL)));
  }

  @Test
  void methodModifiers() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            MethodModifier.checkWellFormed(
                EnumSet.of(MethodModifier.PROTECTED, MethodModifier.PRIVATE)));
    for (MethodModifier m :
        EnumSet.of(
            MethodModifier.PRIVATE,
            MethodModifier.STATIC,
            MethodModifier.FINAL,
            MethodModifier.SYNCHRONIZED,
            MethodModifier.NATIVE)) {
      assertThrows(
          IllegalArgumentException.class,
          () -> MethodModifier.checkWellFormed(EnumSet.of(MethodModifier.ABSTRACT, m)),
          m.toString());
    }
    assertDoesNotThrow(
        () ->
            MethodModifier.checkWellFormed(
                EnumSet.of(MethodModifier.PUBLIC, MethodModifier.ABSTRACT)));
  }

  @Test
  void classModifiers() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ClassModifier.checkWellFormed(EnumSet.of(ClassModifier.FINAL, ClassModifier.ABSTRACT)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ClassModifier.checkWellFormed(
                EnumSet.of(ClassModifier.INTERFACE, ClassModifier.FINAL)));
    assertThrows(
        IllegalArgumentException.class,
        () -> ClassModifier.checkWellFormed(EnumSet.of(ClassModifier.ANNOTATION)));
    assertDoesNotThrow(
        () ->
            ClassModifier.checkWellFormed(
                EnumSet.of(
                    ClassModifier.PUBLIC,
                    ClassModifier.INTERFACE,
                    ClassModifier.ABSTRACT,
                    ClassModifier.ANNOTATION)));
  }

  // --- invokes ---

  @Test
  void argCountMustMatchSignature() {
    MethodSignature oneInt =
        sig("m", VoidType.getInstance(), Collections.singletonList(PrimitiveType.getInt()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JStaticInvokeExpr(oneInt, Collections.emptyList()));
    assertDoesNotThrow(
        () ->
            new JStaticInvokeExpr(
                oneInt, Collections.<Immediate>singletonList(IntConstant.getInstance(1))));
  }

  @Test
  void initOnlyViaSpecialInvoke() {
    Local base = TestUtil.createDummyLocalForObject();
    MethodSignature init = sig("<init>");
    assertDoesNotThrow(() -> new JSpecialInvokeExpr(base, init, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JVirtualInvokeExpr(base, init, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JInterfaceInvokeExpr(base, init, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class, () -> new JStaticInvokeExpr(init, Collections.emptyList()));
  }

  @Test
  void clinitNeverViaInstanceInvoke() {
    Local base = TestUtil.createDummyLocalForObject();
    MethodSignature clinit = sig("<clinit>");
    // synthetic models (qilin fake main, solver) call <clinit> statically
    assertDoesNotThrow(() -> new JStaticInvokeExpr(clinit, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JSpecialInvokeExpr(base, clinit, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JVirtualInvokeExpr(base, clinit, Collections.emptyList()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new JInterfaceInvokeExpr(base, clinit, Collections.emptyList()));
  }

  // --- body ---

  private static Body.BodyBuilder bodyWith(MethodSignature ms, Stmt single) {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.addNode(single);
    graph.setStartingStmt(single);
    return Body.builder(graph).setMethodSignature(ms);
  }

  @Test
  void returnKindMustMatchSignature() {
    StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
    MethodSignature voidSig = sig("v");
    MethodSignature intSig = sig("i", PrimitiveType.getInt(), Collections.emptyList());

    assertThrows(
        IllegalStateException.class,
        () -> bodyWith(voidSig, Jimple.newReturnStmt(IntConstant.getInstance(1), pos)).build());
    assertThrows(
        IllegalStateException.class, () -> bodyWith(intSig, Jimple.newReturnVoidStmt(pos)).build());

    assertDoesNotThrow(() -> bodyWith(voidSig, Jimple.newReturnVoidStmt(pos)).build());
    assertDoesNotThrow(
        () -> bodyWith(intSig, Jimple.newReturnStmt(IntConstant.getInstance(1), pos)).build());
  }

  // --- names (JVMS 4.2.2) ---

  @Test
  void illegalNames() {
    for (String bad : new String[] {"", "a.b", "a;b", "a[b", "a/b", "<foo>", "a<b"}) {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              SignatureInterner.getMethodSubSignature(
                  bad, VoidType.getInstance(), Collections.emptyList()),
          bad);
    }
    for (String bad : new String[] {"", "a.b", "a;b", "a[b", "a/b"}) {
      assertThrows(
          IllegalArgumentException.class,
          () -> SignatureInterner.getFieldSubSignature(bad, PrimitiveType.getInt()),
          bad);
    }
    for (String ok :
        new String[] {"<init>", "<clinit>", "lambda$main$0", "access$000", "a b-c", "\u00e4"}) {
      assertDoesNotThrow(
          () ->
              SignatureInterner.getMethodSubSignature(
                  ok, VoidType.getInstance(), Collections.emptyList()),
          ok);
    }
    assertDoesNotThrow(
        () -> SignatureInterner.getFieldSubSignature("a<b>", PrimitiveType.getInt()));
  }
}
