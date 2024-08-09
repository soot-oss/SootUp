/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.pta.tools;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import qilin.core.PTAScene;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.Parm;
import qilin.pta.toolkits.eagle.Eagle;
import qilin.util.Pair;
import qilin.util.Stopwatch;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.stmt.Stmt;

/*
 * refer to "Precision-Preserving Yet Fast Object-Sensitive Pointer Analysis with Partial Context Sensitivity" (OOPSLA'19)
 * and "Eagle: CFL-Reachability-based Precision-Preserving Acceleration of Object-Sensitive Pointer Analysis with Partial Context Sensitivity"
 * (TOSEM'21)
 * */
public class EaglePTA extends PartialObjSensPTA {
  public EaglePTA(PTAScene scene, int ctxLen) {
    super(scene, ctxLen);
    System.out.println("Eagle ....");
  }

  @Override
  protected Map<Object, Integer> calculatingNode2Length() {
    Stopwatch timer = Stopwatch.newAndStart("TransGraph Construction");
    Eagle eagle = new Eagle();
    eagle.buildGraph(prePTA);
    timer.stop();
    System.out.println(timer);
    Stopwatch eagleTimer = Stopwatch.newAndStart("Eagle Selection");
    Map<Object, Integer> ret = eagle.contxtLengthAnalysis();
    eagleTimer.stop();
    System.out.println(eagleTimer);
    eagle.dumpCount();
    //        try {
    //            writeToFile(ret);
    //        } catch (FileNotFoundException e) {
    //            System.out.println("no file exists for dumping eagle selected partial
    // heaps/variables.");
    //        }
    System.out.println("#Node:" + eagle.totalNodesCount());
    System.out.println("#Edge:" + eagle.totalEdgesCount());
    return ret;
  }

  protected void writeToFile(Map<Object, Integer> partialResults) throws FileNotFoundException {
    final char EOL = '\n';
    String insensVarFile = "InsensitiveVar.facts";
    String insensHeapFile = "InsensitiveHeap.facts";
    try (PrintWriter varWriter = new PrintWriter(insensVarFile); PrintWriter heapWriter = new PrintWriter(insensHeapFile)) {
      partialResults.forEach(
              (k, v) -> {
                if (v > 0) {
                  return;
                }
                if (k instanceof AllocNode) {
                  AllocNode heap = (AllocNode) k;
                  if (heap.getMethod() == null) {
                    return;
                  }
                  String newExpr = heap.getNewExpr().toString();
                  if (heap.getNewExpr() instanceof JNewArrayExpr) {
                    JNewArrayExpr nae = (JNewArrayExpr) heap.getNewExpr();
                    newExpr = "new " + nae.getBaseType() + "[]";
                  }
                  String heapSig = heap.getMethod().toString() + "/" + newExpr;
                  heapWriter.write(heapSig);
                  heapWriter.write(EOL);
                } else if (k instanceof LocalVarNode) {
                  LocalVarNode lvn = (LocalVarNode) k;
                  Object variable = lvn.getVariable();
                  String varName = variable.toString();
                  if (variable instanceof Parm) {
                    Parm parm = (Parm) variable;
                    if (parm.isThis()) {
                      varName = "@this";
                    } else if (parm.isReturn()) {
                      return;
                    } else if (parm.isThrowRet()) {
                      return;
                    } else {
                      varName = "@parameter" + parm.getIndex();
                    }
                  } else if (variable instanceof Local) {

                  } else if (variable instanceof Stmt) {
                    return;
                  } else if (variable instanceof Expr) {
                    return;
                  } else if (variable instanceof Pair) {
                    return;
                  } else {
                    return;
                  }
                  String varSig = lvn.getMethod().toString() + "/" + varName;
                  varWriter.write(varSig);
                  varWriter.write(EOL);
                }
              });
    }
  }
}
