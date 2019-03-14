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
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "test", declareClassSig, "void", Arrays.asList("int", "int")));
    assertTrue(m.isPresent());
    method = m.get();
  }

  @Test
  public void testBinaryOpInstruction() {
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

    assertEquals(14, stmtPos.getFirstLine());
    assertEquals(14, stmtPos.getLastLine());
    assertEquals(17, stmtPos.getLastCol());
    // FIX ME
    // assertEquals(4, stmtPos.getFirstCol());
  }
}
