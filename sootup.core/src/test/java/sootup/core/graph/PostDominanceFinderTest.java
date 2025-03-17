package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.stmt.*;

public class PostDominanceFinderTest {

  TestGraphGenerator graphGenerator = new TestGraphGenerator();

  @Test
  public void testPostDominanceFinder() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph();
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(graph);

    int[] domsArr = postDominanceFinder.getImmediateDominators();
    List<Integer> pDoms = Arrays.stream(domsArr).boxed().collect(Collectors.toList());

    List<Integer> expectedPDoms = Arrays.asList(-1, 5, 1, 1, 1, 0, 5);
    assertEquals(expectedPDoms, pDoms);
  }

  @Test
  public void testPostDominanceFrontiers() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph();
    PostDominanceFinder postDominanceFinder = new PostDominanceFinder(graph);

    List<BasicBlock<?>> blocks = postDominanceFinder.getIdxToBlock();
    List<Set<BasicBlock<?>>> pdfList =
        blocks.stream()
            .map(block -> postDominanceFinder.getDominanceFrontiers(block))
            .collect(Collectors.toList());

    List<Set<BasicBlock<?>>> expectedPDFList = new ArrayList<>();
    expectedPDFList.add(Collections.emptySet());
    expectedPDFList.add(Collections.singleton(blocks.get(5)));
    expectedPDFList.add(Collections.singleton(blocks.get(4)));
    expectedPDFList.add(Collections.singleton(blocks.get(4)));
    expectedPDFList.add(Collections.singleton(blocks.get(5)));
    expectedPDFList.add(Collections.emptySet());
    expectedPDFList.add(Collections.emptySet());

    assertEquals(expectedPDFList, pdfList);
  }

  @Test
  @Disabled("PostDominanceFinder doesn't work, if blockgraph contains two end-blocks")
  public void testDominanceFinder2() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph3();
    PostDominanceFinder postDom = new PostDominanceFinder(graph);

    int[] domsArr = postDom.getImmediateDominators();
    List<Integer> pDoms = Arrays.stream(domsArr).boxed().collect(Collectors.toList());

    List<Integer> expectedPDoms = Arrays.asList(2, 2, 2);
    assertEquals(expectedPDoms, pDoms);
  }

  @Test
  public void testBlockToIdxInverse() {
    MutableBlockStmtGraph graph = graphGenerator.createStmtGraph();
    DominanceFinder dom = new PostDominanceFinder(graph);

    // check that getBlockToIdx and getIdxToBlock are inverses
    for (BasicBlock<?> block : graph.getBlocks()) {
      List<BasicBlock<?>> idxToBlock = dom.getIdxToBlock();
      Map<BasicBlock<?>, Integer> blockToIdx = dom.getBlockToIdx();
      assertEquals(block, idxToBlock.get(blockToIdx.get(block)));
    }
  }
}
