package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PostOrderTraversalTest {
  TestGraphGenerator graphGenerator = new TestGraphGenerator();

  @Test
  void testPostOrderTraversal1() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph();
    Map<BasicBlock<?>, Integer> blockToId = new HashMap<>();
    // assign ids according to blocks sorted by BasicBlock::toString
    List<? extends BasicBlock<?>> blocks =
        graph.getBlocks().stream()
            .sorted(Comparator.comparing(BasicBlock::toString))
            .collect(Collectors.toList());
    int i = 0;
    for (BasicBlock<?> block : blocks) {
      blockToId.put(block, i);
      i++;
    }

    PostOrderBlockTraversal traversal = new PostOrderBlockTraversal(graph);
    List<BasicBlock<?>> postOrderBlocks = traversal.getBlocksSorted();
    List<Integer> PO =
        postOrderBlocks.stream().map(b -> blockToId.get(b)).collect(Collectors.toList());

    List<Integer> expectedPO = Arrays.asList(6, 0, 5, 4, 1, 2, 3);
    assertEquals(expectedPO, PO);
  }

  @Test
  void testPostOrderTraversal2() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph2();
    Map<BasicBlock<?>, Integer> blockToId = new HashMap<>();
    Map<BasicBlock<?>, Integer> blockToPOId = new HashMap<>();
    // assign ids according to blocks sorted by BasicBlock::toString
    List<? extends BasicBlock<?>> blocks =
        graph.getBlocks().stream()
            .sorted(Comparator.comparing(BasicBlock::toString))
            .collect(Collectors.toList());
    int i = 0;
    for (BasicBlock<?> block : blocks) {
      blockToId.put(block, i);
      i++;
    }

    PostOrderBlockTraversal traversal = new PostOrderBlockTraversal(graph);
    List<BasicBlock<?>> postOrderBlocks = traversal.getBlocksSorted();
    List<Integer> PO =
        postOrderBlocks.stream().map(b -> blockToId.get(b)).collect(Collectors.toList());

    List<Integer> expectedPO = Arrays.asList(2, 5, 3, 0, 4, 1);
    assertEquals(expectedPO, PO);
  }
}
