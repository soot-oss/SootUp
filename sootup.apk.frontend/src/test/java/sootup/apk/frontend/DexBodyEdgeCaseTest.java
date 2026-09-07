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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.SwitchLabelElement;
import org.jf.dexlib2.builder.instruction.BuilderArrayPayload;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31t;
import org.jf.dexlib2.builder.instruction.BuilderPackedSwitchPayload;
import org.jf.dexlib2.builder.instruction.BuilderSparseSwitchPayload;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.jimple.common.Trap;
import sootup.core.model.Body;
import sootup.core.util.printer.BriefStmtPrinter;
import sootup.java.core.views.JavaView;

/**
 * Converts hand-built dex bodies that are legal for Dalvik but unusual for a Java compiler, to pin
 * down what the frontend makes of them.
 *
 * <p>The bodies are assembled with dexlib2 and written to a bare {@code .dex} file instead of an
 * APK, which the frontend accepts and which keeps every case readable next to its assertion. Most
 * of them are about {@code nop}: dex has that instruction and needs it to pad the payload of a
 * switch or of fill-array-data to a four byte boundary, so nops turn up in ordinary compiler output
 * and anywhere an obfuscator wants to move code around.
 *
 * <p>The line the frontend draws is reachability, not the opcode: a Stmt that flow can arrive at
 * stays in the Jimple, a nop as much as anything else, and only what nothing reaches is dropped -
 * it cannot be expressed in a ControlFlowGraph that {@code Body.build} accepts. So each case below
 * says both which Stmts survive and, where it matters, that a reachable nop is among them.
 */
public class DexBodyEdgeCaseTest {

  @TempDir static Path tempDir;

