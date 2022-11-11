package sootup.java.sourcecode.frontend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.sourcecode.WalaClassLoaderTestUtils;

/**
 * Test for source position mapping.
 *
 * @author Linghui Luo
 */
@Category(Java8Test.class)
@Ignore // stmtposition need to be adapted
public class StmtPositionInfoTest {
  private WalaJavaClassProvider loader;

  private JavaIdentifierFactory identifierFactory;
  private JavaClassType declareClassSig;
  private SootMethod method;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "../shared-test-resources/selected-java-target/";
    loader = new WalaJavaClassProvider(srcDir);
    identifierFactory = JavaIdentifierFactory.getInstance();
    declareClassSig = identifierFactory.getClassType("InstructionCollection");
  }

  void loadCurrentMethod(
      String methodName,
      JavaClassType declaringClassSignature,
      String fqReturnType,
      List<String> parameters) {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loader,
            identifierFactory.getMethodSignature(
                declaringClassSignature, methodName, fqReturnType, parameters));
    assertTrue(m.isPresent());
    method = m.get();
  }

  @Test
  public void testBinaryOpInstruction() {
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(3);
    StmtPositionInfo info = stmt.getPositionInfo();
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
  public void testBinaryOpInstructionMultiline() {

    loadCurrentMethod("complexOperands", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(8);
    StmtPositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(85, stmtPos.getFirstLine());
    assertEquals(87, stmtPos.getLastLine());
    assertEquals(16, stmtPos.getFirstCol());
    assertEquals(25, stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    Position pos2 = info.getOperandPosition(1);

    /* FIXME: multiline binOp parameter are nullpointer -> WALA has no debugInfo.instructionPosition data for that range
    assertEquals(85, pos1.getFirstLine());
    assertEquals(85, pos1.getLastLine());
    assertEquals(16, pos1.getFirstCol());
    assertEquals(17, pos1.getLastCol());

    assertEquals(87, pos2.getFirstLine());
    assertEquals(87, pos2.getLastLine());
    assertEquals(24, pos2.getFirstCol());
    assertEquals(25, pos2.getLastCol());
    */
  }

  @Test
  public void testReturnInstruction() {

    loadCurrentMethod("call1", declareClassSig, "long", Collections.singletonList("int"));

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(4);
    StmtPositionInfo info = stmt.getPositionInfo();
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
    loadCurrentMethod("call0", declareClassSig, "void", Collections.emptyList());
    {
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(6);
      StmtPositionInfo info = stmt.getPositionInfo();
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
    loadCurrentMethod("call01", declareClassSig, "void", Collections.emptyList());
    {
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(2);
      StmtPositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();

      assertEquals(47, stmtPos.getFirstLine());
      assertEquals(47, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(11, stmtPos.getLastCol());
    }
  }

  @Test
  public void testGotoInstruction() {
    // has no operands
    loadCurrentMethod("test", declareClassSig, "void", Arrays.asList("int", "int"));

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(12);
    StmtPositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(20, stmtPos.getFirstLine());
    assertEquals(20, stmtPos.getLastLine());
    assertEquals(8, stmtPos.getFirstCol());
    assertEquals(22, stmtPos.getLastCol());
  }

  @Test
  public void testGetInstruction() {

    loadCurrentMethod("readSth", declareClassSig, "void", Collections.emptyList());
    {
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(1);
      StmtPositionInfo info = stmt.getPositionInfo();
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
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(2);
      StmtPositionInfo info = stmt.getPositionInfo();
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
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(3);
      StmtPositionInfo info = stmt.getPositionInfo();
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
      List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
      Stmt stmt = stmts.get(4);
      StmtPositionInfo info = stmt.getPositionInfo();
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
    loadCurrentMethod("<init>", declareClassSig, "void", Collections.emptyList());

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(2);
    StmtPositionInfo info = stmt.getPositionInfo();
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

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    {
      Stmt stmt = stmts.get(9);
      StmtPositionInfo info = stmt.getPositionInfo();
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
      Stmt stmt = stmts.get(14);
      StmtPositionInfo info = stmt.getPositionInfo();
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
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    {
      // !hasDef() -> same class, no parameters
      Stmt stmt = stmts.get(4);
      StmtPositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();
      assertEquals(15, stmtPos.getFirstLine());
      assertEquals(15, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(11, stmtPos.getLastCol());
    }

    {
      // hasDef -> has parameter or is method of different class
      Stmt stmt = stmts.get(5);
      StmtPositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();
      assertEquals(16, stmtPos.getFirstLine());
      assertEquals(16, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(12, stmtPos.getLastCol());

      Assert.assertEquals(16, info.getOperandPosition(1).getFirstLine());
      Assert.assertEquals(10, info.getOperandPosition(1).getFirstCol());
      Assert.assertEquals(16, info.getOperandPosition(1).getLastLine());
      Assert.assertEquals(11, info.getOperandPosition(1).getLastCol());
    }

    {
      Stmt stmt = stmts.get(6);
      StmtPositionInfo info = stmt.getPositionInfo();
      Position stmtPos = info.getStmtPosition();
      assertEquals(17, stmtPos.getFirstLine());
      assertEquals(17, stmtPos.getLastLine());
      assertEquals(4, stmtPos.getFirstCol());
      assertEquals(15, stmtPos.getLastCol());

      Assert.assertEquals(17, info.getOperandPosition(1).getFirstLine());
      Assert.assertEquals(10, info.getOperandPosition(1).getFirstCol());
      Assert.assertEquals(17, info.getOperandPosition(1).getLastLine());
      Assert.assertEquals(11, info.getOperandPosition(1).getLastCol());

      Assert.assertEquals(17, info.getOperandPosition(2).getFirstLine());
      Assert.assertEquals(13, info.getOperandPosition(2).getFirstCol());
      Assert.assertEquals(17, info.getOperandPosition(2).getLastLine());
      Assert.assertEquals(14, info.getOperandPosition(2).getLastCol());
    }
  }

  @Test
  public void testConversionInstruction() {
    loadCurrentMethod("complexOperands", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    {
      Stmt stmt = stmts.get(15);
      StmtPositionInfo info = stmt.getPositionInfo();

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
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(33);
    StmtPositionInfo info = stmt.getPositionInfo();

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
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(18);
    StmtPositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(21, stmtPos.getFirstLine());
    assertEquals(21, stmtPos.getLastLine());
    assertEquals(23, stmtPos.getFirstCol());
    assertEquals(48, stmtPos.getLastCol());

    /*
        // FIXME get type
        Position pos1 = info.getOperandPosition(0);
        assertEquals(21, pos1.getFirstLine());
        assertEquals(21, pos1.getLastLine());
        assertEquals(23, pos1.getFirstCol());
        assertEquals(44, pos1.getLastCol());

        // FIXME get size
        Position pos2 = info.getOperandPosition(1);
        assertEquals(21, pos2.getFirstLine());
        assertEquals(21, pos2.getLastLine());
        assertEquals(45, pos2.getFirstCol());
        assertEquals(47, pos2.getLastCol());
    */

  }

  @Test
  public void testUnaryInstruction() {
    loadCurrentMethod("complexOperands", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    { // assignment: int a;
      Stmt stmt = stmts.get(1);
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(78, stmtPos.getFirstLine());
      assertEquals(78, stmtPos.getLastLine());
      assertEquals(12, stmtPos.getFirstCol());
      assertEquals(13, stmtPos.getLastCol());

      /* FIXME
      Position pos1 = info.getOperandPosition(0);
      assertEquals(78, pos1.getFirstLine());
      assertEquals(78, pos1.getLastLine());
      assertEquals(12, pos1.getFirstCol());
      assertEquals(13, pos1.getLastCol());
      */
    }

    { // assignment: int b = 2;
      Stmt stmt = stmts.get(2);
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(79, stmtPos.getFirstLine());
      assertEquals(79, stmtPos.getLastLine());
      assertEquals(12, stmtPos.getFirstCol());
      assertEquals(17, stmtPos.getLastCol());

      /* FIXME operands are missing
      Position pos1 = info.getOperandPosition(0);
      assertEquals(79, pos1.getFirstLine());
      assertEquals(79, pos1.getLastLine());
      assertEquals(12, pos1.getFirstCol());
      assertEquals(13, pos1.getLastCol());

      Position pos2 = info.getOperandPosition(1);
      assertEquals(79, pos2.getFirstLine());
      assertEquals(79, pos2.getLastLine());
      assertEquals(16, pos2.getFirstCol());
      assertEquals(13, pos2.getLastCol());
      */
    }

    { // unary: !
      Stmt stmt = stmts.get(20);
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(92, stmtPos.getFirstLine());
      assertEquals(92, stmtPos.getLastLine());
      assertEquals(22, stmtPos.getFirstCol());
      assertEquals(28, stmtPos.getLastCol());

      /* FIXME
      Position pos1 = info.getOperandPosition(0);
      assertEquals(92, pos1.getFirstLine());
      assertEquals(92, pos1.getLastLine());
      assertEquals(23, pos1.getFirstCol());
      assertEquals(28, pos1.getLastCol());
      */
    }
  }

  @Test
  public void testThrowInstruction() {
    loadCurrentMethod("exceptionMethod", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(3);
    StmtPositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(106, stmtPos.getFirstLine());
    assertEquals(106, stmtPos.getLastLine());
    assertEquals(12, stmtPos.getFirstCol());
    assertEquals(50, stmtPos.getLastCol());

    /*
        // FIXME type
        Position pos1 = info.getOperandPosition(0);
        assertEquals(106, pos1.getFirstLine());
        assertEquals(106, pos1.getLastLine());
        assertEquals(18, pos1.getFirstCol());
        assertEquals(49, pos1.getLastCol());
    */

  }

  @Test
  public void testSwitchInstruction() {
    loadCurrentMethod("favouriteNumber", declareClassSig, "int", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(2);
    StmtPositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(65, stmtPos.getFirstLine());
    assertEquals(73, stmtPos.getLastLine());
    assertEquals(8, stmtPos.getFirstCol());
    assertEquals(9, stmtPos.getLastCol());

    // TODO: organize keybox,labels, targets as operands?

  }

  @Ignore
  public void testLoadMetadataInstruction() {
    // TODO: implement - no instruction example found
    loadCurrentMethod("metadata", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(0);
    StmtPositionInfo info = stmt.getPositionInfo();
    /*
    Position stmtPos = info.getStmtPosition();
    assertEquals(, stmtPos.getFirstLine());
    assertEquals(, stmtPos.getLastLine());
    assertEquals(, stmtPos.getFirstCol());
    assertEquals( , stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals( , pos1.getFirstLine());
    assertEquals( , pos1.getLastLine());
    assertEquals( , pos1.getFirstCol());
    assertEquals( , pos1.getLastCol());
    */
  }

  @Ignore
  public void testCheckCastInstruction() {
    // TODO: implement - no instruction example found
    loadCurrentMethod("TODO", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(0);
    StmtPositionInfo info = stmt.getPositionInfo();
    /*
    Position stmtPos = info.getStmtPosition();
    assertEquals(, stmtPos.getFirstLine());
    assertEquals(, stmtPos.getLastLine());
    assertEquals(, stmtPos.getFirstCol());
    assertEquals( , stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals( , pos1.getFirstLine());
    assertEquals( , pos1.getLastLine());
    assertEquals( , pos1.getFirstCol());
    assertEquals( , pos1.getLastCol());
    */
  }

  @Ignore
  public void testEnclosingObjectReference() {
    // TODO: implement - no instruction example found
    loadCurrentMethod("enclosingobject", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(0);
    StmtPositionInfo info = stmt.getPositionInfo();
    /*
    Position stmtPos = info.getStmtPosition();
    assertEquals(, stmtPos.getFirstLine());
    assertEquals(, stmtPos.getLastLine());
    assertEquals(, stmtPos.getFirstCol());
    assertEquals( , stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals( , pos1.getFirstLine());
    assertEquals( , pos1.getLastLine());
    assertEquals( , pos1.getFirstCol());
    assertEquals( , pos1.getLastCol());
    */
  }

  @Ignore
  public void testAstLexicalRead() {
    // TODO: implement - no instruction example found
    loadCurrentMethod("TODO", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(0);
    StmtPositionInfo info = stmt.getPositionInfo();
    /*
    Position stmtPos = info.getStmtPosition();
    assertEquals(, stmtPos.getFirstLine());
    assertEquals(, stmtPos.getLastLine());
    assertEquals(, stmtPos.getFirstCol());
    assertEquals( , stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals( , pos1.getFirstLine());
    assertEquals( , pos1.getLastLine());
    assertEquals( , pos1.getFirstCol());
    assertEquals( , pos1.getLastCol());
    */
  }

  @Ignore
  public void testAstLexicalWrite() {
    // TODO: implement - no instruction example found
    loadCurrentMethod("TODO", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(0);
    StmtPositionInfo info = stmt.getPositionInfo();
    /*
    Position stmtPos = info.getStmtPosition();
    assertEquals(, stmtPos.getFirstLine());
    assertEquals(, stmtPos.getLastLine());
    assertEquals(, stmtPos.getFirstCol());
    assertEquals( , stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals( , pos1.getFirstLine());
    assertEquals( , pos1.getLastLine());
    assertEquals( , pos1.getFirstCol());
    assertEquals( , pos1.getLastCol());
    */
  }

  @Test
  public void testAssertInstruction() {
    loadCurrentMethod("atomictwo", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    {
      Stmt stmt = stmts.get(9); // to 17
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(133, stmtPos.getFirstLine());
      assertEquals(133, stmtPos.getLastLine());
      assertEquals(8, stmtPos.getFirstCol());
      // FIXME: currently condition is not included only the referenced/checked object;
      // assertEquals(32, stmtPos.getLastCol());

      /*  TODO: how to evaluate/set generated code positions? [ms] i set both cols to -1 to indicate generated code
      Position pos1 = info.getOperandPosition(0);
      assertEquals( 130, pos1.getFirstLine());
      assertEquals( 130, pos1.getLastLine());
      assertEquals( -1, pos1.getFirstCol());
      assertEquals( -1, pos1.getLastCol());
      */
    }
  }

  @Test
  public void testMonitorInstruction() {

    // FIXME: [ms] synchronized void atomicone has no monitors?

    loadCurrentMethod("atomictwo", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    { // entermonitor
      Stmt stmt = stmts.get(2);
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(130, stmtPos.getFirstLine());
      assertEquals(132, stmtPos.getLastLine());
      assertEquals(8, stmtPos.getFirstCol());
      assertEquals(9, stmtPos.getLastCol());

      /* FIXME: if referenced object should get position information
      Position pos1 = info.getOperandPosition(0);
      assertEquals( 130, pos1.getFirstLine());
      assertEquals( 130, pos1.getLastLine());
      assertEquals( 21, pos1.getFirstCol());
      assertEquals( 25, pos1.getLastCol());
      */
    }

    { // exitmonitor
      Stmt stmt = stmts.get(4);
      StmtPositionInfo info = stmt.getPositionInfo();

      Position stmtPos = info.getStmtPosition();
      assertEquals(130, stmtPos.getFirstLine());
      assertEquals(132, stmtPos.getLastLine());
      assertEquals(8, stmtPos.getFirstCol());
      assertEquals(9, stmtPos.getLastCol());
    }
  }

  @Test
  public void testGetCaughtExceptionInstruction() {
    loadCurrentMethod("exceptionMethod", declareClassSig, "void", Collections.emptyList());
    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());

    Stmt stmt = stmts.get(4);
    StmtPositionInfo info = stmt.getPositionInfo();

    Position stmtPos = info.getStmtPosition();
    assertEquals(107, stmtPos.getFirstLine());
    assertEquals(109, stmtPos.getLastLine());
    assertEquals(10, stmtPos.getFirstCol());
    assertEquals(9, stmtPos.getLastCol());
    /*
        // FIXME position of the caught exception
        Position pos1 = info.getOperandPosition(0);
        assertEquals(107, pos1.getFirstLine());
        assertEquals(107, pos1.getLastLine());
        assertEquals(17, pos1.getFirstCol());
        assertEquals(28, pos1.getLastCol());
    */
  }

  @Test
  public void testArrayLengthInstruction() {
    loadCurrentMethod("exceptionMethod", declareClassSig, "void", Collections.emptyList());

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(7);
    StmtPositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(111, stmtPos.getFirstLine());
    assertEquals(111, stmtPos.getLastLine());
    assertEquals(18, stmtPos.getFirstCol());
    // FIXME: it is at arrayname currently; imho it should include .length    assertEquals(32,
    // stmtPos.getLastCol());

    Position pos1 = info.getOperandPosition(0);
    assertEquals(111, pos1.getFirstLine());
    assertEquals(111, pos1.getLastLine());
    assertEquals(18, pos1.getFirstCol());
    assertEquals(25, pos1.getLastCol());
  }

  @Test
  public void testArrayLoadInstruction() {
    loadCurrentMethod("exceptionMethod", declareClassSig, "void", Collections.emptyList());

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(11);
    StmtPositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(115, stmtPos.getFirstLine());
    assertEquals(115, stmtPos.getLastLine());
    assertEquals(21, stmtPos.getFirstCol());
    assertEquals(31, stmtPos.getLastCol());

    /* FIXME
    Position pos1 = info.getOperandPosition(0);
    assertEquals(115, pos1.getFirstLine());
    assertEquals(115, pos1.getLastLine());
    assertEquals(29, pos1.getFirstCol());
    assertEquals(30, pos1.getLastCol());
    */
  }

  @Test
  public void testArrayStoreInstruction() {
    loadCurrentMethod("exceptionMethod", declareClassSig, "void", Collections.emptyList());

    List<Stmt> stmts = new ArrayList<>(method.getBody().getStmts());
    Stmt stmt = stmts.get(8);
    StmtPositionInfo info = stmt.getPositionInfo();
    Position stmtPos = info.getStmtPosition();

    assertEquals(112, stmtPos.getFirstLine());
    assertEquals(112, stmtPos.getLastLine());
    assertEquals(8, stmtPos.getFirstCol());
    assertEquals(24, stmtPos.getLastCol());

    /* FIXME
    // index
    Position pos1 = info.getOperandPosition(0);
    assertEquals(112, pos1.getFirstLine());
    assertEquals(112, pos1.getLastLine());
    assertEquals(8, pos1.getFirstCol());
    assertEquals(18, pos1.getLastCol());

    // value
    Position pos2 = info.getOperandPosition(1);
    assertEquals(112, pos2.getFirstLine());
    assertEquals(112, pos2.getLastLine());
    assertEquals(21, pos2.getFirstCol());
    assertEquals(24, pos2.getLastCol());
    */
  }
}
