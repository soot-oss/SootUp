package sootup.core.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

public class PostOrderBlockTraversal {

  private final BasicBlock<?> startNode;

  public PostOrderBlockTraversal(StmtGraph<?> cfg) {
    startNode = cfg.getStartingStmtBlock();
  }

  public PostOrderBlockTraversal(BasicBlock<?> startNode) {
    this.startNode = startNode;
  }

  public Iterable<BasicBlock<?>> getOrder() {
    return this::iterator;
  }

  public BlockIterator iterator() {
    return new BlockIterator(startNode);
  }

  @Nonnull
  public static List<BasicBlock<?>> getBlocksSorted(StmtGraph<?> cfg) {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                new PostOrderBlockTraversal(cfg).iterator(), Spliterator.ORDERED),
            false)
        .collect(Collectors.toList());
  }

  public static class BlockIterator implements Iterator<BasicBlock<?>> {
    private final Stack<Frame> stack = new Stack<>();
    private final Set<BasicBlock<?>> visited = new HashSet<>();

    public BlockIterator(@Nonnull BasicBlock<?> startNode) {
      visitNode(startNode);
      stack.push(
          new Frame(startNode, ((List<BasicBlock<?>>) startNode.getSuccessors()).iterator()));
    }

    private boolean visitNode(@Nonnull BasicBlock<?> node) {
      return visited.add(node);
    }

    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public BasicBlock<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException("There is no more block.");
      }
      while (!stack.isEmpty()) {
        Frame frame = stack.peek();
        if (frame.succIterator.hasNext()) {
          BasicBlock<?> succ = frame.succIterator.next();
          if (visitNode(succ)) {
            stack.push(new Frame(succ, ((List<BasicBlock<?>>) succ.getSuccessors()).iterator()));
          }
        } else {
          stack.pop();
          return frame.node;
        }
      }
      return null;
    }

    private static class Frame {
      final BasicBlock<?> node;
      final Iterator<BasicBlock<?>> succIterator;

      Frame(BasicBlock<?> node, Iterator<BasicBlock<?>> childIterator) {
        this.node = node;
        this.succIterator = childIterator;
      }
    }
  }
}
