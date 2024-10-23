package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.UnknownType;

@Tag("Java8")
public class MutableBlockStmtGraphTest {

  BranchingStmt firstGoto = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
  JNopStmt firstNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  JNopStmt secondNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  JNopStmt thirdNop = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

  BranchingStmt ifStmt =
      new JIfStmt(
          new JLeExpr(IntConstant.getInstance(2), IntConstant.getInstance(3)),
          StmtPositionInfo.getNoStmtPositionInfo());

  private ClassType throwableSig =
      new ClassType() {

        @Override
        public String getFullyQualifiedName() {
          return getPackageName() + "." + getClassName();
        }

        @Override
        public String getClassName() {
          return "Throwable";
        }

        @Override
        public PackageName getPackageName() {
          return new PackageName("java.lang");
        }
      };

  private ClassType ioExceptionSig =
      new ClassType() {

        @Override
        public String getFullyQualifiedName() {
          return getPackageName() + "." + getClassName();
        }

        @Override
        public String getClassName() {
          return "IOException";
        }

        @Override
        public PackageName getPackageName() {
          return new PackageName("java.io");
        }
      };

  Stmt firstHandlerStmt =
      new JIdentityStmt(
          new Local("ex", throwableSig),
          new JCaughtExceptionRef(throwableSig),
          StmtPositionInfo.getNoStmtPositionInfo());
  Stmt secondHandlerStmt =
      new JIdentityStmt(
          new Local("ex2", throwableSig),
          new JCaughtExceptionRef(ioExceptionSig),
          StmtPositionInfo.getNoStmtPositionInfo());

