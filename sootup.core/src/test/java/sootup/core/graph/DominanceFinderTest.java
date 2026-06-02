package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class DominanceFinderTest {

  TestGraphGenerator graphGenerator = new TestGraphGenerator();

  @Test
  public void testDominanceFinder() {
    MutableBlockControlFlowGraph graph = graphGenerator.createControlFlowGraph();
    DominanceFinder dominanceFinder = new DominanceFinder(graph);

    int[] domsArr = dominanceFinder.getImmediateDominators();
    List<Integer> doms = Arrays.stream(domsArr).boxed().collect(Collectors.toList());

    List<Integer> expectedDoms = Arrays.asList(-1, 0, 1, 2, 2, 2, 1);
    assertEquals(expectedDoms, doms);
  }

  @Test
  public void testDominanceFrontiers() {
    MutableBlockControlFlowGraph graph = graphGenerator.createControlFlowGraph();
    DominanceFinder dominanceFinder = new DominanceFinder(graph);

    List<BasicBlock<?>> blocks = dominanceFinder.getIdxToBlock();
    List<Set<BasicBlock<?>>> dfList =
        blocks.stream()
            .map(block -> dominanceFinder.getDominanceFrontiers(block))
            .collect(Collectors.toList());

    // create expectedDFList
    List<Set<BasicBlock<?>>> expectedDFList = new ArrayList<>();
    expectedDFList.add(Collections.emptySet());
    expectedDFList.add(Collections.singleton(blocks.get(1)));
    expectedDFList.add(Collections.singleton(blocks.get(1)));
    expectedDFList.add(Collections.singleton(blocks.get(5)));
    expectedDFList.add(Collections.singleton(blocks.get(5)));
    expectedDFList.add(Collections.singleton(blocks.get(1)));
    expectedDFList.add(Collections.emptySet());

    assertEquals(expectedDFList, dfList);
  }

  @Test
  public void testBlockToIdxInverse() {
    MutableBlockControlFlowGraph graph = graphGenerator.createControlFlowGraph();
    DominanceFinder dom = new DominanceFinder(graph);

    // check that getBlockToIdx and getIdxToBlock are inverses
    for (BasicBlock<?> block : graph.getBlocks()) {
      List<BasicBlock<?>> idxToBlock = dom.getIdxToBlock();
      Map<BasicBlock<?>, Integer> blockToIdx = dom.getBlockToIdx();
      assertEquals(block, idxToBlock.get(blockToIdx.get(block)));
    }
  }

  @Test
  public void testDominanceFinder2() {
    MutableBlockControlFlowGraph graph = graphGenerator.createControlFlowGraph2();
    DominanceFinder dominanceFinder = new DominanceFinder(graph);

    int[] domsArr = dominanceFinder.getImmediateDominators();
    List<Integer> doms = Arrays.stream(domsArr).boxed().collect(Collectors.toList());

    List<Integer> expectedDoms = Arrays.asList(-1, 0, 0, 0, 0, 0);

    assertEquals(expectedDoms, doms);
  }

  @Test
  public void testDominanceFrontiers2() {
    MutableBlockControlFlowGraph graph = graphGenerator.createControlFlowGraph2();
    DominanceFinder dominanceFinder = new DominanceFinder(graph);

    List<BasicBlock<?>> blocks = dominanceFinder.getIdxToBlock();
    List<Set<BasicBlock<?>>> dfList =
        blocks.stream()
            .map(block -> dominanceFinder.getDominanceFrontiers(block))
            .collect(Collectors.toList());

    // create expectedDFList
    List<Set<BasicBlock<?>>> expectedDFList = new ArrayList<>();
    expectedDFList.add(Collections.emptySet());
    expectedDFList.add(Collections.singleton(blocks.get(5)));
    expectedDFList.add(new HashSet<>(Arrays.asList(blocks.get(3), blocks.get(4))));
    expectedDFList.add(Collections.singleton(blocks.get(4)));
    expectedDFList.add(new HashSet<>(Arrays.asList(blocks.get(3), blocks.get(5))));
    expectedDFList.add(Collections.singleton(blocks.get(4)));

    assertEquals(expectedDFList, dfList);
  }
}
