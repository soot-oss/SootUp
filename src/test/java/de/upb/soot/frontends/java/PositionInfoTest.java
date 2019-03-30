package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test for source position mapping.
 *
 * @author Linghui Luo
 */
@Category(Java8Test.class)
public class PositionInfoTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;
  private SootMethod method;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
    declareClassSig = sigFactory.getClassSignature("InstructionCollection");
  }

  void loadCurrentMethod(
      String methodName,
      JavaClassSignature declaringClassSignature,
      String fqReturnType,
      List<String> parameters) {
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                methodName, declaringClassSignature, fqReturnType, parameters));
    assertTrue(m.isPresent());
    method = m.get();
  }

  @Test
  public void testBinaryOpInstruction() {
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));

    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
    IStmt stmt = stmts.get(3);
    PositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();
    Position pos1 = info.getOperandPosition(0);
    Position pos2 = info.getOperandPosition(1);
    assertEquals(14, pos1.getFirstLine());
    assertEquals(14, pos1.getLastLine());
    assertEquals(12, pos1.getFirstCol());
    assertEquals(13, pos1.getLastCol());

    assertEquals(14, pos2.getFirstLine());
    assertEquals(14, pos2.getLastLine());
    assertEquals(16, pos2.getFirstCol());
    assertEquals(17, pos2.getLastCol());

    assertEquals(14, stmtPos.getLastLine());
    assertEquals(17, stmtPos.getLastCol());
    // FIXME: its coincedence that it works here; problem is like the next line
    assertEquals(14, stmtPos.getFirstLine());
    // FIXME:
    // assertEquals(4, stmtPos.getFirstCol());

  }

  @Test
  public void testReturnInstruction() {

    loadCurrentMethod("call1", declareClassSig, "long", Arrays.asList("int"));

    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
    IStmt stmt = stmts.get(4);
    PositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    Position pos1 = info.getOperandPosition(0);
    assertEquals(38, pos1.getFirstLine());
    assertEquals(38, pos1.getLastLine());
    assertEquals(11, pos1.getFirstCol());
    assertEquals(21, pos1.getLastCol());

    assertEquals(38, stmtPos.getFirstLine());
    assertEquals(38, stmtPos.getLastLine());
    assertEquals(4, stmtPos.getFirstCol());
    assertEquals(22, stmtPos.getLastCol());
  }

  @Test
  public void testReturnVoidInstruction() {

    // implicit return i.e. end of method
    loadCurrentMethod("call0", declareClassSig, "void", Arrays.asList());
    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(6);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      // TODO: to be discussed? there is no explicit return -> position of the last statement seems
      // valid but could be irritating for first time users -> better Col to 0/-1 to indicate its
      // implicit existence?
      assertEquals(33, stmtPos.getFirstLine());
      assertEquals(33, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(48, stmtPos.getLastCol());
    }

    // with explicit return (at the end)
    loadCurrentMethod("call01", declareClassSig, "void", Arrays.asList());
    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(2);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      assertEquals(47, stmtPos.getFirstLine());
      assertEquals(47, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(11, stmtPos.getLastCol());
    }
  }

  @Test
  public void testGotoInstruction() {
    // has no useful operands
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));

    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
    IStmt stmt = stmts.get(12);
    PositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(20, stmtPos.getFirstLine());
    assertEquals(20, stmtPos.getLastLine());
    assertEquals(8, stmtPos.getFirstCol());
    assertEquals(22, stmtPos.getLastCol());
  }

  @Test
  public void testGetInstruction() {

    loadCurrentMethod("readSth", declareClassSig, "void", Arrays.asList());
    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(1);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      /*
       * FIXME Position pos1 = info.getOperandPosition(0); assertEquals(51, pos1.getFirstLine()); assertEquals(51,
       * pos1.getLastLine()); assertEquals(21, pos1.getFirstCol()); assertEquals(31, pos1.getLastCol());
       */

      assertEquals(51, stmtPos.getFirstLine());
      assertEquals(51, stmtPos.getLastLine());
      // FIXME (assigned local is missing) assertEquals(11, stmtPos.getFirstCol());
      assertEquals(31, stmtPos.getLastCol());
    }

    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(2);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      /*
       * FIXME Position pos1 = info.getOperandPosition(0); assertEquals(52, pos1.getFirstLine()); assertEquals(52,
       * pos1.getLastLine()); assertEquals(11, pos1.getFirstCol()); assertEquals(23, pos1.getLastCol());
       */
      assertEquals(52, stmtPos.getFirstLine());
      assertEquals(52, stmtPos.getLastLine());
      // FIXME assertEquals(11, stmtPos.getFirstCol());
      assertEquals(22, stmtPos.getLastCol());
    }

    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(3);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      /*
       * FIXME Position pos1 = info.getOperandPosition(0); assertEquals(53, pos1.getFirstLine()); assertEquals(53,
       * pos1.getLastLine()); assertEquals(25, pos1.getFirstCol()); assertEquals(58, pos1.getLastCol());
       */
      assertEquals(53, stmtPos.getFirstLine());
      assertEquals(53, stmtPos.getLastLine());
      // FIXME assertEquals(10, stmtPos.getFirstCol());
      assertEquals(58, stmtPos.getLastCol());
    }

    {
      List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
      IStmt stmt = stmts.get(4);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      /*
       * FIXME Position pos1 = info.getOperandPosition(0); assertEquals(54, pos1.getFirstLine()); assertEquals(54,
       * pos1.getLastLine()); assertEquals(21, pos1.getFirstCol()); assertEquals(32, pos1.getLastCol());
       */
      assertEquals(54, stmtPos.getFirstLine());
      assertEquals(54, stmtPos.getLastLine());
      // FIXME assertEquals(10, stmtPos.getFirstCol());
      assertEquals(32, stmtPos.getLastCol());
    }
  }

  @Test
  public void testPutInstruction() {
    loadCurrentMethod("<init>", declareClassSig, "void", Arrays.asList());

    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
    IStmt stmt = stmts.get(2);
    PositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(11, stmtPos.getFirstLine());
    assertEquals(11, stmtPos.getLastLine());
    assertEquals(17, stmtPos.getFirstCol());
    assertEquals(26, stmtPos.getLastCol());

    /*
     * FIXME: no operands are given Position pos1 = info.getOperandPosition(0); assertEquals(11, pos1.getFirstLine());
     * assertEquals(11, pos1.getLastLine()); assertEquals(17, pos1.getFirstCol()); assertEquals(22, pos1.getLastCol());
     *
     * Position pos2 = info.getOperandPosition(1); assertEquals(11, pos2.getFirstLine()); assertEquals(11,
     * pos2.getLastLine()); assertEquals(25, pos2.getFirstCol()); assertEquals(26, pos2.getLastCol());
     */
  }

  @Test
  public void testBranchInstruction() {
    // no operand: includes only the branching conditions itself

    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));

    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());
    {
      IStmt stmt = stmts.get(9);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      // TODO: stmtPos

      /* FIXME
      Position pos1 = info.getOperandPosition(0);
      assertEquals(20, pos1.getFirstLine());
      assertEquals(20, pos1.getLastLine());
      assertEquals(8, pos1.getFirstCol());
      assertEquals(13, pos1.getLastCol());
      */
    }
    {
      IStmt stmt = stmts.get(14);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      /* FIXME
      Position pos1 = info.getOperandPosition(0);
      assertEquals(20, pos1.getFirstLine());
      assertEquals(20, pos1.getLastLine());
      assertEquals(8, pos1.getFirstCol());
      assertEquals(22, pos1.getLastCol());
      */
    }
  }

  @Test
  public void testInvokeInstruction() {
    // [ms] has no interesting operand data/positions; maybe parameters but i don't see a additional
    // benefit
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));
    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());

    {
      // !hasDef() -> same class, no parameters
      IStmt stmt = stmts.get(4);
      PositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(15, stmtPos.getFirstLine());
      assertEquals(15, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(11, stmtPos.getLastCol());
    }

    {
      // hasDef -> has parameter or is method of different class
      IStmt stmt = stmts.get(5);
      PositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      assertEquals(16, stmtPos.getFirstLine());
      assertEquals(16, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(12, stmtPos.getLastCol());
    }
  }

  @Test
  public void testConversionInstruction() {
    loadCurrentMethod("complexOperands", declareClassSig, "void", Arrays.asList());
    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());

    {
      IStmt stmt = stmts.get(15);
      PositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(90, stmtPos.getFirstLine());
      assertEquals(90, stmtPos.getLastLine());
      assertEquals(16, stmtPos.getFirstCol());
      assertEquals(24, stmtPos.getLastCol());

      /*
       * FIXME get operand data 0: (type) 1: unconvertedvalue Position pos1 = info.getOperandPosition(0); assertEquals(88,
       * pos1.getFirstLine()); assertEquals(88, pos1.getLastLine()); assertEquals(16, pos1.getFirstCol()); assertEquals(24,
       * pos1.getLastCol());
       *
       * Position pos2 = info.getOperandPosition(1); assertEquals(88, pos2.getFirstLine()); assertEquals(88,
       * pos2.getLastLine()); assertEquals(23, pos2.getFirstCol()); assertEquals(24, pos2.getLastCol());
       */
    }
  }

  @Test
  public void testInstanceOfInstruction() {
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));
    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());

    IStmt stmt = stmts.get(30);
    PositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(25, stmtPos.getFirstLine());
    assertEquals(25, stmtPos.getLastLine());
    assertEquals(10, stmtPos.getFirstCol());
    assertEquals(32, stmtPos.getLastCol());

    /*
     * FIXME get left and right value positions Position pos1 = info.getOperandPosition(0); assertEquals(25,
     * pos1.getFirstLine()); assertEquals(25, pos1.getLastLine()); assertEquals(10, pos1.getFirstCol()); assertEquals(11,
     * pos1.getLastCol());
     *
     * Position pos2 = info.getOperandPosition(1); assertEquals(25, pos2.getFirstLine()); assertEquals(25,
     * pos2.getLastLine()); assertEquals(23, pos2.getFirstCol()); assertEquals(32, pos2.getLastCol());
     */
  }

  @Test
  public void testNewInstruction() {
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));
    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());

    IStmt stmt = stmts.get(18);
    PositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(21, stmtPos.getFirstLine());
    assertEquals(21, stmtPos.getLastLine());
    assertEquals(23, stmtPos.getFirstCol());
    assertEquals(48, stmtPos.getLastCol());

    /*
     * FIXME get left and right value positions Position pos1 = info.getOperandPosition(0); assertEquals(25,
     * pos1.getFirstLine()); assertEquals(25, pos1.getLastLine()); assertEquals(10, pos1.getFirstCol()); assertEquals(11,
     * pos1.getLastCol());
     *
     * Position pos2 = info.getOperandPosition(1); assertEquals(25, pos2.getFirstLine()); assertEquals(25,
     * pos2.getLastLine()); assertEquals(23, pos2.getFirstCol()); assertEquals(32, pos2.getLastCol());
     */

  }

  @Test
  public void testUnaryInstruction() {
    loadCurrentMethod("complexOperands", declareClassSig, "void", Arrays.asList());
    List<IStmt> stmts = new ArrayList<IStmt>(method.getActiveBody().getStmts());

    IStmt stmt = stmts.get(20);
    PositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(92, stmtPos.getFirstLine());
    assertEquals(92, stmtPos.getLastLine());
    assertEquals(22, stmtPos.getFirstCol());
    assertEquals(28, stmtPos.getLastCol());

    // TODO: operands

  }
}
