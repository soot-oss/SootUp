package sootup.core.graph;

import java.util.*;

public class ReversePostOrderBlockTraversal {
  private final StmtGraph<?> cfg;

  public ReversePostOrderBlockTraversal(StmtGraph<?> cfg) {
    this.cfg = cfg;
  }

  public Iterable<BasicBlock<?>> getOrder() {
    return () -> new ReversePostOrderBlockIterator(cfg);
  }

  public static class ReversePostOrderBlockIterator implements Iterator<BasicBlock<?>> {
    StmtGraph<?> cfg;
    private Set<BasicBlock<?>> visited;
    Map<BasicBlock<?>, WorkUnit> worklist;
    boolean tryPop;
    WorkUnit popResult;

    private static class Frame {
      final BasicBlock<?> node;
      final Iterator<BasicBlock<?>> childIterator;

      Frame(BasicBlock<?> node, Iterator<BasicBlock<?>> childIterator) {
        this.node = node;
        this.childIterator = childIterator;
      }
    }

    public ReversePostOrderBlockIterator(StmtGraph<?> cfg) {
      this.cfg = cfg;
      this.visited = new HashSet<>();
      this.worklist = new HashMap<>();
      this.tryPop = false;

      initialize();
    }

    public void initialize() {
      BasicBlock<?> startNode = cfg.getStartingStmtBlock();
      if (startNode != null) {
        worklist.put(startNode, new WorkUnit(startNode));
      }
    }

    private void popWithHighestPriority() {
      tryPop = true;
      popResult = null;
      if (worklist.isEmpty()) {
        return;
      }
      Optional<Map.Entry<BasicBlock<?>, WorkUnit>> optEntry =
          worklist.entrySet().stream()
              .min(Map.Entry.comparingByValue(Comparator.comparingInt(WorkUnit::getPriority)));
      if (optEntry.isPresent()) {
        Map.Entry<BasicBlock<?>, WorkUnit> entry = optEntry.get();
        worklist.remove(entry.getKey());
        popResult = entry.getValue();
      }
    }

    private void updateWorkListBySuccessors(BasicBlock<?> currentNode) {
      for (BasicBlock<?> succ : currentNode.getSuccessors()) {
        if (!visited.contains(succ)) {
          WorkUnit work = worklist.getOrDefault(succ, new WorkUnit(succ));
          work.addVisitedPred(currentNode);
          worklist.put(succ, work);
        }
      }
    }

    @Override
    public boolean hasNext() {
      if (!tryPop) {
        popWithHighestPriority();
      }
      return popResult != null;
    }

    @Override
    public BasicBlock<?> next() {
      if (!tryPop) {
        popWithHighestPriority();
      }
      if (popResult == null) {
        throw new NoSuchElementException("There are no more blocks.");
      }

      tryPop = false;
      BasicBlock<?> currentNode = popResult.getNode();
      visited.add(currentNode);

      updateWorkListBySuccessors(currentNode);
      return currentNode;
    }

    public static class WorkUnit {
      private final BasicBlock<?> node;
      private final Set<BasicBlock<?>> visitedPreds = new HashSet<>();
      private int priority;

      public WorkUnit(BasicBlock<?> node) {
        this.node = node;
        computePriority(node);
      }

      private void computePriority(BasicBlock<?> node) {
        int totalPreds = node.getPredecessors().size();
        this.priority = totalPreds - visitedPreds.size();
      }

      public void addVisitedPred(BasicBlock<?> node) {
        this.visitedPreds.add(node);
        computePriority(node);
      }

      public BasicBlock<?> getNode() {
        return node;
      }

      public int getPriority() {
        return priority;
      }
    }
  }
}
