package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.SlotLocal;
import sootup.core.jimple.common.StackLocal;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.core.types.UnknownType;
import sootup.interceptors.typeresolving.TypeResolver;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

/**
 * Verifies local kind, original JVM slot index, and Java annotation handling during packing,
 * splitting, type resolution, and name standardization.
 */
class LocalProvenanceTest {
  private static final StmtPositionInfo NO_POS = StmtPositionInfo.getNoStmtPositionInfo();
  private final JavaView view = new JavaView(Collections.emptyList());

  private Body.BodyBuilder body(List<Local> locals, List<Stmt> stmts) {
    var graph = new MutableBlockControlFlowGraph();
    for (int i = 1; i < stmts.size(); i++) {
      graph.putEdge((FallsThroughStmt) stmts.get(i - 1), stmts.get(i));
    }
    graph.setStartingStmt(stmts.get(0));
    return Body.builder(graph)
        .setLocals(new LinkedHashSet<>(locals))
        .setMethodSignature(
            new JavaIdentifierFactory()
                .getMethodSignature(
                    new JavaIdentifierFactory().getClassType("example.Test"),
                    "test",
                    PrimitiveType.getInt(),
                    List.of()));
  }

  private Body.BodyBuilder disjoint(Local first, Local second) {
    return body(
        List.of(first, second),
        List.of(
            Jimple.newAssignStmt(first, IntConstant.getInstance(1), NO_POS),
            Jimple.newAssignStmt(
                first, Jimple.newAddExpr(first, IntConstant.getInstance(1)), NO_POS),
            Jimple.newAssignStmt(second, IntConstant.getInstance(2), NO_POS),
            Jimple.newReturnStmt(second, NO_POS)));
  }

  /** Supplies a parameter color so packing checks do not depend on allocating a new color. */
  private Body.BodyBuilder disjointWithParameter(Local first, Local second) {
    Local parameter = Jimple.newLocal("parameter", PrimitiveType.getInt());
    var builder =
        body(
            List.of(first, second, parameter),
            List.of(
                Jimple.newIdentityStmt(
                    parameter, Jimple.newParameterRef(PrimitiveType.getInt(), 0), NO_POS),
                Jimple.newAssignStmt(
                    first, Jimple.newAddExpr(parameter, IntConstant.getInstance(1)), NO_POS),
                Jimple.newAssignStmt(
                    second, Jimple.newAddExpr(first, IntConstant.getInstance(1)), NO_POS),
                Jimple.newReturnStmt(second, NO_POS)));
    var factory = new JavaIdentifierFactory();
    return builder.setMethodSignature(
        factory.getMethodSignature(
            factory.getClassType("example.Test"),
            "test",
            PrimitiveType.getInt(),
            List.of(PrimitiveType.getInt())));
  }

