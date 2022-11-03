package de.upb.swt.soot.test.callgraph.spark.propagater;

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.pag.nodes.*;
import de.upb.swt.soot.callgraph.spark.solver.CyclePropagator;
import de.upb.swt.soot.core.model.Field;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class CyclePropagatorTest extends PropagatorTest {
  @Test
  public void test() {

    PointerAssignmentGraph pag = buildPAG("propagator.PropTest");
    CyclePropagator propagator = new CyclePropagator(pag);
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
        Assert.assertEquals(0, frNode.getPointsToSet().size());
      }
    }

    for (AllocationNode allocNode : pag.getAllocationEdges().keySet()) {
      if (allocNode.toString().contains("newExpr=new benchmark.objects.O")) {
        Map<Field, AllocationDotField> fieldMap = allocNode.getFields();
        Assert.assertEquals(2, fieldMap.size());
        for (AllocationDotField anDotField : fieldMap.values()) {
          if (anDotField.toString().contains("benchmark.objects.B f")) {
            Assert.assertEquals(2, anDotField.getPointsToSet().size());
          } else if (anDotField.toString().contains("benchmark.objects.B g")) {
            Assert.assertEquals(1, anDotField.getPointsToSet().size());
          }
        }
      } else if (allocNode.toString().contains("newExpr=STRING_ARRAY_NODE")) {
        Map<Field, AllocationDotField> fieldMap = allocNode.getFields();
        Assert.assertEquals(1, fieldMap.size());
        for (AllocationDotField anDotField : fieldMap.values()) {
          Assert.assertEquals(1, anDotField.getPointsToSet().size());
        }
      } else {
        Assert.assertNull(allocNode.getFields());
      }
    }
  }
}
