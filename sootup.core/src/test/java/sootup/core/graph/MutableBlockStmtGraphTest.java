package sootup.core.graph;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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

@Category(Java8Test.class)
public class MutableBlockStmtGraphTest {

  Stmt firstNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt secondNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
  Stmt thirdNop = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

  BranchingStmt conditionalStmt =
      new JIfStmt(
          new JLeExpr(IntConstant.getInstance(2), IntConstant.getInstance(3)),
          StmtPositionInfo.createNoStmtPositionInfo());

  private ClassType throwableSig =
      new ClassType() {
        @Override
        public boolean isBuiltInClass() {
          return true;
        }

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
        public boolean isBuiltInClass() {
          return true;
        }

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
      new JIdentityStmt<>(
          new Local("ex", throwableSig),
          new JCaughtExceptionRef(throwableSig),
          StmtPositionInfo.createNoStmtPositionInfo());
  Stmt secondHandlerStmt =
      new JIdentityStmt<>(
          new Local("ex2", throwableSig),
          new JCaughtExceptionRef(ioExceptionSig),
          StmtPositionInfo.createNoStmtPositionInfo());

  @Test
  public void addNodeTest() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocksSorted().size());
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocksSorted().size());

    // test duplicate insertion of the same node
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.getBlocksSorted().get(0).getStmts().size());

    graph.addNode(secondNop);
    assertEquals(2, graph.getBlocksSorted().size());
    assertEquals(1, graph.getBlocksSorted().get(1).getStmts().size());

    graph.removeNode(firstNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.getBlocksSorted().get(0).getStmts().size());

    // removal of not existing
    try {
      graph.removeNode(firstNop);
      fail("should not be reachable due to exception");
    } catch (Exception ignored) {
    }
    assertEquals(1, graph.getBlocksSorted().size());

    graph.removeNode(secondNop);
    assertEquals(0, graph.getBlocksSorted().size());
  }

  @Test
  public void removeStmtBetweenEdges() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    assertEquals(3, graph.getBlocksSorted().get(0).getStmts().size());

    graph.removeNode(secondNop);
    assertEquals(Arrays.asList(firstNop, thirdNop), graph.getBlocksSorted().get(0).getStmts());
  }

  @Test
  public void removeStmtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(thirdNop);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlocksSorted().get(0).getStmts());
  }

  @Test
  public void removeStmtHead() {
    assertNotEquals(Arrays.asList(firstNop, secondNop), Arrays.asList(firstNop, thirdNop));

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(firstNop);
    assertEquals(Arrays.asList(secondNop, thirdNop), graph.getBlocksSorted().get(0).getStmts());
  }

  @Test
  public void removeStmtConditionalTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, conditionalStmt);

    graph.removeNode(conditionalStmt);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlocksSorted().get(0).getStmts());
  }

  @Test
  public void testSetEdges() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.setEdges(firstNop, Collections.singletonList(conditionalStmt));
    assertEquals(
        Arrays.asList(firstNop, conditionalStmt), graph.getBlocksSorted().get(0).getStmts());

    graph.setEdges(conditionalStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(3, graph.getBlocksSorted().size());

    assertEquals(
        Arrays.asList(firstNop, conditionalStmt).toString(),
        graph.getBlockOf(firstNop).getStmts().toString());
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
    graph.putEdge(firstNop, conditionalStmt);

    graph.setEdges(conditionalStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(3, graph.getBlocksSorted().size());

    graph.removeNode(conditionalStmt);
    final List<? extends BasicBlock<?>> blocksSorted = graph.getBlocksSorted();
    assertEquals(
        Collections.singletonList(firstNop).toString(), blocksSorted.get(0).getStmts().toString());
    assertEquals(
        Collections.singletonList(secondNop).toString(), blocksSorted.get(1).getStmts().toString());
    assertEquals(
        Collections.singletonList(thirdNop).toString(), blocksSorted.get(2).getStmts().toString());
  }

  public void modifyStmtToBlockAtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocksSorted().size());
    assertEquals(0, graph.getNodes().size());

    graph.addNode(firstNop);
    graph.setStartingStmt(firstNop);
    assertEquals(1, graph.getNodes().size());
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.getBlocksSorted().get(0).getStmts().size());

    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(2, graph.getNodes().size());

    graph.putEdge(secondNop, thirdNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(3, graph.getNodes().size());

    // insert branchingstmt at end
    graph.putEdge(thirdNop, conditionalStmt);
    assertEquals(4, graph.getNodes().size());
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(0, graph.getBlocksSorted().get(0).getPredecessors().size());
    assertEquals(0, graph.getBlocksSorted().get(0).getSuccessors().size());

    // add connection between branchingstmt and first stmt
    graph.putEdge(conditionalStmt, firstNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.getBlocksSorted().get(0).getPredecessors().size());
    assertEquals(1, graph.getBlocksSorted().get(0).getSuccessors().size());

    // add connection between branchingstmt and second stmt
    graph.putEdge(conditionalStmt, secondNop);
    assertEquals(2, graph.getBlocksSorted().size());

    assertEquals(3, graph.getBlocksSorted().get(0).getStmts().size());
    assertEquals(2, graph.getBlocksSorted().get(0).getPredecessors().size());
    assertEquals(2, graph.getBlocksSorted().get(0).getSuccessors().size());

    assertEquals(1, graph.getBlocksSorted().get(1).getStmts().size());
    assertEquals(1, graph.getBlocksSorted().get(1).getPredecessors().size());
    assertEquals(1, graph.getBlocksSorted().get(1).getSuccessors().size());

    // remove non-existing edge
    graph.removeEdge(firstNop, conditionalStmt);
    assertEquals(2, graph.getBlocksSorted().size());

    // remove branchingstmt at end -> edge across blocks
    graph.removeEdge(conditionalStmt, firstNop);
    assertEquals(2, graph.getBlocksSorted().size());

    assertEquals(4, graph.getNodes().size());
    graph.removeNode(firstNop);
    assertEquals(3, graph.getNodes().size());

    // remove branchingstmt at head
    graph.removeEdge(conditionalStmt, secondNop);
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(3, graph.getNodes().size());

    graph.removeNode(secondNop);
    assertEquals(2, graph.getNodes().size());
    assertEquals(1, graph.getBlocksSorted().size());
  }

  @Test
  public void removeStmtInBetweenBlock() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(secondNop);

    assertEquals(graph.getBlocksSorted().get(0).getStmts(), Arrays.asList(firstNop, thirdNop));
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

  @Test(expected = IllegalArgumentException.class)
  public void addBadSuccessorCount() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(firstNop, thirdNop);
  }

  @Test(expected = IllegalArgumentException.class)
  public void addDuplicateBadSuccessorCount() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(firstNop, secondNop);
  }

  @Test
  public void addMultipleBranchingEdgesToSameTarget() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(conditionalStmt, secondNop);
    graph.putEdge(conditionalStmt, secondNop);
    assertEquals(2, graph.successors(conditionalStmt).size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void addMultipleBranchingEdgesToSameTargetBAdCount() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(conditionalStmt, secondNop);
    graph.putEdge(conditionalStmt, secondNop);
    graph.putEdge(conditionalStmt, secondNop);
  }

  @Test
  public void addSameSuccessorMultipleTimes() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(conditionalStmt, secondNop);
    graph.putEdge(conditionalStmt, secondNop);

    assertEquals(2, graph.getBlocksSorted().size());

    assertEquals(0, graph.outDegree(secondNop));
    assertEquals(2, graph.inDegree(secondNop));
    assertEquals(Arrays.asList(conditionalStmt, conditionalStmt), graph.predecessors(secondNop));
    assertEquals(2, graph.outDegree(conditionalStmt));
    assertEquals(Arrays.asList(secondNop, secondNop), graph.successors(conditionalStmt));
    assertTrue(graph.hasEdgeConnecting(conditionalStmt, secondNop));
    assertFalse(graph.hasEdgeConnecting(secondNop, conditionalStmt));
  }

  @Test
  public void addBlocks() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, conditionalStmt);
    graph.putEdge(conditionalStmt, secondNop);
    graph.putEdge(conditionalStmt, thirdNop);

    assertEquals(3, graph.getBlocksSorted().size());
  }

  @Test
  public void addBlockDirectly() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocksSorted().size());

    MutableBasicBlock blockA = new MutableBasicBlock();
    blockA.addStmt(firstNop);
    MutableBasicBlock blockB = new MutableBasicBlock();
    blockB.addStmt(secondNop);
    MutableBasicBlock blockC = new MutableBasicBlock();
    blockC.addStmt(thirdNop);

    graph.addBlock(blockA.getStmts(), Collections.emptyMap());
    assertEquals(1, graph.getBlocksSorted().size());

    graph.addBlock(blockB.getStmts(), Collections.emptyMap());
    assertEquals(2, graph.getBlocksSorted().size());
  }

  @Test
  public void linkDirectlyAddedBlocks() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock blockA = new MutableBasicBlock();
    blockA.addStmt(firstNop);
    MutableBasicBlock blockB = new MutableBasicBlock();
    blockB.addStmt(secondNop);
    MutableBasicBlock blockC = new MutableBasicBlock();
    blockC.addStmt(thirdNop);

    graph.addBlock(blockA.getStmts(), Collections.emptyMap());
    graph.addBlock(blockB.getStmts(), Collections.emptyMap());
    graph.addBlock(blockC.getStmts(), Collections.emptyMap());

    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(1, graph.successors(secondNop).size());

    graph.removeEdge(secondNop, thirdNop);
    assertEquals(2, graph.getBlocksSorted().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(secondNop, thirdNop); // empty operation
    assertEquals(2, graph.getBlocksSorted().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(firstNop, thirdNop); // empty operation
    assertEquals(2, graph.getBlocksSorted().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(firstNop, secondNop);
    assertEquals(3, graph.getBlocksSorted().size());
  }

  @Test
  public void testRemoveNodeAtBeginning() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(firstNop);
    assertEquals(1, graph.getBlocksSorted().size());
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
    assertEquals(1, graph.getBlocksSorted().size());
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
    assertEquals(1, graph.getBlocksSorted().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());
    assertEquals(0, graph.predecessors(firstNop).size());
    assertEquals(1, graph.predecessors(secondNop).size());
  }

  @Test
  public void testBlockAddStmt() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
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

  @Test(expected = IllegalArgumentException.class)
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraph() {
    // firstnop already has a successor and its impossible to add another edge
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.addBlock(Arrays.asList(firstNop, thirdNop), Collections.emptyMap());
  }

  @Ignore
  public void testBlockAddStmtDuplicateStmtMaybePossibleInTheFutureButNotImplementedThatWayYet() {
    // firstnop already has a successor and its impossible to add another edge
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.addBlock(Arrays.asList(firstNop), Collections.emptyMap());
  }

  @Test
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraphDirectManiupaltionAfterwards() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
    graph.addNode(firstNop);
    graph.addBlock(block.getStmts(), Collections.emptyMap());
    block.addStmt(firstNop); // BAD! don't do that!
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBlockStmtValidity() {
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(conditionalStmt);
    block.addStmt(firstNop);
  }

  @Test
  public void modifyTrapToCompleteBlock() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocksSorted().size());
    // graph.addTrap(throwableSig, secondNop, secondNop, firstHandlerStmt);
  }

  @Test
  public void testTrapAggregation() {
    final ClassType exception1 =
        new ClassType() {
          @Override
          public boolean isBuiltInClass() {
            return false;
          }

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
          public boolean isBuiltInClass() {
            return false;
          }

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
        new JIdentityStmt<>(
            exc,
            new JCaughtExceptionRef(UnknownType.getInstance()),
            StmtPositionInfo.createNoStmtPositionInfo());
    Stmt catchStmt2 =
        new JIdentityStmt<>(
            exc,
            new JCaughtExceptionRef(PrimitiveType.getInt()),
            StmtPositionInfo.createNoStmtPositionInfo());
    final JReturnVoidStmt returnStmt =
        new JReturnVoidStmt(StmtPositionInfo.createNoStmtPositionInfo());

    final JGotoStmt stmt1 = new JGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
    final JGotoStmt stmt2 = new JGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
    final JGotoStmt stmt3 = new JGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());

    // test same trap merging simple
    MutableBlockStmtGraph graph0 = new MutableBlockStmtGraph();
    graph0.setStartingStmt(stmt1);
    graph0.addNode(stmt1, Collections.singletonMap(exception1, catchStmt1));
    graph0.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));

    graph0.putEdge(stmt1, stmt2);
    graph0.putEdge(stmt2, returnStmt);

    {
      final List<Trap> traps = graph0.getTraps();
      assertEquals(2, traps.size()); // as @caughtexception gets currently in their way.
      assertEquals(stmt2, traps.get(1).getBeginStmt());
      assertEquals(returnStmt, traps.get(1).getEndStmt());
      assertEquals(catchStmt1, traps.get(1).getHandlerStmt());
    }

    // test merging traps from sequential blocks with the same trap
    MutableBlockStmtGraph graph1 = new MutableBlockStmtGraph();
    graph1.setStartingStmt(stmt1);
    graph1.putEdge(stmt1, stmt2);
    graph1.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph1.addNode(stmt3, Collections.singletonMap(exception1, catchStmt1));

    graph1.putEdge(stmt2, returnStmt);
    graph1.putEdge(stmt3, returnStmt);
    graph1.putEdge(catchStmt1, stmt3);

    {
      final List<Trap> traps = graph1.getTraps();
      final Trap containedTrap = new Trap(exception1, stmt2, catchStmt1, catchStmt1);
      assertTrue(traps.contains(containedTrap));
      assertEquals(2, traps.size());
    }

    // test "dont merge exceptional successors" keeping trap split as the traphandler differs
    MutableBlockStmtGraph graph2 = new MutableBlockStmtGraph();
    graph2.setStartingStmt(stmt1);
    graph2.addNode(stmt1, Collections.singletonMap(exception1, catchStmt1));
    graph2.addNode(stmt2, Collections.singletonMap(exception1, catchStmt2));

    graph2.putEdge(stmt1, stmt2);
    assertEquals(4, graph2.getBlocks().size());

    graph2.putEdge(stmt2, returnStmt);
    {
      assertEquals(5, graph2.getBlocks().size());
      final List<Trap> traps = graph2.getTraps();
      assertEquals(2, traps.size());
    }

    // dont merge as the exceptiontype is different
    MutableBlockStmtGraph graph3 = new MutableBlockStmtGraph();
    graph3.setStartingStmt(stmt1);
    graph3.addNode(stmt1, Collections.singletonMap(exception2, catchStmt1));
    graph3.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph3.addNode(stmt3, Collections.emptyMap());

    graph3.putEdge(stmt1, stmt2);
    graph3.putEdge(stmt2, stmt3);
    graph3.putEdge(stmt3, returnStmt);

    {
      final List<Trap> traps = graph3.getTraps();
      assertEquals(5, graph2.getBlocks().size());
      System.out.println(traps);
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

    graph4.putEdge(stmt1, stmt2);
    graph4.putEdge(stmt2, stmt3);
    graph4.putEdge(stmt3, returnStmt);

    assertEquals(3, graph4.getTraps().size());

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

    graph5.putEdge(stmt1, stmt2);
    graph5.putEdge(stmt2, stmt3);
    graph5.putEdge(stmt3, returnStmt);

    {
      final List<Trap> traps = graph5.getTraps();
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

    graph6.putEdge(stmt1, stmt2);
    graph6.putEdge(stmt2, stmt3);
    graph6.putEdge(stmt3, returnStmt);
    {
      final List<Trap> traps = graph6.getTraps();
      assertEquals(5, traps.size());
      assertEquals(6, graph6.getBlocks().size());
    }
  }

  @Test
  public void copyOfImmutable() {
    /*
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
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
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
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
    Stmt stmt = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt);
    assertEquals(0, graph.inDegree(stmt));
    assertEquals(0, graph.outDegree(stmt));
  }

  @Test
  public void setEdgesSimple() {
    Stmt stmt1 =
        new JIfStmt(
            new JNeExpr(BooleanConstant.getInstance(1), BooleanConstant.getInstance(0)),
            StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

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

  @Test(expected = IllegalArgumentException.class)
  public void setEdgesReplacing() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt1, stmt3);
  }

  @Test
  public void removeNodeWOEdges() {
    Stmt stmt = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt);
    assertTrue(graph.getNodes().contains(stmt));
    graph.removeNode(stmt);
    assertFalse(graph.getNodes().contains(stmt));
  }

  @Test
  public void removeNodeWOPredecessors() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
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
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
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
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertEquals(1, graph.successors(stmt1).size());
    assertTrue(graph.hasEdgeConnecting(stmt1, stmt2));

    graph.removeEdge(stmt1, stmt2);
    assertEquals(0, graph.successors(stmt1).size());
    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void removeEdgeNonExistingEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();

    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test
  public void testNonExistingEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    graph.addNode(stmt1);
    graph.addNode(stmt2);

    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test
  public void removeImpossibleEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    // nodes are not in the graph!
    graph.removeEdge(stmt1, stmt2);
  }

  @Test
  public void putEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableBlockStmtGraph();
    // stmt2 is not in the graph!
    graph.putEdge(stmt1, stmt2);
  }

  @Test
  public void simpleInsertion() {

    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

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
}
