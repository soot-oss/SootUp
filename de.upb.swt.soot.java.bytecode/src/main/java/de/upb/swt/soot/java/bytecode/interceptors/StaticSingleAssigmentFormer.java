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
public class StaticSingleAssigmentFormer implements BodyInterceptor {

  private BlockGraph blockGraph;

  // Keys: all blocks in BlockGraph. Values: a set of locals which defined in the corresponding
  // block
  Map<Block, Set<Local>> blockToDefs = new HashMap<>();

  // Keys: all locals in BodyBuilder. Values: a set of blocks which constants stmts with
  // corresponding local's def.
  Map<Local, Set<Block>> localToBlocks = new HashMap<>();

  //key: Block which contains phiStmts. Values : a set of phiStmts which contained by corresponding Block
  Map<Block, Set<Stmt>> blockToPhiStmts = new HashMap<>();

  int nextFreeIdx = 0;

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    // determine blockToDefs and localToBlocks
    blockGraph = builder.getBlockGraph();
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

    // decide which block should be add a phi assignStmt
    Map<Local, Set<Block>> localToPhiBlocks = new HashMap<>();

    DominanceFinder dominanceFinder = new DominanceFinder(blockGraph);
    for (Local local : localToBlocks.keySet()) {
      localToPhiBlocks.put(local, new HashSet<>());
      Deque<Block> blocks = new ArrayDeque<>(localToBlocks.get(local));
      while (!blocks.isEmpty()) {
        Block block = blocks.removeFirst();
        Set<Block> dfs = dominanceFinder.getDominanceFrontiers(block);
        for (Block df : dfs) {
          if (!localToPhiBlocks.get(local).contains(df)) {
            List<Local> args = new ArrayList<>();
            Map<Local, Block> argToBlock = new HashMap<>();
            JPhiExpr phi = new JPhiExpr(args, argToBlock);
            JAssignStmt phiStmt =
                new JAssignStmt(local, phi, StmtPositionInfo.createNoStmtPositionInfo());
            Block newBlock = blockGraph.addStmtOnTopOfBlock(phiStmt, df);

            // fit newBlock to everywhere
            dominanceFinder.replaceBlock(newBlock, df);
            blockToDefs.put(newBlock, blockToDefs.get(df));
            blockToDefs.remove(df);
            replaceBlockInMap(localToBlocks, newBlock, df);
            replaceBlockInMap(localToPhiBlocks, newBlock, df);
            if (blockToPhiStmts.containsKey(df)) {
              blockToPhiStmts.get(df).add(phiStmt);
              blockToPhiStmts.put(newBlock, blockToPhiStmts.get(df));
              blockToPhiStmts.remove(df);
            } else {
              Set<Stmt> phiStmts = new HashSet<>();
              phiStmts.add(phiStmt);
              blockToPhiStmts.put(newBlock, phiStmts);
            }

            localToPhiBlocks.get(local).add(newBlock);
            if (!blockToDefs.get(newBlock).contains(local)) {
              blocks.add(newBlock);
            }
          }
        }
      }
    }
    // renaming
    DominanceTree tree = dominanceFinder.getDominanceTree();
    Map<Local, Stack<Local>> localToNameStack = new HashMap<>();
    for(Local local : builder.getLocals()){
      localToNameStack.put(local, new Stack<>());
    }
    rename(tree, localToNameStack);

    System.out.println(builder.build());

  }

  private void replaceBlockInMap(Map<Local, Set<Block>> map, Block nb, Block ob) {
    for (Local key : map.keySet()) {
      if (map.get(key).contains(ob)) {
        Set<Block> bs = map.get(key);
        bs.remove(ob);
        bs.add(nb);
      }
    }
  }

  private void rename(DominanceTree tree, Map<Local, Stack<Local>> localToNameStack) {
    Block block = tree.getContent();
    Block oldBlock = block;
    boolean isChanged = false;

    for (Stmt stmt : blockGraph.getBlockStmts(block)) {
      // replace use
      if (!stmt.getUses().isEmpty() && !constainsPhiExpr(stmt)) {
        for (Value use : stmt.getUses()) {
          if (use instanceof Local) {
            Local newUse = localToNameStack.get(use).peek();
            Stmt newStmt = BodyUtils.withNewUse(stmt, use, newUse);
            block = blockGraph.replaceStmtInBlock(stmt, newStmt, block);
            isChanged = true;
            stmt = newStmt;
          }
        }
      }
      // generate new def and replace with new def
      if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
        Local def = (Local) stmt.getDefs().get(0);
        Local newDef = def.withName(def.getName() + "#" + nextFreeIdx);
        nextFreeIdx++;
        localToNameStack.get(def).push(newDef);
        Stmt newStmt = BodyUtils.withNewDef(stmt, newDef);
        block = blockGraph.replaceStmtInBlock(stmt, newStmt, block);
        isChanged = true;
      }
    }
    if(isChanged && blockToPhiStmts.containsKey(oldBlock)){
      blockToPhiStmts.put(block, blockToPhiStmts.get(oldBlock));
      blockToPhiStmts.remove(oldBlock);
    }
    for (Block succ : blockGraph.blockSuccessors(block)) {
      if (blockToPhiStmts.containsKey(succ)) {
        Set<Stmt> phiStmts = blockToPhiStmts.get(succ);
        for (Stmt phiStmt : phiStmts) {
          Local def = (Local) phiStmt.getDefs().get(0);
          Local oriDef = getOriginalLocal(def, localToNameStack.keySet());
          if(!localToNameStack.get(oriDef).isEmpty()){
            Local arg = localToNameStack.get(oriDef).peek();
            for (Value use : phiStmt.getUses()) {
              if (use instanceof JPhiExpr) {
                //todo: phi addVisitor
                ((JPhiExpr) use).addArg(arg, block);
              }
            }
          }
        }
      }
    }
    // call renaming recursively
    if(!tree.getChildren().isEmpty()){
      for(DominanceTree child : tree.getChildren()){
        rename(child, localToNameStack);
      }
    }

    for(Stmt stmt : blockGraph.getBlockStmts(block)){
      if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
        Local def = (Local) stmt.getDefs().get(0);
        Local oriDef = getOriginalLocal(def, localToNameStack.keySet());
        if(!localToNameStack.get(oriDef).isEmpty()){
          localToNameStack.get(oriDef).pop();
        }
      }
    }
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
    throw new RuntimeException(local.toString() + " has no original local!");
  }
}
