package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.SlotLocal;
import sootup.core.jimple.common.StackLocal;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.types.PrimitiveType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class LocalSlotIndexTest {

  private final String directory = "src/test/resources/bugfixes/";

  @Test
  public void testSlotIndicesInStaticMethod() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots"))
            .get()
            .getMethod(
                "main",
                Collections.singletonList(
                    view.getIdentifierFactory().getType("java.lang.String[]")))
            .get();

    Body body = method.getBody();
    Map<String, Local> localsByName =
        body.getLocals().stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    // Parameter args has slot index 0 in static method
    Local argsLocal = localsByName.get("args");
    assertNotNull(argsLocal);
    assertEquals(0, assertInstanceOf(SlotLocal.class, argsLocal).getSlotIndex());

    // Disjoint scope locals reusing slots
    Local aLocal = localsByName.get("a");
    assertNotNull(aLocal);
    assertEquals(1, assertInstanceOf(SlotLocal.class, aLocal).getSlotIndex());

    Local bLocal = localsByName.get("b");
    assertNotNull(bLocal);
    assertEquals(2, assertInstanceOf(SlotLocal.class, bLocal).getSlotIndex());

    Local cLocal = localsByName.get("c");
    assertNotNull(cLocal);
    assertEquals(1, assertInstanceOf(SlotLocal.class, cLocal).getSlotIndex());

    Local dLocal = localsByName.get("d");
    assertNotNull(dLocal);
    assertEquals(2, assertInstanceOf(SlotLocal.class, dLocal).getSlotIndex());

    assertTrue(body.getLocals().stream().anyMatch(StackLocal.class::isInstance));
    for (Local local : body.getLocals()) {
      assertTrue(local instanceof SlotLocal || local instanceof StackLocal);
      if (local instanceof StackLocal) {
        assertFalse(local instanceof SlotLocal);
      }
    }
  }

  @Test
  public void testSlotIndicesInInstanceMethod() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots$Foo"))
            .get()
            .getMethod(
                "use",
                Collections.singletonList(
                    view.getIdentifierFactory().getClassType("LocalNamesReusedSlots$Foo")))
            .get();

    Body body = method.getBody();
    Map<String, Local> localsByName =
        body.getLocals().stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    // In instance methods, 'this' occupies slot 0
    Local thisLocal = localsByName.get("this");
    assertNotNull(thisLocal);
    assertEquals(0, assertInstanceOf(SlotLocal.class, thisLocal).getSlotIndex());

    // Formal parameter 'other' occupies slot 1
    Local otherLocal = localsByName.get("other");
    assertNotNull(otherLocal);
    assertEquals(1, assertInstanceOf(SlotLocal.class, otherLocal).getSlotIndex());

    assertTrue(body.getLocals().stream().anyMatch(StackLocal.class::isInstance));
    for (Local local : body.getLocals()) {
      assertTrue(local instanceof SlotLocal || local instanceof StackLocal);
      if (local instanceof StackLocal) {
        assertFalse(local instanceof SlotLocal);
      }
    }
  }

  @Test
  public void testSlotIndicesInSameVarNamesInDifferentScopes() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("SameVarNamesInDifferentScopes"))
            .get()
            .getMethod("foo", Collections.emptyList())
            .get();

    Body body = method.getBody();
    Set<Local> locals = body.getLocals();

    long slotIndexedLocalsCount = locals.stream().filter(SlotLocal.class::isInstance).count();
    assertTrue(slotIndexedLocalsCount > 0, "Expected at least one Local with slot index >= 0");

    Map<String, Local> localsByName =
        locals.stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    Local thisLocal = localsByName.get("this");
    assertNotNull(thisLocal);
    assertEquals(0, assertInstanceOf(SlotLocal.class, thisLocal).getSlotIndex());

    Local candidate = localsByName.get("candidate");
    assertNotNull(candidate);
    assertEquals(1, assertInstanceOf(SlotLocal.class, candidate).getSlotIndex());

    Local candidate1 = localsByName.get("candidate_1");
    assertNotNull(candidate1);
    assertEquals(2, assertInstanceOf(SlotLocal.class, candidate1).getSlotIndex());

    Local theresAnother = localsByName.get("theresAnother");
    assertNotNull(theresAnother);
    assertEquals(1, assertInstanceOf(SlotLocal.class, theresAnother).getSlotIndex());
  }

  /**
   * Verifies slot indices without debug metadata: long and double parameters each occupy two slots,
   * and instance methods reserve slot 0 for this. Also checks the next local's slot and that nested
   * arithmetic creates {@link StackLocal} temporaries.
   */
  @Test
  void wideParametersAndLocalsWithoutDebugInformation(@TempDir Path directory) throws Exception {
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "WideSlots", null, "java/lang/Object", null);
    for (boolean isStatic : List.of(true, false)) {
      int base = isStatic ? 0 : 1;
      var method =
          writer.visitMethod(
              Opcodes.ACC_PUBLIC | (isStatic ? Opcodes.ACC_STATIC : 0),
              isStatic ? "staticMethod" : "instanceMethod",
              "(JDI)I",
              null,
              null);
      method.visitCode();
      method.visitVarInsn(Opcodes.ILOAD, base + 4);
      method.visitVarInsn(Opcodes.ILOAD, base + 4);
      method.visitInsn(Opcodes.IADD);
      // The nested addition requires materializing the first expression into a stack local.
      method.visitInsn(Opcodes.ICONST_1);
      method.visitInsn(Opcodes.IADD);
      method.visitVarInsn(Opcodes.ISTORE, base + 5);
      method.visitVarInsn(Opcodes.ILOAD, base + 5);
      method.visitInsn(Opcodes.IRETURN);
      method.visitMaxs(0, 0);
      method.visitEnd();
    }
    writer.visitEnd();
    Files.write(directory.resolve("WideSlots.class"), writer.toByteArray());
    var view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                directory.toString(), SourceType.Application, Collections.emptyList()));
    var clazz = view.getClass(view.getIdentifierFactory().getClassType("WideSlots")).orElseThrow();
    for (boolean isStatic : List.of(true, false)) {
      int base = isStatic ? 0 : 1;
      Body body =
          clazz
              .getMethod(
                  isStatic ? "staticMethod" : "instanceMethod",
                  List.of(
                      PrimitiveType.getLong(), PrimitiveType.getDouble(), PrimitiveType.getInt()))
              .orElseThrow()
              .getBody();
      var slots = new HashSet<Integer>();
      for (Local local : body.getLocals()) {
        if (local instanceof SlotLocal) {
          slots.add(((SlotLocal) local).getSlotIndex());
        } else {
          assertInstanceOf(StackLocal.class, local);
        }
      }
      var expected = new HashSet<>(List.of(base, base + 2, base + 4, base + 5));
      if (!isStatic) expected.add(0);
      assertEquals(expected, slots);
      assertTrue(body.getLocals().stream().anyMatch(StackLocal.class::isInstance));
      for (var stmt : body.getStmts()) {
        if (stmt instanceof JIdentityStmt) {
          var identity = (JIdentityStmt) stmt;
          int slot = assertInstanceOf(SlotLocal.class, identity.getLeftOp()).getSlotIndex();
          if (identity.getRightOp() instanceof JParameterRef) {
            int parameter = ((JParameterRef) identity.getRightOp()).getIndex();
            assertEquals(base + 2 * parameter, slot);
          } else {
            assertInstanceOf(JThisRef.class, identity.getRightOp());
            assertEquals(0, slot);
          }
        }
      }
    }
  }
}