  @Test
  public void addNodeTest() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());
    graph.addNode(firstGoto);
    assertEquals(1, graph.getBlocks().size());

    // test duplicate insertion of the same node
    graph.addNode(firstGoto);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlockOf(firstGoto).getStmts().size());

    graph.addNode(secondNop);
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.getBlockOf(firstGoto).getStmts().size());

    try {
      graph.removeNode(firstGoto);
      fail("should not be reachable due to exception");
    } catch (Exception ignored) {
    }

    graph.removeNode(firstGoto, false);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlockOf(secondNop).getStmts().size());

    // removal of not existing
    try {
      graph.removeNode(firstGoto);
      fail("should not be reachable due to exception");
    } catch (Exception ignored) {
    }
    assertEquals(1, graph.getBlocks().size());

    graph.removeNode(secondNop);
    assertEquals(0, graph.getBlocks().size());
  }

  @Test
  public void removeStmtBetweenEdges() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    assertEquals(3, graph.getBlockOf(firstNop).getStmts().size());

    graph.removeNode(secondNop);
    assertEquals(Arrays.asList(firstNop, thirdNop), graph.getBlockOf(firstNop).getStmts());
  }

  @Test
  public void removeStmtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(thirdNop);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlockOf(firstNop).getStmts());
  }

  @Test
  public void removeStmtHead() {
    assertNotEquals(Arrays.asList(firstNop, secondNop), Arrays.asList(firstNop, thirdNop));

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(firstNop);
    assertEquals(Arrays.asList(secondNop, thirdNop), graph.getBlockOf(secondNop).getStmts());
  }

  @Test
  public void removeStmtConditionalTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, ifStmt);

    graph.removeNode(ifStmt, false);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlockOf(firstNop).getStmts());
  }

  @Test
  public void testSetEdges() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstGoto);
    graph.setEdges(firstGoto, Collections.singletonList(ifStmt));
    assertEquals(Arrays.asList(ifStmt), graph.successors(firstGoto));

    graph.setEdges(ifStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(4, graph.getBlocks().size());

    assertEquals(
        Arrays.asList(firstGoto).toString(), graph.getBlockOf(firstGoto).getStmts().toString());
    assertEquals(
        Collections.singletonList(secondNop).toString(),
        graph.getBlockOf(secondNop).getStmts().toString());
    assertEquals(
        Collections.singletonList(thirdNop).toString(),
        graph.getBlockOf(thirdNop).getStmts().toString());
  }

  @Test
  public void removeStmtConditionalTailBetweenBlocks() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, ifStmt);

    graph.setEdges(ifStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(3, graph.getBlocks().size());

    graph.removeNode(ifStmt, false);
    assertEquals(
        Collections.singletonList(firstNop).toString(),
        graph.getBlockOf(firstNop).getStmts().toString());
    assertEquals(
        Collections.singletonList(secondNop).toString(),
        graph.getBlockOf(secondNop).getStmts().toString());
    assertEquals(
        Collections.singletonList(thirdNop).toString(),
        graph.getBlockOf(thirdNop).getStmts().toString());
  }

  @Test
  public void modifyStmtToBlockAtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());
    assertEquals(0, graph.getNodes().size());

    graph.addNode(firstNop);
    graph.setStartingStmt(firstNop);
    assertEquals(1, graph.getNodes().size());
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlockOf(firstNop).getStmts().size());

    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(2, graph.getNodes().size());

    graph.putEdge(secondNop, thirdNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(3, graph.getNodes().size());

    // insert branchingstmt at end
    graph.putEdge(thirdNop, ifStmt);
    assertEquals(4, graph.getNodes().size());
    assertEquals(1, graph.getBlocks().size());
    assertEquals(0, graph.getBlockOf(firstNop).getPredecessors().size());
    assertEquals(0, graph.getBlockOf(firstNop).getSuccessors().size());

    // add connection between branchingstmt and first stmt
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlockOf(firstNop).getPredecessors().size());
    assertEquals(1, graph.getBlockOf(firstNop).getSuccessors().size());

    // add connection between branchingstmt and second stmt
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, secondNop);
    assertEquals(2, graph.getBlocks().size());

    assertEquals(3, graph.getBlockOf(ifStmt).getStmts().size());
    assertEquals(2, graph.getBlockOf(ifStmt).getPredecessors().size());
    assertEquals(2, graph.getBlockOf(ifStmt).getSuccessors().size());

    assertEquals(1, graph.getBlockOf(firstNop).getStmts().size());
    assertEquals(1, graph.getBlockOf(firstNop).getPredecessors().size());
    assertEquals(1, graph.getBlockOf(firstNop).getSuccessors().size());

    // remove non-existing edge
    assertEquals(0, graph.removeEdge(firstNop, ifStmt).size());
    assertEquals(2, graph.getBlocks().size());

    // remove branchingstmt at end -> edge across blocks
    assertEquals(1, graph.removeEdge(ifStmt, firstNop).size());
    assertEquals(2, graph.getBlocks().size());

    assertEquals(4, graph.getNodes().size());
    graph.removeNode(firstNop);
    assertEquals(3, graph.getNodes().size());

    // remove branchingstmt at head
    assertEquals(1, graph.removeEdge(ifStmt, secondNop).size());
    assertEquals(1, graph.getBlocks().size());
    assertEquals(3, graph.getNodes().size());

    graph.removeNode(secondNop);
    assertEquals(2, graph.getNodes().size());
    assertEquals(1, graph.getBlocks().size());
  }

  @Test
  public void removeEdgeMerge() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    graph.addNode(firstNop);
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, ifStmt);

    assertEquals(1, graph.getBlocks().size());
    // this edge splits the block between the first and second Nop
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, secondNop);
    assertEquals(2, graph.getBlocks().size());

    // this edge removal should merge both blocks together again
    graph.removeEdge(ifStmt, secondNop);
    assertEquals(1, graph.getBlocks().size());
  }

  @Test
  public void removeStmtInBetweenBlock() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(secondNop);

    assertEquals(graph.getBlockOf(firstNop).getStmts(), Arrays.asList(firstNop, thirdNop));
  }

  @Test
  public void checkInfoMethods() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    assertEquals(Arrays.asList(firstNop), graph.predecessors(secondNop));
    assertEquals(Arrays.asList(secondNop), graph.successors(firstNop));
    assertEquals(1, graph.outDegree(firstNop));
    assertEquals(1, graph.inDegree(secondNop));
    assertTrue(graph.hasEdgeConnecting(firstNop, secondNop));
    assertTrue(graph.hasEdgeConnecting(firstNop, secondNop));

    assertFalse(graph.hasEdgeConnecting(secondNop, firstNop));
  }

  @Test
  public void addBadSuccessorCount() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          graph.putEdge(firstNop, secondNop);
          graph.putEdge(firstGoto, 1, thirdNop);
        });
  }

  public void setBadSuccessorIdx() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstGoto, 1, secondNop);
  }

  @Test
  public void addDuplicateBadSuccessorCount() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          graph.putEdge(firstNop, secondNop);
          graph.putEdge(firstNop, secondNop);
        });
  }

  @Test
  public void addMultipleBranchingEdgesToSameTarget() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, secondNop);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, secondNop);
    assertEquals(2, graph.successors(ifStmt).size());
  }

  @Test
  public void addSameSuccessorMultipleTimes() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, secondNop);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, secondNop);

    assertEquals(2, graph.getBlocks().size());

    assertEquals(0, graph.outDegree(secondNop));
    assertEquals(2, graph.inDegree(secondNop));
    assertEquals(Arrays.asList(ifStmt, ifStmt), graph.predecessors(secondNop));
    assertEquals(2, graph.outDegree(ifStmt));

    assertEquals(Arrays.asList(secondNop, secondNop), graph.successors(ifStmt));
    assertTrue(graph.hasEdgeConnecting(ifStmt, secondNop));
    assertFalse(graph.hasEdgeConnecting(secondNop, ifStmt));
  }

  @Test
  public void addBlocks() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, ifStmt);
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, secondNop);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, thirdNop);

    assertEquals(3, graph.getBlocks().size());
  }

  @Test
  public void addBlockDirectly() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());

    MutableBasicBlock blockA = new MutableBasicBlockImpl();
    blockA.addStmt(firstGoto);
    MutableBasicBlock blockB = new MutableBasicBlockImpl();
    blockB.addStmt(secondNop);
    MutableBasicBlock blockC = new MutableBasicBlockImpl();
    blockC.addStmt(thirdNop);

    graph.addBlock(blockA.getStmts(), Collections.emptyMap());
    assertEquals(1, graph.getBlocks().size());

    graph.addBlock(blockB.getStmts(), Collections.emptyMap());
    assertEquals(2, graph.getBlocks().size());
  }

  @Test
  public void linkDirectlyAddedBlocks() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock blockA = new MutableBasicBlockImpl();
    blockA.addStmt(firstNop);
    MutableBasicBlock blockB = new MutableBasicBlockImpl();
    blockB.addStmt(secondNop);
    MutableBasicBlock blockC = new MutableBasicBlockImpl();
    blockC.addStmt(thirdNop);

    graph.addBlock(blockA.getStmts(), Collections.emptyMap());
    graph.addBlock(blockB.getStmts(), Collections.emptyMap());
    graph.addBlock(blockC.getStmts(), Collections.emptyMap());

    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(1, graph.successors(secondNop).size());

    assertEquals(1, graph.removeEdge(secondNop, thirdNop).size());
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    assertEquals(0, graph.removeEdge(secondNop, thirdNop).size()); // empty operation
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    assertEquals(0, graph.removeEdge(firstNop, thirdNop).size()); // empty operation
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    assertEquals(1, graph.removeEdge(firstNop, secondNop).size());
    assertEquals(3, graph.getBlocks().size());
  }

  @Test
  public void testRemoveNodeAtBeginning() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.successors(secondNop).size());
    assertEquals(0, graph.successors(thirdNop).size());
    assertEquals(0, graph.predecessors(secondNop).size());
    assertEquals(1, graph.predecessors(thirdNop).size());
  }

  @Test
  public void testRemoveNodeMalcolm /* i.e. in the middle */() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(secondNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(thirdNop).size());
    assertEquals(0, graph.predecessors(firstNop).size());
    assertEquals(1, graph.predecessors(thirdNop).size());
  }

  @Test
  public void testRemoveNodeAtEnd() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(thirdNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());
    assertEquals(0, graph.predecessors(firstNop).size());
    assertEquals(1, graph.predecessors(secondNop).size());
  }

  @Test
  public void testBlockAddStmt() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlockImpl();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
  }

  @Test
  public void testBlockAddStmtDuplicateStmtObjectViaGraph() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(firstNop);
    graph.addBlock(Collections.singletonList(firstNop), Collections.emptyMap());
    assertTrue(graph.containsNode(firstNop));
  }

  @Test
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraph() {
    // firstnop already has a successor and its impossible to add another edge
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          graph.putEdge(firstNop, secondNop);
          graph.addBlock(Arrays.asList(firstNop, thirdNop), Collections.emptyMap());
        });
  }

  @Disabled
  public void testBlockAddStmtDuplicateStmtMaybePossibleInTheFutureButNotImplementedThatWayYet() {
    // firstnop already has a successor and its impossible to add another edge
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.addBlock(Arrays.asList(firstNop), Collections.emptyMap());
  }

  @Test
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraphDirectManiupaltionAfterwards() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlockImpl();
    graph.addNode(firstNop);
    graph.addBlock(block.getStmts(), Collections.emptyMap());
    block.addStmt(firstNop); // BAD! don't do that!
  }

  @Test
  public void testBlockStmtValidity() {
    // try adding a stmt after branchingstmt -> definitely the last stmt of a block -> must fail
    MutableBasicBlock block = new MutableBasicBlockImpl();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          block.addStmt(ifStmt);
          block.addStmt(firstNop);
        });
  }

  @Test
  public void modifyTrapToCompleteBlock() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocks().size());
    // graph.addTrap(throwableSig, secondNop, secondNop, firstHandlerStmt);
  }

  @Test
  public void testTrapAggregation() {
    final ClassType exception1 =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return getPackageName() + "." + getClassName();
          }

          @Override
          public String getClassName() {
            return "ball";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("some.object");
          }
        };

    final ClassType exception2 =
        new ClassType() {

          @Override
          public String getFullyQualifiedName() {
            return getPackageName() + "." + getClassName();
          }

          @Override
          public String getClassName() {
            return "javelin";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("some.object");
          }
        };

    Local exc = new Local("ex", UnknownType.getInstance());
    // hint: applied types make no sense in this test!
    Stmt catchStmt1 =
        new JIdentityStmt(
            exc,
            new JCaughtExceptionRef(UnknownType.getInstance()),
            StmtPositionInfo.getNoStmtPositionInfo());
    Stmt catchStmt2 =
        new JIdentityStmt(
            exc,
            new JCaughtExceptionRef(PrimitiveType.getInt()),
            StmtPositionInfo.getNoStmtPositionInfo());
    final JReturnVoidStmt returnStmt =
        new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    final JGotoStmt stmt1 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    final JGotoStmt stmt2 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    final JGotoStmt stmt3 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());

    // test same trap merging simple
    MutableBlockStmtGraph graph0 = new MutableBlockStmtGraph();
    graph0.setStartingStmt(stmt1);
    graph0.addNode(stmt1, Collections.singletonMap(exception1, catchStmt1));
    graph0.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));

    graph0.putEdge(stmt1, 0, stmt2);
    graph0.putEdge(stmt2, 0, returnStmt);

    {
      final List<Trap> traps = graph0.buildTraps();
      assertEquals(2, traps.size()); // as @caughtexception gets currently in their way.
      assertEquals(stmt2, traps.get(1).getBeginStmt());
      assertEquals(returnStmt, traps.get(1).getEndStmt());
      assertEquals(catchStmt1, traps.get(1).getHandlerStmt());
    }

    // test merging traps from sequential blocks with the same trap
    MutableBlockStmtGraph graph1 = new MutableBlockStmtGraph();
    graph1.setStartingStmt(stmt1);
    graph1.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    graph1.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph1.addNode(stmt3, Collections.singletonMap(exception1, catchStmt1));

    graph1.putEdge(stmt2, JGotoStmt.BRANCH_IDX, returnStmt);
    graph1.putEdge(stmt3, JGotoStmt.BRANCH_IDX, returnStmt);

    // FIXME:: check if this StmtGraph structure is even valid.. at least the part that is necessary
    // for the test or if the order from Blocks/Stmts in Stmt.Iterator needs to be adapted
    /*{
      final List<Trap> traps = graph1.getTraps();
      final Trap containedTrap = new Trap(exception1, stmt2, catchStmt1, catchStmt2);
      assertTrue(traps.contains(containedTrap));
      assertEquals(2, traps.size());
    }*/

    // test "dont merge exceptional successors" keeping trap split as the traphandler differs
    MutableBlockStmtGraph graph2 = new MutableBlockStmtGraph();
    graph2.setStartingStmt(stmt1);
    graph2.addNode(stmt1, Collections.singletonMap(exception1, catchStmt1));
    graph2.addNode(stmt2, Collections.singletonMap(exception1, catchStmt2));

    graph2.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    assertEquals(4, graph2.getBlocks().size());

    graph2.putEdge(stmt2, JGotoStmt.BRANCH_IDX, returnStmt);
    {
      assertEquals(5, graph2.getBlocks().size());
      final List<Trap> traps = graph2.buildTraps();
      assertEquals(2, traps.size());
    }

    // dont merge as the exceptiontype is different
    MutableBlockStmtGraph graph3 = new MutableBlockStmtGraph();
    graph3.setStartingStmt(stmt1);
    graph3.addNode(stmt1, Collections.singletonMap(exception2, catchStmt1));
    graph3.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph3.addNode(stmt3, Collections.emptyMap());

    graph3.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    graph3.putEdge(stmt2, JGotoStmt.BRANCH_IDX, stmt3);
    graph3.putEdge(stmt3, JGotoStmt.BRANCH_IDX, returnStmt);

    {
      final List<Trap> traps = graph3.buildTraps();
      assertEquals(5, graph2.getBlocks().size());
      assertEquals(2, traps.size());
    }

    // mixed 1
    MutableBlockStmtGraph graph4 = new MutableBlockStmtGraph();
    graph4.setStartingStmt(stmt1);
    graph4.addNode(
        stmt1,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception1, catchStmt1);
            put(exception2, catchStmt1);
          }
        });
    graph4.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph4.addNode(stmt3, Collections.emptyMap());

    graph4.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    graph4.putEdge(stmt2, JGotoStmt.BRANCH_IDX, stmt3);
    graph4.putEdge(stmt3, JGotoStmt.BRANCH_IDX, returnStmt);

    assertEquals(3, graph4.buildTraps().size());

    // mixed 2
    MutableBlockStmtGraph graph5 = new MutableBlockStmtGraph();
    graph5.setStartingStmt(stmt1);
    graph5.addNode(
        stmt1,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception1, catchStmt1);
            put(exception2, catchStmt1);
          }
        });
    graph5.addNode(
        stmt2,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception2, catchStmt1);
            put(exception1, catchStmt2);
          }
        });
    graph5.addNode(
        stmt3,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception2, catchStmt1);
            put(exception1, catchStmt2);
          }
        });

    graph5.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    graph5.putEdge(stmt2, JGotoStmt.BRANCH_IDX, stmt3);
    graph5.putEdge(stmt3, JGotoStmt.BRANCH_IDX, returnStmt);

    {
      final List<Trap> traps = graph5.buildTraps();
      assertEquals(6, traps.size());
      assertEquals(6, graph5.getBlocks().size());
    }

    // mixed 3
    MutableBlockStmtGraph graph6 = new MutableBlockStmtGraph();
    graph6.setStartingStmt(stmt1);
    graph6.addNode(
        stmt1,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception1, catchStmt1);
            put(exception2, catchStmt1);
          }
        });
    graph6.addNode(
        stmt2,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception2, catchStmt1);
            put(exception1, catchStmt2);
          }
        });
    graph6.addNode(
        stmt3,
        new HashMap<ClassType, Stmt>() {
          {
            put(exception1, catchStmt2);
          }
        });

    graph6.putEdge(stmt1, JGotoStmt.BRANCH_IDX, stmt2);
    graph6.putEdge(stmt2, JGotoStmt.BRANCH_IDX, stmt3);
    graph6.putEdge(stmt3, JGotoStmt.BRANCH_IDX, returnStmt);
    {
      final List<Trap> traps = graph6.buildTraps();
      assertEquals(5, traps.size());
      assertEquals(6, graph6.getBlocks().size());
      assertEquals(
          "[Block [goto], Block [goto], Block [goto]]",
          graph6.exceptionalPredecessorBlocks(graph6.getBlockOf(catchStmt1)).toString());
    }

    graph6.removeBlock(graph6.getBlockOf(stmt2));
    assertEquals(5, graph6.getBlocks().size());
  }

  @Test
  public void copyOfImmutable() {
    /*
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.setStartingStmt(stmt1);

    final ImmutableStmtGraph immutableGraph = ImmutableStmtGraph.copyOf(graph);

    assertEquals(graph.getStartingStmt(), immutableGraph.getStartingStmt());
    assertEquals(graph.nodes().size(), immutableGraph.nodes().size());
    assertEquals(
        Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(stmt1, stmt2))),
        immutableGraph.nodes());

    assertEquals(graph.nodes().size(), immutableGraph.nodes().size());
    for (Stmt node : graph.nodes()) {
      assertEquals(graph.predecessors(node), immutableGraph.predecessors(node));
      assertEquals(graph.successors(node), immutableGraph.successors(node));
    }

    try {
      immutableGraph.nodes().add(stmt1);
      assertTrue(false);
    } catch (Exception ignore) {
    }

    try {
      immutableGraph.predecessors(stmt1).add(stmt2);
      assertTrue(false);
    } catch (Exception ignore) {
    }

    try {
      immutableGraph.successors(stmt1).add(stmt2);
      assertTrue(false);
    } catch (Exception ignore) {
    }
     */
  }

  @Test
  public void copyOf() {
    JNopStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JNopStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.setStartingStmt(stmt1);

    final StmtGraph<?> graph2 = new MutableBlockStmtGraph(graph);

    assertEquals(graph.getStartingStmt(), graph2.getStartingStmt());
    assertEquals(graph.getNodes().size(), graph2.getNodes().size());
    assertEquals(
        Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(stmt1, stmt2))),
        graph2.getNodes());

    assertEquals(graph.getNodes().size(), graph2.getNodes().size());
    for (Stmt node : graph.getNodes()) {
      assertEquals(graph.predecessors(node), graph2.predecessors(node));
      assertEquals(graph.successors(node), graph2.successors(node));
    }
  }

  @Test
  public void addNode() {
    Stmt stmt = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt);
    assertEquals(0, graph.inDegree(stmt));
    assertEquals(0, graph.outDegree(stmt));
  }

  @Test
  public void setEdgesSimple() {
    BranchingStmt stmt1 =
        new JIfStmt(
            new JNeExpr(BooleanConstant.getInstance(1), BooleanConstant.getInstance(0)),
            StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.setEdges(stmt1, Arrays.asList(stmt2, stmt3));

    assertTrue(graph.predecessors(stmt2).contains(stmt1));
    assertTrue(graph.successors(stmt1).contains(stmt2));

    assertTrue(graph.predecessors(stmt3).contains(stmt1));
    assertTrue(graph.successors(stmt1).contains(stmt3));

    // order of stmts
    assertEquals(stmt2, graph.successors(stmt1).get(0));
    assertEquals(stmt3, graph.successors(stmt1).get(1));
  }

  @Test
  public void removeNodeWOEdges() {
    Stmt stmt = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt);
    assertTrue(graph.getNodes().contains(stmt));
    graph.removeNode(stmt);
    assertFalse(graph.getNodes().contains(stmt));
  }

  @Test
  public void removeNodeWOPredecessors() {
    FallsThroughStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertTrue(graph.getNodes().contains(stmt1));
    assertEquals(Collections.singletonList(stmt2), graph.successors(stmt1));
    assertEquals(Collections.singletonList(stmt1), graph.predecessors(stmt2));

    graph.removeNode(stmt1);
    assertFalse(graph.getNodes().contains(stmt1));
    assertTrue(graph.predecessors(stmt2).isEmpty());

    try {
      graph.successors(stmt1);
      fail();
    } catch (Exception ignored) {

    }
    assertEquals(Collections.emptyList(), graph.predecessors(stmt2));
  }

  @Test
  public void removeNodeWOSuccessors() {
    FallsThroughStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertTrue(graph.getNodes().contains(stmt2));
    assertEquals(Collections.singletonList(stmt2), graph.successors(stmt1));
    assertEquals(Collections.singletonList(stmt1), graph.predecessors(stmt2));

    graph.removeNode(stmt2);
    assertFalse(graph.getNodes().contains(stmt2));
    try {
      graph.predecessors(stmt2);
      fail();
    } catch (Exception ignored) {

    }
    try {
      graph.successors(stmt2);
      fail();
    } catch (Exception ignored) {

    }
    assertEquals(Collections.emptyList(), graph.successors(stmt1));
  }

  @Test
  public void removeEdge() {
    FallsThroughStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertEquals(1, graph.successors(stmt1).size());
    assertTrue(graph.hasEdgeConnecting(stmt1, stmt2));

    assertEquals(1, graph.removeEdge(stmt1, stmt2).size());
    assertEquals(0, graph.successors(stmt1).size());
    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test
  public void removeEdgeNonExistingEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();

    assertThrows(IllegalArgumentException.class, () -> graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test
  public void testNonExistingEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt1);
    graph.addNode(stmt2);

    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test
  public void removeImpossibleEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    // nodes are not in the graph!
    assertEquals(0, graph.removeEdge(stmt1, stmt2).size());
  }

  @Test
  public void putEdge() {
    FallsThroughStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    // stmt2 is not in the graph!
    graph.putEdge(stmt1, stmt2);
  }

  @Test
  public void simpleInsertion() {

    FallsThroughStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt2 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    FallsThroughStmt stmt3 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt2, stmt3);

    assertEquals(0, graph.inDegree(stmt1));
    assertEquals(1, graph.outDegree(stmt1));
    assertEquals(1, graph.degree(stmt1));

    assertEquals(1, graph.inDegree(stmt2));
    assertEquals(1, graph.outDegree(stmt2));
    assertEquals(2, graph.degree(stmt2));

    assertEquals(1, graph.inDegree(stmt3));
    assertEquals(0, graph.outDegree(stmt3));
    assertEquals(1, graph.degree(stmt3));

    assertTrue(graph.hasEdgeConnecting(stmt1, stmt2));
    assertTrue(graph.hasEdgeConnecting(stmt2, stmt3));

    assertFalse(graph.hasEdgeConnecting(stmt1, stmt1));
    assertFalse(graph.hasEdgeConnecting(stmt2, stmt1));
    assertFalse(graph.hasEdgeConnecting(stmt3, stmt1));
    assertFalse(graph.hasEdgeConnecting(stmt3, stmt2));

    assertEquals(0, graph.predecessors(stmt1).size());
    assertEquals(1, graph.successors(stmt1).size());
    assertTrue(graph.successors(stmt1).contains(stmt2));
  }

  @Test
  public void testRemoveSingleTrap() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    // Create distinct statements and an exception handler
    JReturnVoidStmt stmt1 = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JGotoStmt stmt2 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    Stmt handlerStmt =
        new JIdentityStmt(
            new Local("ex", throwableSig),
            new JCaughtExceptionRef(throwableSig),
            StmtPositionInfo.getNoStmtPositionInfo());

    // Add blocks and starting statement
    graph.addBlock(Collections.singletonList(stmt1)); // Block 1 with return statement
    graph.addBlock(Collections.singletonList(stmt2)); // Block 2 with goto statement
    graph.setStartingStmt(stmt1);

    // Add an exceptional edge, simulating a trap
    graph.addExceptionalEdge(stmt1, throwableSig, handlerStmt);

    // Verify the trap is present
    List<Trap> traps = graph.buildTraps();
    assertEquals(1, traps.size());
    assertEquals(stmt1, traps.get(0).getBeginStmt());
    assertEquals(handlerStmt, traps.get(0).getHandlerStmt());

    // Remove the trap and verify it's removed
    Trap trapToRemove = traps.get(0);
    graph.removeExceptionalFlowFromAllBlocks(
        trapToRemove.getExceptionType(), trapToRemove.getHandlerStmt());
    traps = graph.buildTraps();
    assertEquals(0, traps.size());
  }

  @Test
  public void testRemoveMultipleTrapsWithDifferentExceptionTypes() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    JGotoStmt stmt1 = new JGotoStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt stmt2 = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt stmt3 = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    Stmt handlerStmt1 =
        new JIdentityStmt(
            new Local("ex1", throwableSig),
            new JCaughtExceptionRef(throwableSig),
            StmtPositionInfo.getNoStmtPositionInfo());

    Stmt handlerStmt2 =
        new JIdentityStmt(
            new Local("ex2", ioExceptionSig),
            new JCaughtExceptionRef(ioExceptionSig),
            StmtPositionInfo.getNoStmtPositionInfo());

    // Add blocks and starting statement
    graph.addBlock(Collections.singletonList(stmt1));
    graph.addBlock(Collections.singletonList(stmt2));
    graph.addBlock(Collections.singletonList(stmt3));
    graph.setStartingStmt(stmt1);

    graph.addExceptionalEdge(stmt1, throwableSig, handlerStmt1);
    graph.addExceptionalEdge(stmt2, ioExceptionSig, handlerStmt2);

    // Verify both traps are present
    List<Trap> traps = graph.buildTraps();
    assertEquals(2, traps.size());

    // Remove one trap and verify the remaining
    Trap trapToRemove = traps.get(0);
    Trap trapToKeep = traps.get(1);

    graph.removeExceptionalFlowFromAllBlocks(
        trapToRemove.getExceptionType(), trapToRemove.getHandlerStmt());
    traps = graph.buildTraps();
    assertEquals(1, traps.size());
    assertEquals(stmt2, trapToKeep.getBeginStmt());
    assertEquals(handlerStmt2, trapToKeep.getHandlerStmt());
  }

  @Test
  public void testGetEntrypoints() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    JNopStmt stmt1 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt stmt2 = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    JReturnVoidStmt stmt3 = new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

    Stmt handlerStmt1 =
        new JIdentityStmt(
            new Local("ex1", throwableSig),
            new JCaughtExceptionRef(throwableSig),
            StmtPositionInfo.getNoStmtPositionInfo());

    Stmt handlerStmt2 =
        new JIdentityStmt(
            new Local("ex2", ioExceptionSig),
            new JCaughtExceptionRef(ioExceptionSig),
            StmtPositionInfo.getNoStmtPositionInfo());

    JNopStmt stmt4 = new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());

    // Add blocks and starting statement
    graph.addBlock(Collections.singletonList(stmt1));
    graph.addBlock(Collections.singletonList(stmt2));
    graph.addBlock(Collections.singletonList(stmt3));
    graph.addBlock(Collections.singletonList(stmt4));
    graph.setStartingStmt(stmt1);

    graph.addExceptionalEdge(stmt2, throwableSig, handlerStmt1);
    graph.addExceptionalEdge(stmt3, ioExceptionSig, handlerStmt2);

    Collection<Stmt> entrypoints = graph.getEntrypoints();
    assertEquals(3, entrypoints.size());
    assertTrue(entrypoints.contains(stmt1));
    assertTrue(entrypoints.contains(handlerStmt1));
    assertTrue(entrypoints.contains(handlerStmt2));
  }
}
