package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction51l;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.apk.frontend.main.DexCastExpr;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.interceptors.LocalSplitter;
import sootup.java.core.views.JavaView;

/**
 * A dex conversion such as {@code float-to-int} may be the only use that tells that the bits of a
 * constant are a float. Its cast keeps the source type ({@link DexCastExpr}), so that the number
 * transformer gives the constant its floating point value.
 */
public class DexCastSourceTypeTest {

  @TempDir static Path tempDir;

  /** Builds a dex with one static {@code void run()} and loads it with the dex interceptors */
  private static Body convert(String name, Consumer<MethodImplementationBuilder> instructions) {
    String descriptor = "Ldex/" + name + ";";
    MethodImplementationBuilder builder = new MethodImplementationBuilder(2);
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

    List<BodyInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new LocalSplitter());
    interceptors.addAll(DexBodyInterceptors.Default.bodyInterceptors());
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

  /** {@code invoke-static {vReg}, dex.App.use(type)} */
  private static BuilderInstruction35c use(String type, int reg, int registerCount) {
    return new BuilderInstruction35c(
        Opcode.INVOKE_STATIC,
        registerCount,
        reg,
        reg + 1,
        0,
        0,
        0,
        new ImmutableMethodReference("Ldex/App;", "use", List.of(type), "V"));
  }

  private static Value constant(Body body) {
    return body.getStmts().stream()
        .filter(s -> s instanceof JAssignStmt)
        .map(s -> ((JAssignStmt) s).getRightOp())
        .filter(v -> v instanceof sootup.core.jimple.common.constant.Constant)
        .findFirst()
        .orElseThrow();
  }

  private static DexCastExpr cast(Body body) {
    for (Stmt s : body.getStmts()) {
      if (s instanceof JAssignStmt && ((JAssignStmt) s).getRightOp() instanceof DexCastExpr) {
        return (DexCastExpr) ((JAssignStmt) s).getRightOp();
      }
    }
    throw new AssertionError("no dex cast in " + body);
  }

  @Test
  public void floatConvertedToInt() {
    // const v0, <bits of 9.99f>; float-to-int v0, v0; use(v0)
    Body body =
        convert(
            "FloatToInt",
            b -> {
              b.addInstruction(
                  new BuilderInstruction31i(Opcode.CONST, 0, Float.floatToIntBits(9.99f)));
              b.addInstruction(new BuilderInstruction12x(Opcode.FLOAT_TO_INT, 0, 0));
              b.addInstruction(use("I", 0, 1));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    // the splitter renamed the cast's operand and kept the source type
    assertEquals(PrimitiveType.FloatType.getInstance(), cast(body).getSourceType());
    assertEquals(PrimitiveType.IntType.getInstance(), cast(body).getType());
    assertEquals(FloatConstant.getInstance(9.99f), constant(body));
  }

  @Test
  public void doubleConvertedToLong() {
    // const-wide v0, <bits of 2.5>; double-to-long v0, v0; use(v0)
    Body body =
        convert(
            "DoubleToLong",
            b -> {
              b.addInstruction(
                  new BuilderInstruction51l(Opcode.CONST_WIDE, 0, Double.doubleToLongBits(2.5)));
              b.addInstruction(new BuilderInstruction12x(Opcode.DOUBLE_TO_LONG, 0, 0));
              b.addInstruction(use("J", 0, 2));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(PrimitiveType.DoubleType.getInstance(), cast(body).getSourceType());
    assertEquals(DoubleConstant.getInstance(2.5), constant(body));
  }

  @Test
  public void intConvertedToFloatStaysAnInt() {
    // const v0, 7; int-to-float v0, v0; use(v0)
    Body body =
        convert(
            "IntToFloat",
            b -> {
              b.addInstruction(new BuilderInstruction31i(Opcode.CONST, 0, 7));
              b.addInstruction(new BuilderInstruction12x(Opcode.INT_TO_FLOAT, 0, 0));
              b.addInstruction(use("F", 0, 1));
              b.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
            });

    assertEquals(PrimitiveType.IntType.getInstance(), cast(body).getSourceType());
    assertEquals(IntConstant.getInstance(7), constant(body));
  }

  @Test
  public void rebuildingTheCastKeepsTheSourceType() {
    DexCastExpr cast =
        new DexCastExpr(
            IntConstant.getInstance(1),
            PrimitiveType.IntType.getInstance(),
            PrimitiveType.FloatType.getInstance());
    assertInstanceOf(DexCastExpr.class, cast.withOp(IntConstant.getInstance(2)));
    assertEquals(
        PrimitiveType.FloatType.getInstance(),
        cast.withType(PrimitiveType.LongType.getInstance()).getSourceType());
    assertTrue(cast.withOp(IntConstant.getInstance(2)).getOp() instanceof IntConstant);
  }
}
