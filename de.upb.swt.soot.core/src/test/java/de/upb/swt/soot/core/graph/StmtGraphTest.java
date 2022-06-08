package de.upb.swt.soot.core.graph;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class StmtGraphTest {

  @Test
  public void copyOfImmutable() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
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
  }

  @Test
  public void copyOf() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.setStartingStmt(stmt1);

    final StmtGraph graph2 = new MutableStmtGraph(graph);

    assertEquals(graph.getStartingStmt(), graph2.getStartingStmt());
    assertEquals(graph.nodes().size(), graph2.nodes().size());
    assertEquals(
        Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(stmt1, stmt2))),
        graph2.nodes());

    assertEquals(graph.nodes().size(), graph2.nodes().size());
    for (Stmt node : graph.nodes()) {
      assertEquals(graph.predecessors(node), graph2.predecessors(node));
      assertEquals(graph.successors(node), graph2.successors(node));
    }
  }

  @Test(
      expected = RuntimeException.class) // Will get exception since stmt is not added in the graph
  public void addNode() {
    Stmt stmt = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    assertEquals(0, graph.inDegree(stmt));
    assertEquals(0, graph.outDegree(stmt));
  }

  @Test
  public void setEdgesSimple() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableStmtGraph();
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
  public void setEdgesReplacing() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt4 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt5 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableStmtGraph();
    graph.putEdge(stmt1, stmt2);
    graph.putEdge(stmt1, stmt3);

    graph.setEdges(stmt1, Arrays.asList(stmt4, stmt5));
    assertTrue(graph.predecessors(stmt4).contains(stmt1));
    assertTrue(graph.successors(stmt1).contains(stmt4));

    assertTrue(graph.predecessors(stmt5).contains(stmt1));
    assertTrue(graph.successors(stmt1).contains(stmt5));

    assertFalse(graph.predecessors(stmt2).contains(stmt1));
    assertFalse(graph.successors(stmt1).contains(stmt2));

    assertFalse(graph.predecessors(stmt3).contains(stmt1));
    assertFalse(graph.successors(stmt1).contains(stmt3));
  }

  @Test
  public void removeNodeWOEdges() {
    Stmt stmt = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    // assertTrue(graph.nodes().contains(stmt)); // This assertion will fail since stmt is not
    // present in the graph
    assertFalse(graph.nodes().contains(stmt));
  }

  @Test
  public void removeNodeWOPredecessors() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertTrue(graph.nodes().contains(stmt1));
    assertEquals(Collections.singletonList(stmt2), graph.successors(stmt1));
    assertEquals(Collections.singletonList(stmt1), graph.predecessors(stmt2));

    // assertFalse(graph.nodes().contains(stmt1)); //This assertion will fail since stmt2 node is
    // added by call to putEdge() method

    graph.removeNode(stmt1); // removing node without predecessor

    // try { //Commenting try catch since no exception will be generated
    graph.predecessors(stmt2);
    // fail(); // predecessors() method returns an empty list or list of statements which are
    // predecessors
    // } catch (Exception ignored) {}

    // try {//Commenting try catch since no exception will be generated
    // graph.successors(stmt1);
    // fail(); // successors() method returns an empty list or list of statements which are
    // successors
    // } catch (Exception ignored) {}
    assertEquals(Collections.emptyList(), graph.predecessors(stmt2));
  }

  @Test
  public void removeNodeWOSuccessors() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertTrue(graph.nodes().contains(stmt2));
    assertEquals(Collections.singletonList(stmt2), graph.successors(stmt1));
    assertEquals(Collections.singletonList(stmt1), graph.predecessors(stmt2));

    // assertFalse(graph.nodes().contains(stmt2)); //This assertion will fail since stmt2 node is
    // added by call to putEdge() method

    graph.removeNode(stmt2); // removing node without predecessor

    // try {//Commenting try catch since no exception will be generated
    // graph.predecessors(stmt2); // stmt2 is removed
    // fail(); // predecessors() method returns an empty list or list of statements which are
    // predecessors
    // } catch (Exception ignored) {    }

    // try {//Commenting try catch since no exception will be generated
    // graph.successors(stmt2);
    // fail(); // successors() method returns an empty list or list of statements which are
    // successors
    // } catch (Exception ignored) {    }

    assertEquals(Collections.emptyList(), graph.successors(stmt1));
  }

  @Test(
      expected =
          RuntimeException
              .class) // Will get exception since stmt1 and stmt2 nodes are not present in the graph
  // after removing the edge
  public void removeEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.putEdge(stmt1, stmt2);

    assertEquals(1, graph.successors(stmt1).size());
    assertTrue(graph.hasEdgeConnecting(stmt1, stmt2));

    graph.removeEdge(stmt1, stmt2);
    assertEquals(0, graph.successors(stmt1).size());
    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test(
      expected =
          RuntimeException
              .class) // Will get exception since stmt1 and stmt2 nodes are not present in the graph
  public void removeEdgeNonExistingEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();

    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));

    graph.removeEdge(stmt1, stmt2);
    assertFalse(graph.hasEdgeConnecting(stmt1, stmt2));
  }

  @Test(expected = RuntimeException.class)
  public void removeImpossibleEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    // nodes are not in the graph!
    graph.removeEdge(stmt1, stmt2);
  }

  @Test // (expected = RuntimeException.class) // Runtime exception will not be generated since
  // getNodeIdxOrCreate() method is used.
  public void putImpossibleEdge() {
    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    MutableStmtGraph graph = new MutableStmtGraph();
    // stmt2 is not in the graph! But getNodeIdxOrCreate() method will create the node for
    // statements not present in the graph
    graph.putEdge(stmt1, stmt2);
  }

  @Test
  public void simpleInsertion() {

    Stmt stmt1 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt2 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    Stmt stmt3 = new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo());

    MutableStmtGraph graph = new MutableStmtGraph();
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
