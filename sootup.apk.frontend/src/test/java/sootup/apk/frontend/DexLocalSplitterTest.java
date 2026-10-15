package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.interceptors.LocalSplitter;
import sootup.interceptors.LocalSplitter.ExceptionalFlow;
import sootup.java.core.views.JavaView;

/**
 * Splits locals of dex bodies whose register is redefined inside a try block, to check which value
 * each handler is given. Dex reuses registers, and instructions like check-cast or an iget into the
 * register they read from both read and redefine it: when they throw, they have not written
 * anything, so the handler still sees the old value.
 *
 * <p>Each test's comment shows its Jimple after splitting, with method signatures shortened, e.g.
 * {@code use(x)} for {@code staticinvoke <dex.App: void use(java.lang.Object)>(x)}.
 */
public class DexLocalSplitterTest {

  @TempDir static Path tempDir;

  private static final String EXCEPTION = "Ljava/lang/Exception;";

  /** Builds a dex with one static {@code void run()}, plus further classes, and loads it. */
  private static Body convert(
      String name,
      int registerCount,
      ExceptionalFlow exceptionalFlow,
      Consumer<MethodImplementationBuilder> instructions,
      List<ImmutableClassDef> otherClasses) {
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
    List<ImmutableClassDef> classes = new ArrayList<>(otherClasses);
    classes.add(classDef(descriptor, "Ljava/lang/Object;", List.of(method)));

    Path dex = tempDir.resolve(name + ".dex");
    try {
      DexPool.writeTo(dex.toString(), new ImmutableDexFile(Opcodes.forApi(15), classes));
    } catch (IOException e) {
      throw new IllegalStateException("could not write " + dex, e);
    }

    List<BodyInterceptor> interceptors =
        new ArrayList<>(DexBodyInterceptors.Default.bodyInterceptors());
    // the default pipeline splits locals first; run that split with the flow under test instead
    interceptors.replaceAll(
        interceptor ->
            interceptor instanceof LocalSplitter ? new LocalSplitter(exceptionalFlow) : interceptor);
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

  private static Body convert(
      String name,
      int registerCount,
      ExceptionalFlow exceptionalFlow,
      Consumer<MethodImplementationBuilder> instructions) {
    return convert(name, registerCount, exceptionalFlow, instructions, List.of());
  }

  private static ImmutableClassDef classDef(
      String descriptor, String superclass, List<ImmutableMethod> methods) {
    return new ImmutableClassDef(
        descriptor,
        AccessFlags.PUBLIC.getValue(),
        superclass,
        null,
        null,
        null,
        null,
        null,
        methods,
        null);
  }

  /** {@code invoke-static {vReg}, dex.App.name(type)} */
  private static BuilderInstruction35c call(String name, String type, int reg) {
    return new BuilderInstruction35c(
        Opcode.INVOKE_STATIC,
        1,
        reg,
        0,
        0,
        0,
        0,
        new ImmutableMethodReference("Ldex/App;", name, List.of(type), "V"));
  }

  /** {@code invoke-static {}, dex.App.work()} */
  private static BuilderInstruction35c work() {
    return new BuilderInstruction35c(
        Opcode.INVOKE_STATIC,
        0,
        0,
        0,
        0,
        0,
        0,
        new ImmutableMethodReference("Ldex/App;", "work", List.of(), "V"));
  }

  /**
   * The end of the try block, a use of v0 after it, and a handler that uses v0 as well; type null
   * makes it a catch-all.
   */
  private static void endTryAndHandle(MethodImplementationBuilder b, String type, int exception) {
    b.addLabel("end");
    b.addInstruction(call("use", "Ljava/lang/Object;", 0));
    b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
    b.addLabel("handler");
    b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, exception));
    b.addInstruction(call("use", "Ljava/lang/Object;", 0));
    b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
    if (type == null) {
      b.addCatch(b.getLabel("try"), b.getLabel("end"), b.getLabel("handler"));
    } else {
      b.addCatch(
          new ImmutableTypeReference(type),
          b.getLabel("try"),
          b.getLabel("end"),
          b.getLabel("handler"));
    }
  }

  /** The local the handler passes to use(), and the one the normal path passes to it. */
  private static List<String> readsOfV0(Body body) {
    List<String> reads = new ArrayList<>();
    for (Stmt stmt : body.getStmts()) {
      String text = stmt.toString();
      if (text.contains("void use(java.lang.Object)")) {
        String local = text.substring(text.lastIndexOf('(') + 1, text.lastIndexOf(')'));
        // drop the _n of DexSharedInitializationLocalSplitter's copies of a shared constant
        reads.add(local.replaceFirst("_\\d+$", ""));
      }
    }
    // the normal path's use comes first in the dex code, the handler's second
    return List.of(reads.get(1), reads.get(0));
  }

  /** A try block around {@code iget-object v0, v0, f}: x = x.f */
  private static Consumer<MethodImplementationBuilder> fieldReadIntoItsObject(boolean thenWork) {
    return b -> {
      b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
      b.addLabel("try");
      b.addInstruction(
          new BuilderInstruction22c(
              Opcode.IGET_OBJECT,
              0,
              0,
              new ImmutableFieldReference("Ldex/F;", "f", "Ljava/lang/Object;")));
      if (thenWork) {
        b.addInstruction(work());
      }
      endTryAndHandle(b, EXCEPTION, 1);
    };
  }

  /**
   * The iget throws before it writes v0, so the handler sees v0 from before the try.
   *
   * <pre>{@code
   *   $u0#0 = 0;
   * label1:
   *   $u0#1 = $u0#0.f;
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u1 := @caughtexception;
   *   use($u0#0);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void fieldReadIntoItsObjectLeavesTheOldValueToItsHandler() {
    Body body = convert("FieldRead", 2, ExceptionalFlow.SYNCHRONOUS, fieldReadIntoItsObject(false));
    // handler, normal path
    assertEquals(List.of("$u0#0", "$u0#1"), readsOfV0(body));
  }

  /**
   * check-cast always rewrites its own register, and throws before it does.
   *
   * <pre>{@code
   *   $u0#0 = "x";
   * label1:
   *   $u0#1 = (java.lang.String) $u0#0;
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u1 := @caughtexception;
   *   use($u0#0);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void checkCastLeavesTheUncastValueToItsHandler() {
    Body body =
        convert(
            "CheckCast",
            2,
            ExceptionalFlow.SYNCHRONOUS,
            b -> {
              b.addInstruction(
                  new BuilderInstruction21c(
                      Opcode.CONST_STRING, 0, new ImmutableStringReference("x")));
              b.addLabel("try");
              b.addInstruction(
                  new BuilderInstruction21c(
                      Opcode.CHECK_CAST, 0, new ImmutableTypeReference("Ljava/lang/String;")));
              endTryAndHandle(b, EXCEPTION, 1);
            });
    assertEquals(List.of("$u0#0", "$u0#1"), readsOfV0(body));
  }

  /**
   * When a call after the iget throws, v0 was already overwritten: the handler can see either
   * value, so they stay one local.
   *
   * <pre>{@code
   *   $u0 = 0;
   * label1:
   *   $u0 = $u0.f;
   *   work();
   * label2:
   *   use($u0);
   *   return;
   * label3:
   *   $u1 := @caughtexception;
   *   use($u0);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void fieldReadIntoItsObjectFollowedByACallKeepsBothValuesForItsHandler() {
    Body body =
        convert("FieldReadThenCall", 2, ExceptionalFlow.SYNCHRONOUS, fieldReadIntoItsObject(true));
    assertEquals(List.of("$u0", "$u0"), readsOfV0(body));
  }

  /**
   * v0 holds an int, then a String copied in by move-object, which cannot throw: the handler is
   * only entered from the call after it and only ever sees the String, so the int stays a local of
   * its own, whatever the handler catches.
   */
  private static Body copyThenCall(String name, ExceptionalFlow flow, String type) {
    return convert(
        name,
        3,
        flow,
        b -> {
          b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 5));
          b.addInstruction(call("useInt", "I", 0));
          b.addInstruction(
              new BuilderInstruction21c(Opcode.CONST_STRING, 1, new ImmutableStringReference("s")));
          b.addLabel("try");
          b.addInstruction(new BuilderInstruction12x(Opcode.MOVE_OBJECT, 0, 1));
          b.addInstruction(work());
          endTryAndHandle(b, type, 2);
        },
        List.of(
            classDef("Ldex/AppError;", "Ljava/lang/Error;", List.of()),
            classDef(
                "Ldex/AppVirtualMachineError;", "Ljava/lang/VirtualMachineError;", List.of())));
  }

  /**
   * Jimple after splitting:
   *
   * <pre>{@code
   *   $u0#0 = 5;
   *   useInt($u0#0);
   *   $u1 = "s";
   * label1:
   *   $u0#1 = $u1;
   *   work();
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0#1);
   *   return;
   * catch java.lang.Throwable from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void copyThatCannotThrowKeepsTheOldValueFromItsHandler() {
    Body body = copyThenCall("CopyThenCall", ExceptionalFlow.SYNCHRONOUS, null);
    assertEquals(List.of("$u0#1", "$u0#1"), readsOfV0(body));
  }

  /**
   * A handler of Exception never catches a VirtualMachineError, so the copy cannot enter it.
   *
   * <pre>{@code
   *   $u0#0 = 5;
   *   useInt($u0#0);
   *   $u1 = "s";
   * label1:
   *   $u0#1 = $u1;
   *   work();
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0#1);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void exceptionHandlerIsNotEnteredAsynchronously() {
    Body body = copyThenCall("ExceptionHandler", ExceptionalFlow.VIRTUAL_MACHINE_ERRORS, EXCEPTION);
    assertEquals(List.of("$u0#1", "$u0#1"), readsOfV0(body));
  }

  /**
   * Nor does a handler of an Error that is not a VirtualMachineError.
   *
   * <pre>{@code
   *   $u0#0 = 5;
   *   useInt($u0#0);
   *   $u1 = "s";
   * label1:
   *   $u0#1 = $u1;
   *   work();
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0#1);
   *   return;
   * catch dex.AppError from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void otherErrorHandlerIsNotEnteredAsynchronously() {
    Body body =
        copyThenCall("AppErrorHandler", ExceptionalFlow.VIRTUAL_MACHINE_ERRORS, "Ldex/AppError;");
    assertEquals(List.of("$u0#1", "$u0#1"), readsOfV0(body));
  }

  /**
   * A catch-all also catches a VirtualMachineError, which may be thrown before the copy completes:
   * the int may reach the handler, and v0 stays one local.
   *
   * <pre>{@code
   *   $u0 = 5;
   *   useInt($u0);
   *   $u1 = "s";
   * label1:
   *   $u0 = $u1;
   *   work();
   * label2:
   *   use($u0);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0);
   *   return;
   * catch java.lang.Throwable from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void catchAllHandlerCanBeEnteredAsynchronously() {
    Body body = copyThenCall("CatchAll", ExceptionalFlow.VIRTUAL_MACHINE_ERRORS, null);
    assertEquals(List.of("$u0", "$u0"), readsOfV0(body));
  }

  /**
   * So does a handler of a subclass of VirtualMachineError.
   *
   * <pre>{@code
   *   $u0 = 5;
   *   useInt($u0);
   *   $u1 = "s";
   * label1:
   *   $u0 = $u1;
   *   work();
   * label2:
   *   use($u0);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0);
   *   return;
   * catch dex.AppVirtualMachineError from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void virtualMachineErrorHandlerCanBeEnteredAsynchronously() {
    Body body =
        copyThenCall(
            "AppVmErrorHandler",
            ExceptionalFlow.VIRTUAL_MACHINE_ERRORS,
            "Ldex/AppVirtualMachineError;");
    assertEquals(List.of("$u0", "$u0"), readsOfV0(body));
  }

  /**
   * A handler type whose superclasses are not in the view might be a VirtualMachineError, so it is
   * treated as one.
   *
   * <pre>{@code
   *   $u0 = 5;
   *   useInt($u0);
   *   $u1 = "s";
   * label1:
   *   $u0 = $u1;
   *   work();
   * label2:
   *   use($u0);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0);
   *   return;
   * catch java.lang.OutOfMemoryError from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void unknownHandlerTypeIsTreatedAsOneThatCanBeEnteredAsynchronously() {
    Body body =
        copyThenCall(
            "UnknownHandler",
            ExceptionalFlow.VIRTUAL_MACHINE_ERRORS,
            "Ljava/lang/OutOfMemoryError;");
    assertEquals(List.of("$u0", "$u0"), readsOfV0(body));
  }

  /**
   * With any exception possible at any point, every handler can be entered from the copy.
   *
   * <pre>{@code
   *   $u0 = 5;
   *   useInt($u0);
   *   $u1 = "s";
   * label1:
   *   $u0 = $u1;
   *   work();
   * label2:
   *   use($u0);
   *   return;
   * label3:
   *   $u2 := @caughtexception;
   *   use($u0);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void anyHandlerCanBeEnteredFromAnyStmt() {
    Body body = copyThenCall("AnyFlow", ExceptionalFlow.ANY, EXCEPTION);
    assertEquals(List.of("$u0", "$u0"), readsOfV0(body));
  }

  /**
   * A try block of only a const cannot throw, so with synchronous exceptions nothing enters its
   * handler. The handler is still in the body; its use of v0 is given the value from before the try
   * instead of failing to be renamed.
   *
   * <pre>{@code
   *   $u0#0 = 0;
   * label1:
   *   $u0#1 = 1;
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u1 := @caughtexception;
   *   use($u0#0);
   *   return;
   * catch java.lang.Exception from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void handlerThatNothingCanEnterIsStillRenamed() {
    Body body =
        convert(
            "NothingEnters",
            2,
            ExceptionalFlow.SYNCHRONOUS,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addLabel("try");
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 1));
              endTryAndHandle(b, EXCEPTION, 1);
            });
    assertEquals(List.of("$u0#0", "$u0#1"), readsOfV0(body));
  }

  /**
   * The same with a catch-all, which asynchronous errors can enter. The range is only the write: an
   * error before it completes leaves v0 at 0, and after it execution is outside the range. So the
   * handler still only sees 0.
   *
   * <pre>{@code
   *   $u0#0 = 0;
   * label1:
   *   $u0#1 = 1;
   * label2:
   *   use($u0#1);
   *   return;
   * label3:
   *   $u1 := @caughtexception;
   *   use($u0#0);
   *   return;
   * catch java.lang.Throwable from label1 to label2 with label3;
   * }</pre>
   */
  @Test
  public void handlerOfOnlyAWriteSeesTheOldValueEvenWithAsynchronousErrors() {
    for (ExceptionalFlow flow :
        List.of(ExceptionalFlow.VIRTUAL_MACHINE_ERRORS, ExceptionalFlow.ANY)) {
      Body body =
          convert(
              "OnlyAWrite" + flow,
              2,
              flow,
              b -> {
                b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
                b.addLabel("try");
                b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 1));
                endTryAndHandle(b, null, 1);
              });
      assertEquals(List.of("$u0#0", "$u0#1"), readsOfV0(body), flow.toString());
    }
  }
}
