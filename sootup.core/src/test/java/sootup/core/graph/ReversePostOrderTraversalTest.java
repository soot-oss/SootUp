package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Java8")
public class ReversePostOrderTraversalTest {

  TestGraphGenerator graphGenerator = new TestGraphGenerator();

  @Test
  void testReversePostOrderTraversal1() {
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

    List<BasicBlock<?>> reversePostOrderBlocks =
        ReversePostOrderBlockTraversal.getBlocksSorted(graph);
    List<Integer> RPO =
        reversePostOrderBlocks.stream().map(b -> blockToId.get(b)).collect(Collectors.toList());

    Integer[] arr = new Integer[] {3, 2, 1, 4, 5, 0, 6};
    List<Integer> expectedRPO = Arrays.asList(arr);
    assertEquals(expectedRPO, RPO);
  }

  @Test
  void testReversePostOrderTraversal2() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph2();
    Map<BasicBlock<?>, Integer> blocksToId = new HashMap<>();
    Map<BasicBlock<?>, Integer> blockToRPOId = new HashMap<>();
    // assign ids according to blocks sorted by BasicBlock::toString
    List<? extends BasicBlock<?>> blocks =
        graph.getBlocks().stream()
            .sorted(Comparator.comparing(BasicBlock::toString))
            .collect(Collectors.toList());
    int i = 0;
    for (BasicBlock<?> block : blocks) {
      blocksToId.put(block, i);
      i++;
    }

    List<BasicBlock<?>> reversePostOrderBlocks =
        ReversePostOrderBlockTraversal.getBlocksSorted(graph);
    List<Integer> RPO =
        reversePostOrderBlocks.stream().map(b -> blocksToId.get(b)).collect(Collectors.toList());

    Integer[] arr = new Integer[] {1, 4, 0, 3, 5, 2};
    List<Integer> expectedRPO = Arrays.asList(arr);

    assertEquals(expectedRPO, RPO);
  }
}
