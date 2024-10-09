package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Java8")
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

    List<BasicBlock<?>> postOrderBlocks = PostOrderBlockTraversal.getBlocksSorted(graph);
    List<Integer> PO =
        postOrderBlocks.stream().map(b -> blockToId.get(b)).collect(Collectors.toList());

    Integer[] arr = new Integer[] {6, 0, 5, 4, 1, 2, 3};
    List<Integer> expectedPO = Arrays.asList(arr);
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

    List<BasicBlock<?>> postOrderBlocks = PostOrderBlockTraversal.getBlocksSorted(graph);
    List<Integer> PO =
        postOrderBlocks.stream().map(b -> blockToId.get(b)).collect(Collectors.toList());

    Integer[] arr = new Integer[] {2, 5, 3, 0, 4, 1};
    List<Integer> expectedPO = Arrays.asList(arr);
    assertEquals(expectedPO, PO);
  }
}
