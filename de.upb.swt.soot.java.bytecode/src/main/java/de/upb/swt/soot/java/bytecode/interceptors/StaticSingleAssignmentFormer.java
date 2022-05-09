package de.upb.swt.soot.java.bytecode.interceptors;

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

import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.graph.BlockGraph;
import de.upb.swt.soot.core.graph.DominanceFinder;
import de.upb.swt.soot.core.graph.DominanceTree;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.JPhiExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.BodyUtils;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;

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
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    Set<Local> newLocals = new LinkedHashSet<>(builder.getLocals());
    int nextFreeIdx = 0;

    BlockGraph blockGraph = builder.getBlockGraph();

    // Keys: all blocks in BlockGraph. Values: a set of locals which defined in the corresponding
    // block
    Map<Block, Set<Local>> blockToDefs = new HashMap<>();

    // Keys: all locals in BodyBuilder. Values: a set of blocks which contains stmts with
    // corresponding local's def.
    Map<Local, Set<Block>> localToBlocks = new HashMap<>();

    // determine blockToDefs and localToBlocks by iterating all blocks.
    for (Block block : blockGraph.getBlocks()) {
      Set<Local> defs = new HashSet<>();
      for (Stmt stmt : blockGraph.getBlockStmts(block)) {
        if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
          Local local = (Local) stmt.getDefs().get(0);
          defs.add(local);
          if (localToBlocks.containsKey(local)) {
            localToBlocks.get(local).add(block);
          } else {
            Set<Block> bs = new HashSet<>();
            bs.add(block);
            localToBlocks.put(local, bs);
          }
        }
      }
      blockToDefs.put(block, defs);
    }

    DominanceFinder dominanceFinder = new DominanceFinder(blockGraph);

    // decide which block should be add a phi assignStmt, and store such info in a map
    // key: Block which contains phiStmts. Values : a set of phiStmts which contained by
    // corresponding Block
    Map<Block, Set<Stmt>> blockToPhiStmts =
        decideBlockToPhiStmts(builder, dominanceFinder, blockToDefs, localToBlocks);

    // delete meaningless phiStmts and add other phiStmts into blockGraph
    addPhiStmts(blockToPhiStmts, blockGraph, blockToDefs);

    DominanceTree tree = new DominanceTree(dominanceFinder);

    Map<Local, Stack<Local>> localToNameStack = new HashMap<>();
    for (Local local : builder.getLocals()) {
      localToNameStack.put(local, new Stack<>());
    }

    List<Block> treeNodes = tree.getALLNodesDFS();
    List<Block> blockStack = new ArrayList<>();
    HashSet<Block> visited = new HashSet<>();

    // rename each def-local and its corresponding name and add args and blocks into phiStmts
    for (int i = 0; i < treeNodes.size(); i++) {
      Block block = treeNodes.get(i);

      // replace use and def in each stmts in the current block
      Set<Stmt> newPhiStmts = new HashSet<>();
      for (Stmt stmt : blockGraph.getBlockStmts(block)) {
        // replace use
        if (!stmt.getUses().isEmpty() && !constainsPhiExpr(stmt)) {
          for (Value use : stmt.getUses()) {
            if (use instanceof Local) {
              Local newUse = localToNameStack.get(use).peek();
              Stmt newStmt = BodyUtils.withNewUse(stmt, use, newUse);
              blockGraph.replaceStmtInBlock(stmt, newStmt, block);
              stmt = newStmt;
            }
          }
        }
        // generate new def and replace with new def
        if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
          Local def = (Local) stmt.getDefs().get(0);
          Local newDef = def.withName(def.getName() + "#" + nextFreeIdx);
          newLocals.add(newDef);
          nextFreeIdx++;
          localToNameStack.get(def).push(newDef);
          Stmt newStmt = BodyUtils.withNewDef(stmt, newDef);
          blockGraph.replaceStmtInBlock(stmt, newStmt, block);
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
      List<Block> succs = new ArrayList<>(blockGraph.blockSuccessors(block));
      succs.addAll(blockGraph.exceptionalBlockSuccessors(block));
      for (Block succ : succs) {
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
              blockGraph.replaceStmtInBlock(phiStmt, newPhiStmt, succ);
            }
          }
          blockToPhiStmts.put(succ, newPhiStmts);
        }
      }

      // if a block's children in dominance tree are visited, pop this block from block stack, and
      // pop all defs in this block from the localToNameStack
      Block top = blockStack.get(blockStack.size() - 1);
      List<Block> children = tree.getChildren(top);
      while (containsAllChildren(visited, children)) {
        blockStack.remove(blockStack.size() - 1);
        for (Stmt stmt : blockGraph.getBlockStmts(top)) {
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
  private Map<Block, Set<Stmt>> decideBlockToPhiStmts(
      Body.BodyBuilder builder,
      DominanceFinder dominanceFinder,
      Map<Block, Set<Local>> blockToDefs,
      Map<Local, Set<Block>> localToBlocks) {
    Map<Block, Set<Stmt>> blockToPhiStmts = new HashMap<>();
    Map<Block, Set<Local>> blockToPhiLocals = new HashMap<>();
    Map<Local, Set<Block>> localToPhiBlocks = new HashMap<>();

    for (Local local : builder.getLocals()) {
      localToPhiBlocks.put(local, new HashSet<>());
      Deque<Block> blocks = new ArrayDeque<>(localToBlocks.get(local));
      while (!blocks.isEmpty()) {
        Block block = blocks.removeFirst();
        Set<Block> dfs = dominanceFinder.getDominanceFrontiers(block);
        // Only dominance frontiers of a block can add a phiStmt
        for (Block df : dfs) {
          if (!localToPhiBlocks.get(local).contains(df)) {
            localToPhiBlocks.get(local).add(df);

            // create an empty phiStmt
            JAssignStmt phiStmt = createEmptyPhiStmt(local);

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
    for (Block block : blockToPhiLocals.keySet()) {
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
      Map<Block, Set<Stmt>> blockToPhiStmts,
      BlockGraph blockGraph,
      Map<Block, Set<Local>> blockToDefs) {

    // key: phiStmt  value: size of phiStmt's arguments
    Map<Stmt, Integer> phiToNum = new HashMap();

    // determine the arguments' size of each phiStmt
    for (Block block : blockGraph.getBlocks()) {
      List<Block> succs = new ArrayList<>(blockGraph.blockSuccessors(block));
      succs.addAll(blockGraph.exceptionalBlockSuccessors(block));

      for (Block succ : succs) {
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
    for (Block block : blockToPhiStmts.keySet()) {
      Set<Stmt> phis = blockToPhiStmts.get(block);
      Set<Stmt> checkedPhis = new HashSet<>(blockToPhiStmts.get(block));
      for (Stmt cphi : checkedPhis) {
        if (phiToNum.get(cphi) < 2) {
          phis.remove(cphi);
        }
      }
      for (Stmt phi : phis) {
        blockGraph.addStmtOnTopOfBlock(phi, block);
      }
    }
  }

  private boolean containsAllChildren(Set<Block> blockSet, List<Block> children) {
    for (Block child : children) {
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

  private JAssignStmt createEmptyPhiStmt(Local local) {
    List<Local> args = new ArrayList<>();
    Map<Local, Block> argToBlock = new HashMap<>();
    JPhiExpr phi = new JPhiExpr(args, argToBlock);
    return new JAssignStmt(local, phi, StmtPositionInfo.createNoStmtPositionInfo());
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

  private Stmt addNewArgToPhi(Stmt phiStmt, Local arg, Block block) {

    Stmt newPhiStmt = null;
    for (Value use : phiStmt.getUses()) {
      if (use instanceof JPhiExpr) {
        JPhiExpr newPhiExpr = (JPhiExpr) use;
        List<Local> args = ((JPhiExpr) use).getArgs();
        Map<Local, Block> argToBlock = ((JPhiExpr) use).getArgToBlockMap();
        args.add(arg);
        argToBlock.put(arg, block);
        newPhiExpr = newPhiExpr.withArgs(args);
        newPhiExpr = newPhiExpr.withArgToBlockMap(argToBlock);
        newPhiStmt = ((JAssignStmt) phiStmt).withRValue(newPhiExpr);
        break;
      }
    }
    return newPhiStmt;
  }
}
