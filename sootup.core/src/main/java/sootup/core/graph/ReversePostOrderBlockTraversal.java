package sootup.core.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

public class ReversePostOrderBlockTraversal {
  private final BasicBlock<?> startNode;

  public ReversePostOrderBlockTraversal(StmtGraph<?> cfg) {
    startNode = cfg.getStartingStmtBlock();
  }

  public ReversePostOrderBlockTraversal(BasicBlock<?> startNode) {
    this.startNode = startNode;
  }

  @Nonnull
  public Iterable<BasicBlock<?>> getOrder() {
    return this::iterator;
  }

  @Nonnull
  public BlockIterator iterator() {
    return new BlockIterator(startNode);
  }

  @Nonnull
  public static List<BasicBlock<?>> getBlocksSorted(StmtGraph<?> cfg) {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                new ReversePostOrderBlockTraversal(cfg).iterator(), Spliterator.ORDERED),
            false)
        .collect(Collectors.toList());
  }

  public static class BlockIterator implements Iterator<BasicBlock<?>> {
    private List<BasicBlock<?>> blocks;
    private int i = 0;

    public BlockIterator(@Nonnull BasicBlock<?> startNode) {
      blocks =
          StreamSupport.stream(
                  Spliterators.spliteratorUnknownSize(
                      new PostOrderBlockTraversal.BlockIterator(startNode), Spliterator.ORDERED),
                  false)
              .collect(Collectors.toList());
      Collections.reverse(blocks);
    }

    @Override
    public boolean hasNext() {
      return i < blocks.size();
    }

    @Override
    public BasicBlock<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException("There is no more block.");
      }
      i++;
      return blocks.get(i - 1);
    }
  }
}