  /**
   * Builds a dex holding one static {@code void run()} with the given instructions and loads it.
   */
  private static Body convert(
      String name, int registerCount, Consumer<MethodImplementationBuilder> instructions) {
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
                new ApkAnalysisInputLocation(
                    dex,
                    new AndroidVersionInfo(dex, ""),
                    DexBodyInterceptors.Default.bodyInterceptors())));
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

  private static List<String> stmtsOf(Body body) {
    return body.getStmts().stream().map(Object::toString).collect(Collectors.toList());
  }

  private static List<Trap> trapsOf(Body body) {
    return new BriefStmtPrinter(body.getControlFlowGraph()).getTraps();
  }

  /**
   * {@code goto} does not fall through, so a nop behind it heads a block that nothing branches to.
   * This is the shape that obfuscated APKs are full of.
   */
  @Test
  public void unreachableNopBehindGotoIsDropped() {
    Body body =
        convert(
            "NopBehindGoto",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("l")));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("goto", "return"), stmtsOf(body));
  }

  /**
   * A nop between a {@code return} and a branch target. The return does not fall through, so the
   * nop is dead, but it sits at the tail of the return's block and carries the fallthrough edge
   * into the target's block. A {@code NopEliminator} run over the built graph cannot repair this
   * one: {@code removeNode(nop, keepFlow=true)} leaves the block's successor edge in place, and it
   * then hangs off the {@code return}.
   */
  @Test
  public void unreachableNopBehindReturnIsDropped() {
    Body body =
        convert(
            "NopBehindReturn",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(new BuilderInstruction21t(Opcode.IF_EQZ, 0, b.getLabel("l")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("$u0 = 0", "if $u0 == 0", "return", "return"), stmtsOf(body));
  }

  /** A nop that is a jump target is reached by that jump, so it stays and keeps the jump. */
  @Test
  public void nopThatIsABranchTargetIsKept() {
    Body body =
        convert(
            "BranchToANop",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(new BuilderInstruction21t(Opcode.IF_EQZ, 0, b.getLabel("l")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 1));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(
        List.of("$u0 = 0", "if $u0 == 0", "return", "nop", "nop", "$u0 = 1", "return"),
        stmtsOf(body));
    // the second successor of the if is the branch target, which is the first of the two nops
    assertEquals(
        "nop", body.getControlFlowGraph().successors(body.getStmts().get(1)).get(1).toString());
  }

  /** The same for a backwards jump, which makes the nop the head of the loop body. */
  @Test
  public void nopThatIsABackwardsBranchTargetIsKept() {
    Body body =
        convert(
            "BackwardsBranchToANop",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction21t(Opcode.IF_EQZ, 0, b.getLabel("l")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("$u0 = 0", "nop", "if $u0 == 0", "return"), stmtsOf(body));
    assertEquals(
        "nop", body.getControlFlowGraph().successors(body.getStmts().get(2)).get(1).toString());
  }

  /** Nops behind the last Stmt of the body - the only case the frontend used to handle. */
  @Test
  public void trailingNopsAreDropped() {
    Body body =
        convert(
            "TrailingNops",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
            });

    assertEquals(List.of("return"), stmtsOf(body));
  }

  /** A nop as the entry instruction is the reachable Stmt of the method, and stays its head. */
  @Test
  public void leadingNopIsKeptAsTheStartingStmt() {
    Body body =
        convert(
            "LeadingNop",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("nop", "$u0 = 0", "return"), stmtsOf(body));
    assertEquals("nop", body.getControlFlowGraph().getStartingStmt().toString());
  }

  /** A body that is nothing but nops and a return: every one of them is on the path. */
  @Test
  public void bodyOfOnlyNopsIsKept() {
    Body body =
        convert(
            "OnlyNops",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("nop", "nop", "nop", "return"), stmtsOf(body));
  }

  /**
   * The reason nops exist in compiler output: a packed-switch payload has to start on a four byte
   * boundary, so dx/d8 pads with a nop whenever the preceding code has an odd number of code units.
   */
  @Test
  public void alignmentNopBeforeAPackedSwitchPayload() {
    Body body =
        convert(
            "PackedSwitchPadding",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 1));
              b.addInstruction(
                  new BuilderInstruction31t(Opcode.PACKED_SWITCH, 0, b.getLabel("payload")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("case0");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("case1");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addLabel("payload");
              b.addInstruction(
                  new BuilderPackedSwitchPayload(
                      0, List.of(b.getLabel("case0"), b.getLabel("case1"))));
            });

    assertEquals(
        List.of(
            "$u0 = 1",
            "switch($u0) {     case 0:     case 1:     default:  }",
            "return",
            "return",
            "return"),
        stmtsOf(body));
  }

  /** The same for a sparse-switch payload. */
  @Test
  public void alignmentNopBeforeASparseSwitchPayload() {
    Body body =
        convert(
            "SparseSwitchPadding",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 1));
              b.addInstruction(
                  new BuilderInstruction31t(Opcode.SPARSE_SWITCH, 0, b.getLabel("payload")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("case7");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addLabel("payload");
              b.addInstruction(
                  new BuilderSparseSwitchPayload(
                      List.of(new SwitchLabelElement(7, b.getLabel("case7")))));
            });

    assertTrue(
        stmtsOf(body).stream().anyMatch(stmt -> stmt.startsWith("switch")),
        stmtsOf(body).toString());
    assertTrue(stmtsOf(body).stream().noneMatch("nop"::equals), stmtsOf(body).toString());
  }

  /**
   * fill-array-data with an empty payload is the one place where the frontend itself emits a nop.
   */
  @Test
  public void emptyArrayPayload() {
    Body body =
        convert(
            "EmptyArrayPayload",
            2,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(
                  new BuilderInstruction22c(
                      Opcode.NEW_ARRAY, 1, 0, new ImmutableTypeReference("[I")));
              b.addInstruction(
                  new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, 1, b.getLabel("data")));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("data");
              b.addInstruction(new BuilderArrayPayload(4, List.of()));
            });

    assertEquals(List.of("$u0 = 0", "$u1 = newarray (int)[$u0]", "nop", "return"), stmtsOf(body));
  }

  /** A try block that starts on a nop keeps that nop, and with it its range. */
  @Test
  public void trapBeginningOnANopKeepsItsRange() {
    Body body =
        convert(
            "TrapBeginsOnNop",
            2,
            b -> {
              b.addLabel("try");
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addLabel("end");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("handler");
              b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, 1));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addCatch(
                  new ImmutableTypeReference("Ljava/lang/Exception;"),
                  b.getLabel("try"),
                  b.getLabel("end"),
                  b.getLabel("handler"));
            });

    assertEquals(
        List.of("nop", "$u0 = 0", "return", "$u1 := @caughtexception", "return"), stmtsOf(body));
    assertEquals(1, trapsOf(body).size());
    assertEquals("nop", trapsOf(body).get(0).getBeginStmt().toString());
  }

  /** A try block whose whole range is nops still covers them: they are reachable code. */
  @Test
  public void trapCoveringOnlyNopsIsKept() {
    Body body =
        convert(
            "TrapCoversOnlyNops",
            2,
            b -> {
              b.addLabel("try");
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addLabel("end");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("handler");
              b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, 1));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addCatch(
                  new ImmutableTypeReference("Ljava/lang/Exception;"),
                  b.getLabel("try"),
                  b.getLabel("end"),
                  b.getLabel("handler"));
            });

    assertEquals(
        List.of("nop", "nop", "return", "$u1 := @caughtexception", "return"), stmtsOf(body));
    assertEquals(1, trapsOf(body).size());
    assertEquals("nop", trapsOf(body).get(0).getBeginStmt().toString());
  }

  /** A trap that reaches the end of the body, with the padding nops behind its last Stmt. */
  @Test
  public void trapReachingTheEndOfTheBodyWithTrailingNops() {
    Body body =
        convert(
            "TrapToEndOfBody",
            2,
            b -> {
              b.addLabel("try");
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("out")));
              b.addLabel("handler");
              b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, 1));
              b.addLabel("out");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
              b.addLabel("end");
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
              b.addCatch(
                  new ImmutableTypeReference("Ljava/lang/Exception;"),
                  b.getLabel("try"),
                  b.getLabel("end"),
                  b.getLabel("handler"));
            });

    // addTraps() answers a Trap that ends on a nop by inserting a caught exception Stmt of its own
    // and ending the Trap there, so the body holds that Stmt next to the move-exception it covers
    assertEquals(
        List.of("$u0 = 0", "goto", "r0 := @caughtexception", "$u1 := @caughtexception", "return"),
        stmtsOf(body));
    assertEquals(1, trapsOf(body).size());
  }

  /** An endless loop, which is a body without any return at all. */
  @Test
  public void endlessLoopWithoutAReturn() {
    Body body =
        convert(
            "EndlessLoop",
            1,
            b -> {
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("l")));
            });

    assertEquals(List.of("goto"), stmtsOf(body));
  }

  /** A throw behind which the padding nops sit. */
  @Test
  public void nopsBehindAThrow() {
    Body body =
        convert(
            "NopsBehindThrow",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
              b.addInstruction(new BuilderInstruction11x(Opcode.THROW, 0));
              b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
            });

    assertEquals(List.of("$u0 = 0", "throw $u0"), stmtsOf(body));
  }

  /** Unreachable code that is not a nop is legal in dex too, and goes the same way. */
  @Test
  public void unreachableCodeThatIsNotANopIsDropped() {
    Body body =
        convert(
            "UnreachableCode",
            1,
            b -> {
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("l")));
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 3));
              b.addLabel("l");
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(List.of("goto", "return"), stmtsOf(body));
  }

  /**
   * A nop that flow reaches and that ends the body means control runs off the end of the method.
   * Dalvik's verifier rejects that, so no compiler or obfuscator emits it, and the frontend cannot
   * express it either: the nop falls through to a block that does not exist. Dropping the nop would
   * only hand the same fallthrough to the Stmt in front of it, which is what the frontend used to
   * do - it reported the very same failure, naming {@code $u0 = 0} instead of the nop.
   */
  @Test
  public void reachableTrailingNopRunsOffTheEndOfTheBody() {
    RuntimeException e =
        assertThrows(
            RuntimeException.class,
            () ->
                convert(
                    "ReachableTrailingNop",
                    1,
                    b -> {
                      b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
                      b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
                    }));

    assertTrue(
        messagesOf(e).stream().anyMatch(m -> m.contains("falls into the abyss")),
        messagesOf(e).toString());
  }

  /** The same where the nop is both the fallthrough and the target of an if. */
  @Test
  public void ifFallingIntoATrailingNopRunsOffTheEndOfTheBody() {
    RuntimeException e =
        assertThrows(
            RuntimeException.class,
            () ->
                convert(
                    "IfIntoTrailingNop",
                    1,
                    b -> {
                      b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, 0, 0));
                      b.addInstruction(
                          new BuilderInstruction21t(Opcode.IF_EQZ, 0, b.getLabel("l")));
                      b.addLabel("l");
                      b.addInstruction(new BuilderInstruction10x(Opcode.NOP));
                    }));

    assertTrue(
        messagesOf(e).stream().anyMatch(m -> m.contains("falls into the abyss")),
        messagesOf(e).toString());
  }

  /** Every message along the cause chain, so a test can look for the one it cares about. */
  private static List<String> messagesOf(Throwable throwable) {
    List<String> messages = new ArrayList<>();
    for (Throwable t = throwable; t != null; t = t.getCause()) {
      messages.add(String.valueOf(t.getMessage()));
    }
    return messages;
  }
}
