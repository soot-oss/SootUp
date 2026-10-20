package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.jf.dexlib2.builder.instruction.BuilderInstruction10t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction23x;
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
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.java.core.views.JavaView;

/**
 * Dex has no array instruction with an immediate index, so {@code arr[1] = x} arrives as {@code v0
 * = 1; arr[v0] = x}. {@link sootup.apk.frontend.interceptors.DexArrayIndexConstantInliner} puts the
 * constant back when it is the same on every path.
 */
public class DexArrayIndexConstantInlinerTest {

  @TempDir static Path tempDir;

  private static final int IDX = 0;
  private static final int ARR = 1;
  private static final int VAL = 2;
  private static final int TMP = 3;

  /** Builds a dex with one static {@code void run()} and loads it with the default interceptors. */
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

  /** {@code int[] arr = new int[2]; int val = 7;} */
  private static void newArray(MethodImplementationBuilder b) {
    b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, IDX, 2));
    b.addInstruction(
        new BuilderInstruction22c(Opcode.NEW_ARRAY, ARR, IDX, new ImmutableTypeReference("[I")));
    b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, VAL, 7));
  }

  private static BuilderInstruction11n constIndex(int value) {
    return new BuilderInstruction11n(Opcode.CONST_4, IDX, value);
  }

  /** {@code arr[idx] = val} */
  private static BuilderInstruction23x store() {
    return new BuilderInstruction23x(Opcode.APUT, VAL, ARR, IDX);
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

  private static BuilderInstruction10x returnVoid() {
    return new BuilderInstruction10x(Opcode.RETURN_VOID);
  }

  /** The index of every array store and load in {@code body}, in statement order. */
  private static List<Value> arrayIndices(Body body) {
    List<Value> indices = new ArrayList<>();
    for (Stmt stmt : body.getStmts()) {
      if (!(stmt instanceof JAssignStmt)) {
        continue;
      }
      JAssignStmt assign = (JAssignStmt) stmt;
      if (assign.getLeftOp() instanceof JArrayRef) {
        indices.add(((JArrayRef) assign.getLeftOp()).getIndex());
      }
      if (assign.getRightOp() instanceof JArrayRef) {
        indices.add(((JArrayRef) assign.getRightOp()).getIndex());
      }
    }
    return indices;
  }

  @Test
  public void literalIndicesAreInlined() {
    // arr[1] = val; arr[2] = val; val = arr[2]; with the index register reused for both.
    Body body =
        convert(
            "Literal",
            3,
            b -> {
              newArray(b);
              b.addInstruction(constIndex(1));
              b.addInstruction(store());
              b.addInstruction(constIndex(2));
              b.addInstruction(store());
              b.addInstruction(new BuilderInstruction23x(Opcode.AGET, VAL, ARR, IDX));
              b.addInstruction(returnVoid());
            });

    assertEquals(
        List.of(IntConstant.getInstance(1), IntConstant.getInstance(2), IntConstant.getInstance(2)),
        arrayIndices(body));
  }

  @Test
  public void differentConstantsOnTwoPathsStayLocal() {
    Body body =
        convert(
            "TwoConstants",
            4,
            b -> {
              newArray(b);
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, TMP, 0));
              b.addInstruction(new BuilderInstruction21t(Opcode.IF_EQZ, TMP, b.getLabel("other")));
              b.addInstruction(constIndex(0));
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("join")));
              b.addLabel("other");
              b.addInstruction(constIndex(1));
              b.addLabel("join");
              b.addInstruction(store());
              b.addInstruction(returnVoid());
            });

    List<Value> indices = arrayIndices(body);
    assertEquals(1, indices.size());
    assertTrue(indices.get(0) instanceof Local, indices.toString());
  }

  @Test
  public void sameConstantOnTwoPathsIsInlined() {
    Body body =
        convert(
            "SameConstant",
            4,
            b -> {
              newArray(b);
              b.addInstruction(new BuilderInstruction11n(Opcode.CONST_4, TMP, 0));
              b.addInstruction(new BuilderInstruction21t(Opcode.IF_EQZ, TMP, b.getLabel("other")));
              b.addInstruction(constIndex(1));
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("join")));
              b.addLabel("other");
              b.addInstruction(constIndex(1));
              b.addLabel("join");
              b.addInstruction(store());
              b.addInstruction(returnVoid());
            });

    assertEquals(List.of(IntConstant.getInstance(1)), arrayIndices(body));
  }

  @Test
  public void nonConstantIndexStaysLocal() {
    // arr[arr.length - ...]: the index comes from array-length, not a constant.
    Body body =
        convert(
            "NonConstant",
            3,
            b -> {
              newArray(b);
              b.addInstruction(new BuilderInstruction12x(Opcode.ARRAY_LENGTH, IDX, ARR));
              b.addInstruction(store());
              b.addInstruction(returnVoid());
            });

    List<Value> indices = arrayIndices(body);
    assertEquals(1, indices.size());
    assertTrue(indices.get(0) instanceof Local, indices.toString());
  }

  @Test
  public void indexReachingThroughAHandlerStaysLocal() {
    // idx = 0; try { work(); idx = 1; work(); } catch (any) {} arr[idx] = val;
    // If the first work() throws, the handler joins the store with idx = 0, an edge only the
    // exceptional predecessors show; on the normal path idx is 1.
    Body body =
        convert(
            "Handler",
            4,
            b -> {
              newArray(b);
              b.addInstruction(constIndex(0));
              b.addLabel("try");
              b.addInstruction(work());
              b.addInstruction(constIndex(1));
              b.addInstruction(work());
              b.addLabel("end");
              b.addLabel("join");
              b.addInstruction(store());
              b.addInstruction(returnVoid());
              b.addLabel("handler");
              b.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, TMP));
              b.addInstruction(new BuilderInstruction10t(Opcode.GOTO, b.getLabel("join")));
              b.addCatch(b.getLabel("try"), b.getLabel("end"), b.getLabel("handler"));
            });

    List<Value> indices = arrayIndices(body);
    assertEquals(1, indices.size());
    assertTrue(indices.get(0) instanceof Local, indices.toString());
  }
}
