package sootup.core.graph;

import static org.junit.Assert.*;

import org.junit.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;

public class MutableBasicBlockTest {

  Stmt firstNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt secondNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt thirdNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt fourthNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

  @Test(expected = IndexOutOfBoundsException.class)
  public void testUnlinkedSplitBeginningNewHead() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockUnlinked(0);
    assertEquals(0, block.getStmtCount());
    assertEquals(4, newBlock.getStmtCount());
  }

  @Test
  public void testUnlinkedSplitBeginning() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockUnlinked(1);
    assertEquals(1, block.getStmtCount());
    assertEquals(3, newBlock.getStmtCount());
  }

  @Test
  public void testUnlinkedSplit() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockUnlinked(2);
    assertEquals(2, block.getStmtCount());
    assertEquals(2, newBlock.getStmtCount());
  }

  @Test
  public void testUnlinkedSplitEnd() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockUnlinked(3);
    assertEquals(3, block.getStmtCount());
    assertEquals(1, newBlock.getStmtCount());
  }

  @Test
  public void testLinkedSplitBeginningNewHead() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(secondNop, true);
    assertEquals(1, block.getStmtCount());
    assertEquals(3, newBlock.getStmtCount());
  }

  @Test
  public void testLinkedSplitBeginningNewTail() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(firstNop, false);
    assertEquals(1, block.getStmtCount());
    assertEquals(3, newBlock.getStmtCount());
  }

  @Test
  public void testLinkedSplit() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(thirdNop, false);
    assertEquals(3, block.getStmtCount());
    assertEquals(1, newBlock.getStmtCount());
  }

  public void testLinkedSplitEndNewHead() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(fourthNop, true);
    assertEquals(1, block.getStmtCount());
    assertEquals(3, newBlock.getStmtCount());
  }

  @Test
  public void testLinkedSplitEnd() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(thirdNop, false);
    assertEquals(3, block.getStmtCount());
    assertEquals(1, newBlock.getStmtCount());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testLinkedSplitEndException() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(fourthNop, false);
    assertEquals(4, block.getStmtCount());
    assertEquals(0, newBlock.getStmtCount());
  }
}
