package de.upb.swt.soot.test.callgraph.spark.propagater;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.FieldReferenceNode;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.callgraph.spark.pag.nodes.VariableNode;
import de.upb.swt.soot.callgraph.spark.solver.AliasPropagator;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class AliasPropagatorTest extends PropagatorTest {

  @Test
  public void test() {

    PointerAssignmentGraph pag = buildPAG("propagator.PropTest");
    AliasPropagator propagator = new AliasPropagator(pag);
    propagator.propagate();
    pag = propagator.getPag();
    for (VariableNode node : pag.getVariableNodes()) {
      if (node.toString().contains("$r4")) {
        Assert.assertEquals(2, node.getPointsToSet().size());
        for (Node an : node.getPointsToSet()) {
          Assert.assertTrue(an.toString().contains("benchmark.objects.B"));
        }
      }
      if (node.toString().contains("$u0")) {
        Assert.assertEquals(2, node.getPointsToSet().size());
        for (Node an : node.getPointsToSet()) {
          Assert.assertTrue(an.toString().contains("benchmark.objects.O"));
        }
      }
    }

    for (Set<FieldReferenceNode> frSet : pag.getStoreEdges().values()) {
      for (FieldReferenceNode frNode : frSet) {
        if (frNode.toString().contains("$u0")) {
          Assert.assertEquals(1, frNode.getPointsToSet().size());
          for (Node an : frNode.getPointsToSet()) {
            Assert.assertTrue(an.toString().contains("benchmark.objects.B"));
          }
        }
        if (frNode.toString().contains("r0")
            && frNode.toString().contains("benchmark.objects.B f")) {
          Assert.assertEquals(1, frNode.getPointsToSet().size());
          for (Node an : frNode.getPointsToSet()) {
            Assert.assertTrue(an.toString().contains("benchmark.objects.B"));
          }
        }
        if (frNode.toString().contains("r0")
            && frNode.toString().contains("benchmark.objects.B g")) {
          Assert.assertEquals(1, frNode.getPointsToSet().size());
          for (Node an : frNode.getPointsToSet()) {
            Assert.assertTrue(an.toString().contains("benchmark.objects.B"));
          }
        }
      }
    }
  }
}
