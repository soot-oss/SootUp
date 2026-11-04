package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.Body;
import sootup.interceptors.LocalSplitter;
import sootup.java.core.views.JavaView;

/**
 * The handlers of a dex try block are tried in order, so a handler behind one that catches {@code
 * Throwable} never runs for that range. dx emits such code for try-with-resources: the catch-all of
 * its {@code finally} behind its {@code catch (Throwable)}. The handler's code reads registers that
 * no definition reaches, and no edge of the control flow graph leads to it.
 */
public class DexShadowedHandlerTest {

  @TempDir static Path tempDir;

  /** Builds a dex with one static {@code void run()} and loads it with the given interceptors */
  private static Body convert(
      String name,
      int registerCount,
      Consumer<MethodImplementationBuilder> instructions,
      List<BodyInterceptor> interceptors) {
    String descriptor = "Ldex/" + name + ";";
    MethodImplementationBuilder builder = new MethodImplementationBuilder(registerCount);
    instructions.accept(builder);
    ImmutableMethod method =
        new ImmutableMethod(
            descriptor,
            "run",
            ImmutableList.of(),
            "V",
            AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
            null,
            null,
            builder.getMethodImplementation());
    ImmutableClassDef classDef =
        new ImmutableClassDef(
            descriptor,
            AccessFlags.PUBLIC.getValue(),
            "Ljava/lang/Object;",
            null,
            null,
            null,
            null,
            null,
            List.of(method),
            null);
    Path dex = tempDir.resolve(name + ".dex");
    try {
      DexPool.writeTo(dex.toString(), new ImmutableDexFile(Opcodes.forApi(15), List.of(classDef)));
    } catch (IOException e) {
      throw new IllegalStateException("could not write " + dex, e);
    }
    JavaView view =
        new JavaView(
            List.of(
                new ApkAnalysisInputLocation(dex, new AndroidVersionInfo(dex, ""), interceptors)));
    return view.getMethod(
            view.getIdentifierFactory()
                .getMethodSignature(
                    view.getIdentifierFactory().getClassType("dex." + name),
                    "run",
                    "void",
                    List.of()))
        .get()
        .getBody();
  }

  /** {@code invoke-static {vReg...}, dex.App.name(types)} */
  private static BuilderInstruction35c call(String name, List<String> types, int... regs) {
    return new BuilderInstruction35c(
        Opcode.INVOKE_STATIC,
        regs.length,
        regs.length > 0 ? regs[0] : 0,
        regs.length > 1 ? regs[1] : 0,
        0,
        0,
        0,
        new ImmutableMethodReference("Ldex/App;", name, types, "V"));
  }

  /**
   *
   *
   * <pre>
   *   const/4 v2, 0
   * try:
   *   invoke-static {}, work()
   * end:
   *   const/4 v2, 1
   *   invoke-static {v2}, use(int)
   *   return-void
   * throwable:                     catch Throwable, try..end
   *   move-exception v1
   *   invoke-static {v1}, handled(Throwable)
   *   return-void
   * shadowed:                      catch-all, try..end - never runs
   *   move-exception v1
   *   invoke-static {v2}, dead(Object)    no definition of v2 reaches it
   *   throw v1
   * </pre>
   */
  private static void shadowedCatchAll(MethodImplementationBuilder b) {
    b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 2, 0));
    b.addLabel("try");
    b.addInstruction(call("work", List.of()));
    b.addLabel("end");
    b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 2, 1));
    b.addInstruction(call("use", List.of("I"), 2));
    b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
    b.addLabel("throwable");
    b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, 1));
    b.addInstruction(call("handled", List.of("Ljava/lang/Throwable;"), 1));
    b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
    b.addLabel("shadowed");
    b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, 1));
    b.addInstruction(call("dead", List.of("Ljava/lang/Object;"), 2));
    b.addInstruction(new BuilderInstruction11x(Opcode.THROW, 1));
    b.addCatch(
        new ImmutableTypeReference("Ljava/lang/Throwable;"),
        b.getLabel("try"),
        b.getLabel("end"),
        b.getLabel("throwable"));
    b.addCatch(b.getLabel("try"), b.getLabel("end"), b.getLabel("shadowed"));
  }

  @Test
  public void handlerBehindCatchThrowableIsRemoved() {
    Body body = convert("ShadowedCatchAll", 3, DexShadowedHandlerTest::shadowedCatchAll, List.of());

    assertFalse(body.toString().contains("dead"), body.toString());
    assertEquals(
        1,
        body.toString().lines().filter(l -> l.trim().startsWith("catch ")).count(),
        body.toString());
  }

  /** Before, the dead handler reached LocalSplitter, which failed on the use of v2 */
  @Test
  public void localSplitterLoadsTheBody() {
    List<BodyInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new LocalSplitter());
    Body body =
        convert("ShadowedCatchAllSplit", 3, DexShadowedHandlerTest::shadowedCatchAll, interceptors);

    assertFalse(body.toString().contains("dead"), body.toString());
  }
}
