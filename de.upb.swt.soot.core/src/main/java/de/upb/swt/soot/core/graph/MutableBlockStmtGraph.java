package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MutableBlockStmtGraph extends MutableStmtGraph {

  @Nullable private Stmt startingStmt = null;
  @Nonnull private final Map<Stmt, Integer> stmtToBlock = new HashMap<>();
  @Nonnull private final ArrayList<MutableBasicBlock> blocks = new ArrayList<>();

  public MutableBlockStmtGraph() {}

  /** copies a StmtGraph into this Mutable instance */
  public MutableBlockStmtGraph(@Nonnull StmtGraph graph) {
    setStartingStmt(graph.getStartingStmt());
    for (Stmt stmt : graph) {
      setEdges(stmt, successors(stmt));
    }
    // FIXME: setTraps(graph.getTraps());
    throw new IllegalArgumentException("cant handle trap conversion ");
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt stmt) {
    // FIXME implement
    throw new IllegalArgumentException("cant handle trap removal yet.");
  }

  @Override
  @Nonnull
  // FIXME: return them in post-reverse-order for ssa?
  public List<? extends BasicBlock> getBlocks() {
    return blocks.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public void addNode(@Nonnull Stmt node, @Nonnull List<ClassType> exceptions) {
    final MutableBasicBlock block = addNodeInternal(node);

    if (block.getExceptionalSuccessors().size() == exceptions.size()) {

    } else {

      // FIXME: implement: handle traps/exceptions i.e. block splitting in here!

    }
  }

  @Override
  public void addBlock(@Nonnull MutableBasicBlock block) {
    // check if the block is already existing in blocks
    // TODO: [ms] -> performance! its currently an ArrayList
    for (MutableBasicBlock mutableBasicBlock : blocks) {
      if (mutableBasicBlock == block) {
        throw new IllegalArgumentException(
            "The given block exists already in this MutableBlockStmtGraph Instance.");
      }
    }

    int newIdx = blocks.size();
    blocks.add(block);
    block
        .getStmts()
        .forEach(
            stmt -> {
              final Integer exists = stmtToBlock.put(stmt, newIdx);
              if (exists != null) {
                throw new IllegalArgumentException(
                    "Stmt \"" + stmt + "\" of the given Block exists already in the graph!");
              }
            });
  }

  protected MutableBasicBlock addNodeInternal(@Nonnull Stmt stmt) {
    Integer blockIdx = stmtToBlock.get(stmt);
    if (blockIdx != null) {
      return blocks.get(blockIdx);
    }

    MutableBasicBlock block = new MutableBasicBlock();
    blockIdx = blocks.size();
    blocks.add(block);
    addNodeInternal(block, blockIdx, stmt);
    return block;
  }

  protected void addNodeInternal(
      @Nonnull MutableBasicBlock block, int blockIdx, @Nonnull Stmt stmt) {
    block.addStmt(stmt);
    stmtToBlock.put(stmt, blockIdx);
  }

  public void removeNode(@Nonnull Stmt stmt) {
    Integer blockIdx = stmtToBlock.remove(stmt);
    if (blockIdx == null) {
      return;
    }
    // do edges from or to this node exist? remove them
    // TODO: [ms] implement more performant solution based on datastructure implications
    predecessors(stmt).forEach(p -> removeEdge(p, stmt));
    successors(stmt).forEach(s -> removeEdge(stmt, s));

    MutableBasicBlock blockOfRemovedStmt = blocks.get(blockIdx);
    blockOfRemovedStmt.removeStmt(stmt);

    if (blockOfRemovedStmt.getStmts().size() <= 0) {
      // for GC: clear entry in blocks if block is empty -> not referenced anymore -> not reachable
      // anymore
      blocks.set(blockIdx, null);
      // blocks.remove( blockIdx.intValue() ); would need an expensive update of all higher blockIdx
      // that are then changed
    }
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    // TODO: [ms] implement it smarter
    removeNode(oldStmt);
    addNode(newStmt);
  }

  @Override
  public void putEdge(@Nonnull Stmt stmtA, @Nonnull Stmt stmtB) {

    Integer blockAIdx = stmtToBlock.get(stmtA);
    Integer blockBIdx = stmtToBlock.get(stmtB);

    MutableBasicBlock blockA;
    MutableBasicBlock blockB;

    if (blockAIdx == null) {
      // stmtA is is not in the graph (i.e. no reference to BlockA) -> create
      blockA = addNodeInternal(stmtA);
      blockAIdx = stmtToBlock.get(stmtA);
    } else {
      blockA = blocks.get(blockAIdx);
      if (blockA.getTail() != stmtA) {
        // if StmtA is not at the end of the block -> it needs to branch to reach StmtB
        // if StmtA branches -> it must at the end of a block
        throw new IllegalArgumentException(
            "StmtA is neither a BranchingStmt nor at the end of a block but it must be to reach StmtB.");
      }
    }

    if (blockA.getSuccessors().size() >= stmtA.getSuccessorCount()) {
      throw new IllegalArgumentException(
          "Can't add another flow - there are already enough flows i.e. "
              + stmtA.getSuccessorCount()
              + " outgoing from StmtA "
              + stmtA);
    }

    if (stmtA.branches()) {
      // branching A indicates the end of BlockA and connects to another BlockB: reuse or create new
      // one
      if (blockBIdx == null) {
        blockB = new MutableBasicBlock();
        blockBIdx = blocks.size();
        blocks.add(blockB);
        blockB.addStmt(stmtB);
        stmtToBlock.put(stmtB, blockBIdx);
        blockB.addPredecessorBlock(blockA);
        blockA.addSuccessorBlock(blockB);

      } else {
        blockB = blocks.get(blockBIdx);

        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          blockB.addPredecessorBlock(blockA);
          blockA.addSuccessorBlock(blockB);

        } else {

          MutableBasicBlock newBlock = blockB.splitBlockLinked(stmtB, true);
          int newBlockIdx = blocks.size();
          blocks.add(newBlock);
          newBlock.getStmts().forEach(stmt -> stmtToBlock.put(stmt, newBlockIdx));

          if (blockA == blockB) {
            // successor of block is the origin: end of block flows to beginning of new splitted
            // block (i.e.
            // the same block)
            newBlock.addSuccessorBlock(newBlock);
            newBlock.addPredecessorBlock(newBlock);
          } else {
            blockA.addSuccessorBlock(newBlock);
            newBlock.addPredecessorBlock(blockA);
          }
        }
      }

    } else {
      // stmt does not branch: it can live in the same block: so add it to the block
      if (blockBIdx == null) {
        addNodeInternal(blockA, blockAIdx, stmtB);
      } else {
        blockB = blocks.get(blockBIdx);
        if (blockB.getHead() == stmtB) {
          // stmtB is at the beginning of the second Block -> connect blockA and blockB
          // lockB.addPredecessorBlock(blockA);
          // blockA.addSuccessorBlock(blockB);

          // merge and remove now obsolete Block B
          Integer finalBlockAIdx = blockAIdx;
          blockB
              .getStmts()
              .forEach(
                  stmt -> {
                    blockA.addStmt(stmt);
                    stmtToBlock.put(stmt, finalBlockAIdx);
                  });
          // dont remove the block slot otherwise we need to update all higher indices to reflect
          // the new index positions
          blocks.set(blockBIdx, null);

          // TODO: hint: [ms] for serialisation we need to validate that n-1 predecessors are
          // branching stmts.
        } else {
          throw new IllegalArgumentException(
              "Stmt is already in the Graph: a) remove StmtB or b) StmtA must be a branching Stmt that branches to StmtB ");
        }
      }
    }
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    Integer blockOfFromIdx = stmtToBlock.get(from);
    removeEdgeInternal(from, to, blockOfFromIdx);
  }

  protected void removeEdgeInternal(
      @Nonnull Stmt from, @Nonnull Stmt to, @Nonnull Integer blockIdxOfFrom) {
    MutableBasicBlock blockOfFrom = blocks.get(blockIdxOfFrom);
    MutableBasicBlock blockOfTo = blocks.get(stmtToBlock.get(to));
    removeBlockBorderEdgesInternal(from, blockOfFrom, blockIdxOfFrom);

    // divide block if from and to are from the same block
    if (blockOfFrom == blockOfTo) {
      // divide block and don't link them
      int fromIdx = blockOfFrom.getStmts().indexOf(from);
      if (blockOfFrom.getStmts().get(fromIdx + 1) == to) {
        MutableBasicBlock newBlock = blockOfFrom.splitBlockUnlinked(from, to);
        Integer newBlockIdx = blocks.size();
        blocks.add(newBlock);
        newBlock.getStmts().forEach(s -> stmtToBlock.put(s, newBlockIdx));
      } else {
        // TODO: do we need to do sth here? i.e. in the case that: ...
      }
    }
  }

  protected void removeBlockBorderEdgesInternal(
      @Nonnull Stmt from, @Nonnull MutableBasicBlock blockOfFrom, @Nonnull Integer blockIdxOfFrom) {
    // TODO: is it intuitive to remove connections to the BasicBlock in the case we cant merge the
    // blocks?

    // merge previous block if possible i.e. previous block has no branchingstmt as tail && same
    // traps
    if (blockOfFrom.getStmts().size() > 0 && from == blockOfFrom.getTail()) {
      if (blockOfFrom.getPredecessors().size() == 1) {
        MutableBasicBlock singlePreviousBlock = blockOfFrom.getPredecessors().get(0);
        if (!singlePreviousBlock.getTail().branches() && singlePreviousBlock != blockOfFrom) {
          if (singlePreviousBlock.getTraps().equals(blockOfFrom.getTraps())) {
            blockOfFrom
                .getStmts()
                .forEach(
                    k -> {
                      singlePreviousBlock.addStmt(k);
                      stmtToBlock.put(k, blockIdxOfFrom);
                    });
          }
        }
      }
    }

    // remove outgoing connections from block if from stmt is the tail
    if (from == blockOfFrom.getTail()) {

      if (!from.branches()) {
        if (blockOfFrom.getStmts().size() > 0 && blockOfFrom.getSuccessors().size() == 1) {
          // merge previous block if possible i.e. no branchingstmt as tail && same traps && no
          // other predesccorblocks
          MutableBasicBlock singleSuccessorBlock = blockOfFrom.getSuccessors().get(0);
          if (singleSuccessorBlock.getPredecessors().size() == 1
              && singleSuccessorBlock.getPredecessors().get(0) == blockOfFrom) {
            if (singleSuccessorBlock.getTraps().equals(blockOfFrom.getTraps())) {
              singleSuccessorBlock
                  .getStmts()
                  .forEach(
                      k -> {
                        blockOfFrom.addStmt(k);
                        stmtToBlock.put(k, blockIdxOfFrom);
                      });
            }
          }
        }
      } else {
        blockOfFrom.clearSuccessorBlocks();
      }
    }
  }

  @Override
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    Integer fromBlockIdx = stmtToBlock.get(from);
    if (fromBlockIdx == null) {
      // 'from Stmt' does not exist yet -> create
      addNodeInternal(from);
    } else {
      successors(from).forEach(succ -> removeEdge(from, succ));
    }
    targets.forEach(to -> putEdge(from, to));
  }

  @Nullable
  @Override
  public Stmt getStartingStmt() {
    // is the stmt currently in a block associated with the graph?
    if (stmtToBlock.get(startingStmt) == null) {
      return null;
    }
    return startingStmt;
  }

  @Nonnull
  @Override
  public StmtGraph unmodifiableStmtGraph() {
    return new ForwardingStmtGraph(this);
  }

  public void setStartingStmt(@Nonnull Stmt startingStmt) {
    if (stmtToBlock.get(startingStmt) == null) {
      addNodeInternal(startingStmt);
    }
    this.startingStmt = startingStmt;
  }

  @Nonnull
  @Override
  public Set<Stmt> nodes() {
    return stmtToBlock.keySet();
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmtToBlock.containsKey(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getHead()) {
      List<MutableBasicBlock> predecessorBlocks = block.getPredecessors();
      // TODO: [ms] maybe dont copy
      List<Stmt> preds = new ArrayList<>(predecessorBlocks.size());
      predecessorBlocks.forEach(p -> preds.add(p.getTail()));
      return preds;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(stmts.indexOf(node) - 1));
    }
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getTail()) {
      List<MutableBasicBlock> successorBlocks = block.getSuccessors();
      List<Stmt> succs = new ArrayList<>(successorBlocks.size());
      successorBlocks.forEach(p -> succs.add(p.getHead()));
      return succs;
    } else {
      // argh indexOf.. possibly expensive..
      List<Stmt> stmts = block.getStmts();
      return Collections.singletonList(stmts.get(stmts.indexOf(node) + 1));
    }
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getHead()) {
      return block.getPredecessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    Integer blockIdx = stmtToBlock.get(node);
    if (blockIdx == null) {
      throw new IllegalArgumentException("Stmt is not contained in the BlockStmtGraph: " + node);
    }
    MutableBasicBlock block = blocks.get(blockIdx);

    if (node == block.getTail()) {
      return block.getSuccessors().size();
    } else {
      return 1;
    }
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    Integer blockAIdx = stmtToBlock.get(source);
    if (blockAIdx == null) {
      throw new IllegalArgumentException(
          "source Stmt is not contained in the BlockStmtGraph: " + source);
    }

    MutableBasicBlock blockA = blocks.get(blockAIdx);

    if (source == blockA.getTail()) {
      Integer blockBIdx = stmtToBlock.get(source);
      if (blockBIdx == null) {
        throw new IllegalArgumentException(
            "target Stmt is not contained in the BlockStmtGraph: " + source);
      }
      MutableBasicBlock blockB = blocks.get(blockBIdx);
      return blockA.getSuccessors().stream()
          .anyMatch(
              successorBlock -> /*successorBlock == blockB && */
                  successorBlock.getHead() == target);
    } else {
      List<Stmt> stmtsA = blockA.getStmts();
      return stmtsA.get(stmtsA.indexOf(source) + 1) == target;
    }
  }
}
