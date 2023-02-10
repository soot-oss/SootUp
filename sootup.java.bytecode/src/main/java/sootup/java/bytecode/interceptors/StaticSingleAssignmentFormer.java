package sootup.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2021 Zun Wang
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.DominanceFinder;
import sootup.core.graph.DominanceTree;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

/**
 * In Static Single Assignment Form, each variable be assigned exactly once, and every variable be
 * defined before it is used.
 *
 * @author Zun Wang
 * @see <a
 *     href="https://en.wikipedia.org/wiki/Static_single_assignment_form">https://en.wikipedia.org/wiki/Static_single_assignment_form</a>
 */
public class StaticSingleAssignmentFormer implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {

    Set<Local> newLocals = new LinkedHashSet<>(builder.getLocals());
    int nextFreeIdx = 0;

    MutableStmtGraph stmtGraph = builder.getStmtGraph();

    // Keys: all blocks in BlockGraph. Values: a set of locals which defined in the corresponding
    // block
    Map<BasicBlock<?>, Set<Local>> blockToDefs = new HashMap<>();

    // Keys: all locals in BodyBuilder. Values: a set of blocks which contains stmts with
    // corresponding local's def.
    Map<Local, Set<BasicBlock<?>>> localToBlocks = new HashMap<>();

    // determine blockToDefs and localToBlocks by iterating all blocks.
    for (BasicBlock<?> block : stmtGraph.getBlocks()) {
      Set<Local> defs = new HashSet<>();
      for (Stmt stmt : block.getStmts()) {
        if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
          Local local = (Local) stmt.getDefs().get(0);
          defs.add(local);
          if (localToBlocks.containsKey(local)) {
            localToBlocks.get(local).add(block);
          } else {
            Set<BasicBlock<?>> bs = new HashSet<>();
            bs.add(block);
            localToBlocks.put(local, bs);
          }
        }
      }
      blockToDefs.put(block, defs);
    }

    DominanceFinder dominanceFinder = new DominanceFinder(stmtGraph);

    // decide which block should be add a phi assignStmt, and store such info in a map
    // key: Block which contains phiStmts. Values : a set of phiStmts which contained by
    // corresponding Block
    Map<BasicBlock<?>, Set<Stmt>> blockToPhiStmts =
        decideBlockToPhiStmts(builder, dominanceFinder, blockToDefs, localToBlocks);

    // delete meaningless phiStmts and add other phiStmts into stmtGraph
    addPhiStmts(blockToPhiStmts, stmtGraph, blockToDefs);

    DominanceTree tree = new DominanceTree(dominanceFinder);

    Map<Local, Stack<Local>> localToNameStack = new HashMap<>();
    for (Local local : builder.getLocals()) {
      localToNameStack.put(local, new Stack<>());
    }

    List<BasicBlock<?>> treeNodes = tree.getAllNodesDFS();
    List<BasicBlock<?>> blockStack = new ArrayList<>();
    Set<BasicBlock<?>> visited = new HashSet<>();

    // rename each def-local and its corresponding name and add args and blocks into phiStmts
    for (BasicBlock<?> block : treeNodes) {
      // replace use and def in each stmts in the current block
      Set<Stmt> newPhiStmts = new HashSet<>();
      for (Stmt stmt : block.getStmts()) {
        // replace use
        final List<Value> uses = stmt.getUses();
        if (!uses.isEmpty() && !constainsPhiExpr(stmt)) {
          for (Value use : uses) {
            if (use instanceof Local) {
              Local newUse = localToNameStack.get(use).peek();
              Stmt newStmt = stmt.withNewUse(use, newUse);
              stmtGraph.replaceNode(stmt, newStmt);
              stmt = newStmt;
            }
          }
        }
        // generate new def and replace with new def
        final List<Value> defs = stmt.getDefs();
        if (!defs.isEmpty() && defs.get(0) instanceof Local) {
          Local def = (Local) defs.get(0);
          Local newDef = def.withName(def.getName() + "#" + nextFreeIdx);
          newLocals.add(newDef);
          nextFreeIdx++;
          localToNameStack.get(def).push(newDef);
          Stmt newStmt = ((AbstractDefinitionStmt<?, ?>) stmt).withNewDef(newDef);
          stmtGraph.replaceNode(stmt, newStmt);
          if (constainsPhiExpr(newStmt)) {
            newPhiStmts.add(newStmt);
          }
        }
      }
      visited.add(block);
      blockStack.add(block);
      if (blockToPhiStmts.containsKey(block)) {
        blockToPhiStmts.put(block, newPhiStmts);
      }

      // if successors has phiStmts, add corresponding args and this block into the phiStmts
      List<BasicBlock<?>> succs = new ArrayList<>(block.getSuccessors());
      succs.addAll(block.getExceptionalSuccessors().values());
      for (BasicBlock<?> succ : succs) {
        if (blockToPhiStmts.containsKey(succ)) {
          Set<Stmt> phiStmts = blockToPhiStmts.get(succ);
          newPhiStmts = new HashSet<>(phiStmts);
          for (Stmt phiStmt : phiStmts) {
            Local def = (Local) phiStmt.getDefs().get(0);
            Local oriDef = getOriginalLocal(def, localToNameStack.keySet());
            if (!localToNameStack.get(oriDef).isEmpty()) {
              Local arg = localToNameStack.get(oriDef).peek();
              Stmt newPhiStmt = addNewArgToPhi(phiStmt, arg, block);
              newPhiStmts.remove(phiStmt);
              newPhiStmts.add(newPhiStmt);
              stmtGraph.replaceNode(phiStmt, newPhiStmt);
            }
          }
          blockToPhiStmts.put(succ, newPhiStmts);
        }
      }

      // if a block's children in dominance tree are visited, pop this block from block stack, and
      // pop all defs in this block from the localToNameStack
      BasicBlock<?> top = blockStack.get(blockStack.size() - 1);
      List<BasicBlock<?>> children = tree.getChildren(top);
      while (containsAllChildren(visited, children)) {
        blockStack.remove(blockStack.size() - 1);
        for (Stmt stmt : top.getStmts()) {
          if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
            Local def = (Local) stmt.getDefs().get(0);
            Local oriDef = getOriginalLocal(def, localToNameStack.keySet());
            if (!localToNameStack.get(oriDef).isEmpty()) {
              localToNameStack.get(oriDef).pop();
            }
          }
        }
        if (!blockStack.isEmpty()) {
          top = blockStack.get(blockStack.size() - 1);
          children = tree.getChildren(top);
        } else {
          break;
        }
      }
    }
    builder.setLocals(newLocals);
  }

  /**
   * This method is used to decide which block should add phiStmts. Note: some phiStmts maybe
   * contain just one argument, it should be not added into StmtGraph
   *
   * @param dominanceFinder an object of DomimanceFinder, it should be created by the given
   *     blockGraph
   * @param blockToDefs maps each block to the set of defs' local in itself
   * @param localToBlocks maps each def local to the set of blocks where it is defined.
   * @return a map, key: block, value: a set of phiStmts that are added in front of the
   *     corresponding block
   */
  private Map<BasicBlock<?>, Set<Stmt>> decideBlockToPhiStmts(
      Body.BodyBuilder builder,
      DominanceFinder dominanceFinder,
      Map<BasicBlock<?>, Set<Local>> blockToDefs,
      Map<Local, Set<BasicBlock<?>>> localToBlocks) {
    Map<BasicBlock<?>, Set<Stmt>> blockToPhiStmts = new HashMap<>();
    Map<BasicBlock<?>, Set<Local>> blockToPhiLocals = new HashMap<>();
    Map<Local, Set<BasicBlock<?>>> localToPhiBlocks = new HashMap<>();

    for (Local local : builder.getLocals()) {
      localToPhiBlocks.put(local, new HashSet<>());
      Deque<BasicBlock<?>> blocks = new ArrayDeque<>(localToBlocks.get(local));
      while (!blocks.isEmpty()) {
        BasicBlock<?> block = blocks.removeFirst();
        Set<BasicBlock<?>> dfs = dominanceFinder.getDominanceFrontiers(block);
        // Only dominance frontiers of a block can add a phiStmt
        for (BasicBlock<?> df : dfs) {
          final Set<BasicBlock<?>> basicBlocks = localToPhiBlocks.get(local);
          if (!basicBlocks.contains(df)) {
            basicBlocks.add(df);

            // create an empty phiStmt
            JAssignStmt<?, ?> phiStmt = createEmptyPhiStmt(local);

            // store phiStmt into map
            if (blockToPhiStmts.containsKey(df)) {
              blockToPhiStmts.get(df).add(phiStmt);
              blockToPhiLocals.get(df).add(local);
            } else {
              Set<Stmt> phiStmts = new LinkedHashSet<>();
              phiStmts.add(phiStmt);
              blockToPhiStmts.put(df, phiStmts);
              Set<Local> phiLocals = new HashSet<>();
              phiLocals.add(local);
              blockToPhiLocals.put(df, phiLocals);
            }

            // if the dominance frontier contains no such local, its dominance frontier should add a
            // phiStmt, so add it into queue
            if (!blockToDefs.get(df).contains(local)) {
              blocks.add(df);
            }
          }
        }
      }
    }

    // if a block has a phiStmt, the local of the phiStmt should be added into blockToDefs
    for (BasicBlock<?> block : blockToPhiLocals.keySet()) {
      blockToDefs.get(block).addAll(blockToPhiLocals.get(block));
    }
    return blockToPhiStmts;
  }

  /**
   * Delete the phiStmts which contain only one argument, and add other undeleted phiStmts into
   * blockGraph
   *
   * @param blockToPhiStmts a map, key: block, value: a set of phiStmts that are added in front of
   *     the corresponding block
   * @param blockGraph blockGraph where the phiStmt should be added
   * @param blockToDefs maps each block to the set of defs' local in itself
   */
  private void addPhiStmts(
      Map<BasicBlock<?>, Set<Stmt>> blockToPhiStmts,
      MutableStmtGraph blockGraph,
      Map<BasicBlock<?>, Set<Local>> blockToDefs) {

    // key: phiStmt  value: size of phiStmt's arguments
    Map<Stmt, Integer> phiToNum = new HashMap();

    // determine the arguments' size of each phiStmt
    for (BasicBlock<?> block : blockGraph.getBlocks()) {
      List<BasicBlock<?>> succs = new ArrayList<>(block.getSuccessors());
      succs.addAll(block.getExceptionalSuccessors().values());

      for (BasicBlock<?> succ : succs) {
        if (blockToPhiStmts.containsKey(succ)) {
          for (Stmt phi : blockToPhiStmts.get(succ)) {
            Local local = (Local) phi.getDefs().get(0);
            if (blockToDefs.get(block).contains(local)) {
              if (phiToNum.containsKey(phi)) {
                int num = phiToNum.get(phi);
                phiToNum.replace(phi, num + 1);
              } else {
                phiToNum.put(phi, 1);
              }
            }
          }
        }
      }
    }

    // if the arguments' size of a phiStmt is less than 2, delete it from blockToPhiStmts map
    // add other phiStmts into corresponding block
    for (BasicBlock<?> block : blockToPhiStmts.keySet()) {
      Set<Stmt> phis = blockToPhiStmts.get(block);
      Set<Stmt> checkedPhis = new HashSet<>(blockToPhiStmts.get(block));
      for (Stmt cphi : checkedPhis) {
        if (phiToNum.get(cphi) < 2) {
          phis.remove(cphi);
        }
      }
      for (Stmt phi : phis) {
        blockGraph.insertBefore(block.getHead(), phi);
      }
    }
  }

  private boolean containsAllChildren(Set<BasicBlock<?>> blockSet, List<BasicBlock<?>> children) {
    for (BasicBlock<?> child : children) {
      if (!blockSet.contains(child)) {
        return false;
      }
    }
    return true;
  }

  private boolean constainsPhiExpr(Stmt stmt) {
    if (stmt instanceof JAssignStmt && !stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof JPhiExpr) {
          return true;
        }
      }
    }
    return false;
  }

  private JAssignStmt<?, ?> createEmptyPhiStmt(Local local) {
    JPhiExpr phi = new JPhiExpr(Collections.emptyList(), Collections.emptyMap());
    return new JAssignStmt<>(local, phi, StmtPositionInfo.createNoStmtPositionInfo());
  }

  private Local getOriginalLocal(Local local, Set<Local> oriLocals) {
    if (oriLocals.contains(local)) {
      return local;
    }
    int hashPos = local.getName().indexOf('#');
    String oriName = local.getName().substring(0, hashPos);
    for (Local oriLocal : oriLocals) {
      if (oriLocal.getName().equals(oriName)) {
        return oriLocal;
      }
    }
    throw new RuntimeException(local + " has no original local!");
  }

  private Stmt addNewArgToPhi(Stmt phiStmt, Local arg, BasicBlock<?> block) {

    Stmt newPhiStmt = null;
    for (Value use : phiStmt.getUses()) {
      if (use instanceof JPhiExpr) {
        JPhiExpr newPhiExpr = (JPhiExpr) use;
        List<Local> args = ((JPhiExpr) use).getArgs();
        Map<Local, BasicBlock<?>> argToBlock = ((JPhiExpr) use).getArgToBlockMap();
        args.add(arg);
        argToBlock.put(arg, block);
        newPhiExpr = newPhiExpr.withArgs(args);
        newPhiExpr = newPhiExpr.withArgToBlockMap(argToBlock);
        newPhiStmt = ((JAssignStmt<?, ?>) phiStmt).withRValue(newPhiExpr);
        break;
      }
    }
    return newPhiStmt;
  }
}
