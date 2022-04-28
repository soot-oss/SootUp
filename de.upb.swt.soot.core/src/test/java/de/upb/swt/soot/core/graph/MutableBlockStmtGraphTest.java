package de.upb.swt.soot.core.graph;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.JLeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.UnknownType;
import de.upb.swt.soot.core.util.GraphVizExporter;
import java.util.*;
import org.junit.Test;

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
    assertEquals(0, graph.getBlocks().size());
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocks().size());

    // test duplicate insertion of the same node
    graph.addNode(firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().get(0).getStmts().size());

    graph.addNode(secondNop);
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().get(1).getStmts().size());

    graph.removeNode(firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().get(0).getStmts().size());

    // removal of not existing
    graph.removeNode(firstNop);
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
    assertEquals(3, graph.getBlocks().get(0).getStmts().size());

    graph.removeNode(secondNop);
    assertEquals(Arrays.asList(firstNop, thirdNop), graph.getBlocks().get(0).getStmts());
  }

  @Test
  public void removeStmtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(thirdNop);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlocks().get(0).getStmts());
  }

  @Test
  public void removeStmtHead() {
    assertNotEquals(Arrays.asList(firstNop, secondNop), Arrays.asList(firstNop, thirdNop));

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);

    graph.removeNode(firstNop);
    assertEquals(Arrays.asList(secondNop, thirdNop), graph.getBlocks().get(0).getStmts());
  }

  @Test
  public void removeStmtConditionalTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, conditionalStmt);

    graph.removeNode(conditionalStmt);
    assertEquals(Arrays.asList(firstNop, secondNop), graph.getBlocks().get(0).getStmts());
  }

  @Test
  public void testSetEdges() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setEdges(firstNop, Collections.singletonList(conditionalStmt));
    assertEquals(Arrays.asList(firstNop, conditionalStmt), graph.getBlocks().get(0).getStmts());

    graph.setEdges(conditionalStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(3, graph.getBlocks().size());

    assertEquals(Arrays.asList(firstNop, conditionalStmt), graph.getBlocks().get(0).getStmts());
    assertEquals(Collections.singletonList(secondNop), graph.getBlocks().get(1).getStmts());
    assertEquals(Collections.singletonList(thirdNop), graph.getBlocks().get(2).getStmts());
  }

  @Test
  public void removeStmtConditionalTailBetweenBlocks() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.setStartingStmt(firstNop);
    graph.putEdge(firstNop, conditionalStmt);

    graph.setEdges(conditionalStmt, Arrays.asList(secondNop, thirdNop));
    assertEquals(3, graph.getBlocks().size());

    graph.removeNode(conditionalStmt);
    assertEquals(Collections.singletonList(firstNop), graph.getBlocks().get(0).getStmts());
    assertEquals(Collections.singletonList(secondNop), graph.getBlocks().get(1).getStmts());
    assertEquals(Collections.singletonList(thirdNop), graph.getBlocks().get(2).getStmts());
  }

  @Test
  public void modifyStmtToBlockAtTail() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());
    assertEquals(0, graph.nodes().size());

    graph.addNode(firstNop);
    graph.setStartingStmt(firstNop);
    assertEquals(1, graph.nodes().size());
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().get(0).getStmts().size());

    graph.putEdge(firstNop, secondNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(2, graph.nodes().size());

    graph.putEdge(secondNop, thirdNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(3, graph.nodes().size());

    // insert branchingstmt at end
    graph.putEdge(thirdNop, conditionalStmt);
    assertEquals(4, graph.nodes().size());
    assertEquals(1, graph.getBlocks().size());
    assertEquals(0, graph.getBlocks().get(0).getPredecessors().size());
    assertEquals(0, graph.getBlocks().get(0).getSuccessors().size());

    // add connection between branchingstmt and first stmt
    graph.putEdge(conditionalStmt, firstNop);
    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.getBlocks().get(0).getPredecessors().size());
    assertEquals(1, graph.getBlocks().get(0).getSuccessors().size());

    System.out.println(GraphVizExporter.createUrlToWebeditor(graph));

    // add connection between branchingstmt and second stmt
    graph.putEdge(conditionalStmt, secondNop);
    assertEquals(2, graph.getBlocks().size());

    System.out.println(GraphVizExporter.createUrlToWebeditor(graph));

    assertEquals(1, graph.getBlocks().get(0).getStmts().size());
    assertEquals(1, graph.getBlocks().get(0).getPredecessors().size());
    assertEquals(1, graph.getBlocks().get(0).getSuccessors().size());

    assertEquals(3, graph.getBlocks().get(1).getStmts().size());
    assertEquals(2, graph.getBlocks().get(1).getPredecessors().size());
    assertEquals(2, graph.getBlocks().get(1).getSuccessors().size());

    // remove non-existing edge
    graph.removeEdge(firstNop, conditionalStmt);
    assertEquals(2, graph.getBlocks().size());

    // remove branchingstmt at end -> edge across blocks
    graph.removeEdge(conditionalStmt, firstNop);
    assertEquals(2, graph.getBlocks().size());

    assertEquals(4, graph.nodes().size());
    graph.removeNode(firstNop);
    assertEquals(3, graph.nodes().size());

    // remove branchingstmt at head
    graph.removeEdge(conditionalStmt, secondNop);
    assertEquals(2, graph.getBlocks().size());
    assertEquals(3, graph.nodes().size());

    graph.removeNode(secondNop);
    assertEquals(2, graph.nodes().size());
    assertEquals(1, graph.getBlocks().size());
  }

  @Test
  public void removeStmtInBetweenBlock() {

    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    graph.putEdge(firstNop, secondNop);
    graph.putEdge(secondNop, thirdNop);
    graph.removeNode(secondNop);

    assertEquals(graph.getBlocks().get(0).getStmts(), Arrays.asList(firstNop, thirdNop));
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

    assertEquals(2, graph.getBlocks().size());

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

    assertEquals(3, graph.getBlocks().size());
  }

  @Test
  public void addBlockDirectly() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    assertEquals(0, graph.getBlocks().size());

    MutableBasicBlock blockA = new MutableBasicBlock();
    blockA.addStmt(firstNop);
    MutableBasicBlock blockB = new MutableBasicBlock();
    blockB.addStmt(secondNop);
    MutableBasicBlock blockC = new MutableBasicBlock();
    blockC.addStmt(thirdNop);

    graph.addBlock(blockA.getStmts(), Collections.emptyMap());
    assertEquals(1, graph.getBlocks().size());

    graph.addBlock(blockB.getStmts(), Collections.emptyMap());
    assertEquals(2, graph.getBlocks().size());
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

    assertEquals(1, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(1, graph.successors(secondNop).size());

    graph.removeEdge(secondNop, thirdNop);
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(secondNop, thirdNop); // empty operation
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(firstNop, thirdNop); // empty operation
    assertEquals(2, graph.getBlocks().size());
    assertEquals(1, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(secondNop).size());

    graph.removeEdge(firstNop, secondNop);
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
    assertEquals(2, graph.getBlocks().size());
    assertEquals(0, graph.successors(firstNop).size());
    assertEquals(0, graph.successors(thirdNop).size());
    assertEquals(0, graph.predecessors(firstNop).size());
    assertEquals(0, graph.predecessors(thirdNop).size());
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
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(secondNop);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBlockAddStmtInvalidDuplicateStmtObject() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
    block.addStmt(firstNop);
    block.addStmt(firstNop);
    /* possible but is a problem in the graph! */
    graph.addBlock(block.getStmts(), Collections.emptyMap());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraph() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
    graph.addNode(firstNop);
    block.addStmt(firstNop);
    graph.addBlock(block.getStmts(), Collections.emptyMap());
  }

  @Test
  public void testBlockAddStmtInvalidDuplicateStmtObjectViaGraphDirectManiupaltionAfterwards() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
    graph.addNode(firstNop);
    graph.addBlock(block.getStmts(), Collections.emptyMap());
    block.addStmt(firstNop); // BAD! don't do that!
  }

  @Test
  public void testDuplicateStmtsViaDifferentLevelsOfResponsibility() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    MutableBasicBlock block = new MutableBasicBlock();
    graph.addNode(firstNop);
    graph.addBlock(block.getStmts(), Collections.emptyMap());
    block.addStmt(secondNop); // BAD! don't do that!
    graph.addBlock(
        block.getStmts(),
        Collections.emptyMap()); // block is currently ignored if added again and not reindexed!
    assertFalse(graph.containsNode(secondNop));
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
    assertEquals(1, graph.getBlocks().size());
    // graph.addTrap(throwableSig, secondNop, secondNop, firstHandlerStmt);
  }

  @Test
  public void modifyTrapToBeginningOfABlock() {
    fail("implement adding");
    fail("implement removal test");
  }

  @Test
  public void modifyTrapToEndOfABlock() {
    fail("implement adding");
    fail("implement removal test");
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
      assertEquals(1, traps.size());
      assertEquals(stmt1, traps.get(0).getBeginStmt());
      assertEquals(returnStmt, traps.get(0).getEndStmt());
      assertEquals(catchStmt1, traps.get(0).getHandlerStmt());
    }

    // test merging traps from sequential blocks with the same trap
    MutableBlockStmtGraph graph1 = new MutableBlockStmtGraph();
    graph1.setStartingStmt(stmt1);
    graph1.addNode(stmt1, Collections.emptyMap());
    graph1.addNode(stmt2, Collections.singletonMap(exception1, catchStmt1));
    graph1.addNode(stmt3, Collections.singletonMap(exception1, catchStmt1));

    graph1.putEdge(stmt1, stmt2);
    graph1.putEdge(stmt2, returnStmt);

    {
      final List<Trap> traps = graph1.getTraps();
      assertTrue(traps.contains(new Trap(exception1, stmt2, returnStmt, catchStmt1)));
      assertEquals(1, traps.size());
    }

    // test "dont merge exceptional successors" keeping trap split as the traphandler differs
    MutableBlockStmtGraph graph2 = new MutableBlockStmtGraph();
    graph2.setStartingStmt(stmt1);
    graph2.addNode(stmt1, Collections.singletonMap(exception1, catchStmt1));
    graph2.addNode(stmt2, Collections.singletonMap(exception1, catchStmt2));

    graph2.putEdge(stmt1, stmt2);
    graph2.putEdge(stmt2, returnStmt);

    {
      final List<Trap> traps = graph2.getTraps();
      assertTrue(traps.contains(new Trap(exception1, stmt1, stmt2, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception1, stmt2, returnStmt, catchStmt2)));
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
      assertTrue(traps.contains(new Trap(exception2, stmt1, stmt2, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception1, stmt2, stmt3, catchStmt1)));
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

    assertEquals(2, graph4.getTraps().size());

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
      assertTrue(traps.contains(new Trap(exception1, stmt1, stmt2, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception2, stmt1, returnStmt, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception1, stmt2, returnStmt, catchStmt2)));
      assertEquals(3, traps.size());
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
      assertTrue(traps.contains(new Trap(exception1, stmt1, stmt2, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception2, stmt1, stmt3, catchStmt1)));
      assertTrue(traps.contains(new Trap(exception1, stmt2, stmt3, catchStmt2)));
      assertEquals(3, traps.size());
    }
  }
}
