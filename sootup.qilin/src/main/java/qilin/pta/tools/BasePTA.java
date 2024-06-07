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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import qilin.CoreConfig;
import qilin.core.CorePTA;
import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.builder.CallGraphBuilder;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ContextVarNode;
import qilin.core.pag.GlobalVarNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.Node;
import qilin.core.pag.PAG;
import qilin.core.pag.ValNode;
import qilin.core.pag.VarNode;
import qilin.core.sets.PointsToSet;
import qilin.core.solver.Propagator;
import qilin.core.solver.Solver;
import qilin.stat.IEvaluator;
import qilin.stat.SimplifiedEvaluator;
import qilin.util.PTAUtils;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;

public abstract class BasePTA extends CorePTA {
  protected IEvaluator evaluator;

  public BasePTA(PTAScene scene) {
    super(scene);
    //    this.evaluator = new PTAEvaluator(this);
    this.evaluator = new SimplifiedEvaluator(this);
  }

  public IEvaluator evaluator() {
    return this.evaluator;
  }

  @Override
  protected PAG createPAG() {
    return new PAG(this);
  }

  @Override
  protected CallGraphBuilder createCallGraphBuilder() {
    return new CallGraphBuilder(this);
  }

  @Override
  public Propagator getPropagator() {
    return new Solver(this);
  }

  @Override
  public void run() {
    evaluator.begin();
    pureRun();
    evaluator.end();
    dumpStats();
    System.out.println(evaluator());
  }

  protected void dumpStats() {
    if (CoreConfig.v().getOutConfig().dumppts) {
      dumpPts(this, !CoreConfig.v().getOutConfig().dumplibpts);
    }
  }

  /** dump pts to sootoutput/pts */
  private void dumpPts(PTA pta, boolean appOnly) {
    final String output_dir = CoreConfig.v().getOutConfig().outDir;
    Map<String, Node> nodes = new TreeMap<>();
    try {
      PrintWriter file = new PrintWriter(new File(output_dir, "pts.txt"));
      file.println("Points-to results:");
      for (final ValNode vn : pta.getPag().getValNodes()) {
        if (!(vn instanceof VarNode)) {
          continue;
        }
        SootClass clz = null;
        if (vn instanceof LocalVarNode) {
          SootMethod sm = ((LocalVarNode) vn).getMethod();
          if (sm != null && !PTAUtils.isFakeMainMethod(sm)) {
            clz = getView().getClass(sm.getDeclaringClassType()).get();
          }
        } else if (vn instanceof GlobalVarNode) {
          GlobalVarNode gvn = (GlobalVarNode) vn;
          Object variable = gvn.getVariable();
          if (variable instanceof SootField) {
            SootField sf = (SootField) variable;
            clz = getView().getClass(sf.getDeclaringClassType()).get();
          }
        } else if (vn instanceof ContextVarNode) {
          ContextVarNode cv = (ContextVarNode) vn;
          VarNode varNode = cv.base();
          if (varNode instanceof LocalVarNode) {
            LocalVarNode cvbase = (LocalVarNode) varNode;
            clz = getView().getClass(cvbase.getMethod().getDeclaringClassType()).get();
          } else if (varNode instanceof GlobalVarNode) {
            GlobalVarNode gvn = (GlobalVarNode) varNode;
            Object variable = gvn.getVariable();
            if (variable instanceof SootField) {
              SootField sf = (SootField) variable;
              clz = getView().getClass(sf.getDeclaringClassType()).get();
            }
          }
        }
        if (appOnly && clz != null && !clz.isApplicationClass()) {
          continue;
        }

        String label = PTAUtils.getNodeLabel(vn);
        nodes.put("[" + label + "]", vn);
        file.print(label + " -> {");
        PointsToSet p2set = pta.reachingObjects(vn);

        if (p2set == null || p2set.isEmpty()) {
          file.print(" empty }\n");
          continue;
        }
        for (Iterator<AllocNode> it = p2set.iterator(); it.hasNext(); ) {
          Node n = it.next();
          label = PTAUtils.getNodeLabel(n);
          nodes.put("[" + label + "]", n);
          file.print(" ");
          file.print(label);
        }
        file.print(" }\n");
      }
      nodes.forEach((l, n) -> file.println(l + n));
      file.close();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't dump solution." + e);
    }
  }
}