  /**
   * Packing locals with disjoint lifetimes preserves the representative's implementation, slot
   * index, and annotation collection.
   */
  @Test
  void packsNonInterferingLocalsAndPreservesRepresentativeState() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    List<Function<String, Local>> factories =
        List.of(
            name -> Jimple.newLocal(name, PrimitiveType.getInt()),
            name -> Jimple.newStackLocal(name, PrimitiveType.getInt()),
            name -> Jimple.newSlotLocal(name, PrimitiveType.getInt(), 3),
            name -> JavaJimple.newLocal(name, PrimitiveType.getInt(), annotations),
            name -> JavaJimple.newStackLocal(name, PrimitiveType.getInt(), annotations),
            name -> JavaJimple.newSlotLocal(name, PrimitiveType.getInt(), 3, annotations));
    for (var factory : factories) {
      Local first = factory.apply("first");
      var builder = disjointWithParameter(first, factory.apply("second"));
      new LocalPacker().interceptBody(builder, view);
      assertEquals(1, builder.getLocals().size());
      Local packed = builder.getLocals().iterator().next();
      assertSame(first.getClass(), packed.getClass());
      if (packed instanceof JavaLocal) {
        assertSame(annotations, ((JavaLocal) packed).getAnnotations());
      }
      if (packed instanceof SlotLocal) {
        assertEquals(3, ((SlotLocal) packed).getSlotIndex());
      }
      assertEquals(List.of(packed), builder.getStmts().get(3).getUses());
    }
  }

  /**
   * Locals with disjoint lifetimes merge across kinds and slots, retaining the first
   * representative's kind and slot index while rewriting all definitions and uses.
   */
  @Test
  void packsAcrossKindsAndDifferentSlots() {
    Local generic = Jimple.newLocal("generic", PrimitiveType.getInt());
    Local stack = Jimple.newStackLocal("stack", PrimitiveType.getInt());
    Local slot0 = Jimple.newSlotLocal("slot0", PrimitiveType.getInt(), 0);
    Local slot1 = Jimple.newSlotLocal("slot1", PrimitiveType.getInt(), 1);
    List<Local> locals = List.of(generic, stack, slot0, slot1);
    for (int i = 0; i < locals.size(); i++) {
      for (int j = 0; j < locals.size(); j++) {
        if (i == j) {
          continue;
        }
        var builder = disjointWithParameter(locals.get(i), locals.get(j));
        new LocalPacker().interceptBody(builder, view);
        assertEquals(1, builder.getLocals().size());
        Local packed = builder.getLocals().iterator().next();
        assertSame(locals.get(i).getClass(), packed.getClass());
        if (packed instanceof SlotLocal) {
          assertEquals(
              ((SlotLocal) locals.get(i)).getSlotIndex(), ((SlotLocal) packed).getSlotIndex());
        }
        for (Stmt stmt : builder.getStmts()) {
          stmt.getDef().ifPresent(def -> assertSame(packed, def));
          stmt.getUses().stream()
              .filter(Local.class::isInstance)
              .forEach(use -> assertSame(packed, use));
        }
      }
    }
  }

  /**
   * Addition operands stay distinct across all kind and slot combinations; the result reuses an
   * operand's local once the operands are dead.
   */
  @Test
  void keepsInterferingLocalsSeparateAcrossKindsAndSlots() {
    List<Function<String, Local>> factories =
        List.of(
            name -> Jimple.newLocal(name, PrimitiveType.getInt()),
            name -> Jimple.newStackLocal(name, PrimitiveType.getInt()),
            name -> Jimple.newSlotLocal(name, PrimitiveType.getInt(), 3),
            name -> Jimple.newSlotLocal(name, PrimitiveType.getInt(), 4));
    for (var firstFactory : factories) {
      for (var secondFactory : factories) {
        Local first = firstFactory.apply("first");
        Local second = secondFactory.apply("second");
        Local result = Jimple.newStackLocal("result", PrimitiveType.getInt());
        var builder =
            body(
                List.of(first, second, result),
                List.of(
                    Jimple.newAssignStmt(first, IntConstant.getInstance(1), NO_POS),
                    Jimple.newAssignStmt(second, IntConstant.getInstance(2), NO_POS),
                    Jimple.newAssignStmt(result, Jimple.newAddExpr(first, second), NO_POS),
                    Jimple.newReturnStmt(result, NO_POS)));
        new LocalPacker().interceptBody(builder, view);
        assertEquals(2, builder.getLocals().size());
        var sum = (JAssignStmt) builder.getStmts().get(2);
        assertEquals(2, sum.getUses().stream().filter(Local.class::isInstance).distinct().count());
      }
    }
  }

  /**
   * Splitting repeated assignments and resolving unknown types preserves each Java stack or slot
   * local's kind, original slot index, and annotation collection.
   */
  @Test
  void splitterAndTypeResolverPreserveJavaProvenance() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    List<JavaLocal> originals =
        List.of(
            JavaJimple.newSlotLocal("slot", UnknownType.getInstance(), 4, annotations),
            JavaJimple.newStackLocal("stack", UnknownType.getInstance(), annotations));
    for (JavaLocal original : originals) {
      var builder = disjoint(original, original);
      new LocalSplitter().interceptBody(builder, view);
      assertEquals(3, builder.getLocals().size());
      assertTrue(new TypeResolver(view).resolve(builder));
      for (Local local : builder.getLocals()) {
        assertSame(original.getClass(), local.getClass());
        assertNotEquals(UnknownType.getInstance(), local.getType());
        assertSame(annotations, ((JavaLocal) local).getAnnotations());
        if (local instanceof SlotLocal) {
          assertEquals(4, ((SlotLocal) local).getSlotIndex());
        }
      }
    }
  }

  /**
   * Name standardization renames Java stack and slot locals while preserving their kinds, slot
   * indices, and annotation collection.
   */
  @Test
  void nameStandardizerPreservesJavaProvenance() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    Local slot = JavaJimple.newSlotLocal("slot", PrimitiveType.getInt(), 4, annotations);
    Local stack = JavaJimple.newStackLocal("stack", PrimitiveType.getInt(), annotations);
    var builder = disjoint(slot, stack);
    new LocalNameStandardizer().interceptBody(builder, view);
    assertEquals(2, builder.getLocals().size());
    assertEquals(1, builder.getLocals().stream().filter(StackLocal.class::isInstance).count());
    SlotLocal renamed =
        (SlotLocal)
            builder.getLocals().stream()
                .filter(SlotLocal.class::isInstance)
                .findFirst()
                .orElseThrow();
    assertEquals(4, renamed.getSlotIndex());
    for (Local local : builder.getLocals()) {
      assertSame(annotations, ((JavaLocal) local).getAnnotations());
      assertFalse(List.of("slot", "stack").contains(local.getName()));
    }
  }
}
