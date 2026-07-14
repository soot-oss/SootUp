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

    List<Integer> expectedDoms = Arrays.asList(-1, 0, 1, 1, 3, 3, 3);
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

    // RPO: A(0) B(1) C(2) D(3) E(4) F(5) G(6)
    List<Set<BasicBlock<?>>> expectedDFList = new ArrayList<>();
    expectedDFList.add(Collections.emptySet()); // A:  DF={}
    expectedDFList.add(Collections.singleton(blocks.get(1))); // B:  DF={B}
    expectedDFList.add(Collections.emptySet()); // C:  DF={}
    expectedDFList.add(Collections.singleton(blocks.get(1))); // D:  DF={B}
    expectedDFList.add(Collections.singleton(blocks.get(6))); // E:  DF={G}
    expectedDFList.add(Collections.singleton(blocks.get(6))); // F:  DF={G}
    expectedDFList.add(Collections.singleton(blocks.get(1))); // G:  DF={B}

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

    // RPO: B0(0) B2(1) B5(2) B4(3) B3(4) B1(5)
    List<Set<BasicBlock<?>>> expectedDFList = new ArrayList<>();
    expectedDFList.add(Collections.emptySet()); // B0: DF={}
    expectedDFList.add(
        new HashSet<>(Arrays.asList(blocks.get(2), blocks.get(3)))); // B2: DF={B5,B4}
    expectedDFList.add(Collections.singleton(blocks.get(3))); // B5: DF={B4}
    expectedDFList.add(
        new HashSet<>(Arrays.asList(blocks.get(4), blocks.get(2)))); // B4: DF={B3,B5}
    expectedDFList.add(Collections.singleton(blocks.get(3))); // B3: DF={B4}
    expectedDFList.add(Collections.singleton(blocks.get(4))); // B1: DF={B3}

    assertEquals(expectedDFList, dfList);
  }
}
