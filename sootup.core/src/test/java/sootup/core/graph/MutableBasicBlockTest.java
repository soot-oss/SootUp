package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;

public class MutableBasicBlockTest {

  Stmt firstNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  Stmt secondNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  Stmt thirdNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  Stmt fourthNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

  @Test
  public void testUnlinkedSplitBeginningNewHead() {
    MutableBasicBlockImpl block = new MutableBasicBlockImpl();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          MutableBasicBlock mutableBasicBlock = block.splitBlockUnlinked(0);
        });
  }

  @Test
  public void testUnlinkedSplitBeginning() {
    MutableBasicBlockImpl block = new MutableBasicBlockImpl();
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
    MutableBasicBlockImpl block = new MutableBasicBlockImpl();
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
    MutableBasicBlockImpl block = new MutableBasicBlockImpl();
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
    MutableBasicBlock block = new MutableBasicBlockImpl();
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
    MutableBasicBlock block = new MutableBasicBlockImpl();
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
    MutableBasicBlock block = new MutableBasicBlockImpl();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(thirdNop, false);
    assertEquals(3, block.getStmtCount());
    assertEquals(1, newBlock.getStmtCount());
  }

  public void testLinkedSplitEndNewHead() {
    MutableBasicBlock block = new MutableBasicBlockImpl();
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
    MutableBasicBlock block = new MutableBasicBlockImpl();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    MutableBasicBlock newBlock = block.splitBlockLinked(thirdNop, false);
    assertEquals(3, block.getStmtCount());
    assertEquals(1, newBlock.getStmtCount());
  }

  @Test
  public void testLinkedSplitEndException() {
    MutableBasicBlock block = new MutableBasicBlockImpl();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
    block.addStmt(thirdNop);
    block.addStmt(fourthNop);

    assertThrows(
        IndexOutOfBoundsException.class,
        () -> {
          MutableBasicBlock newBlock = block.splitBlockLinked(fourthNop, false);
        });
  }
}
