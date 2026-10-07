package qilin.pta.toolkits.moon;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.Set;
import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.moon.objcollection.ObjCollector;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import qilin.pta.toolkits.moon.traversal.TraversalInitializer;
import qilin.pta.toolkits.moon.traversal.VFGTraversal;
import qilin.util.Stopwatch;

/**
 * Context-debloating approach that selects the set of "precision-relevant" allocation sites for
 * object-sensitive pointer analysis.
 */
public class Moon {
  public static boolean enableRecursivePRObjs = true;

  private final PTA pta;
  private final int maxMatchLayer;

  public Moon(PTA pta, int maxMatchLayer) {
    this.pta = pta;
    this.maxMatchLayer = maxMatchLayer;
  }

  public Set<AllocNode> analyze() {
    if (maxMatchLayer > 2) {
      throw new UnsupportedOperationException("Moon only supports 2obj or 3obj analysis for now.");
    }
    Stopwatch dataTimer = Stopwatch.newAndStart("#Moon Data Construction");
    MoonDataConstructor.MoonDataStructure moonData = new MoonDataConstructor(pta).analyze();
    dataTimer.stop();
    System.out.println(dataTimer);

    Stopwatch travTimer = Stopwatch.newAndStart("#VFG Traversal for Object Selection");
    TraversalInitializer traversalInitializer = new TraversalInitializer(moonData, maxMatchLayer);
    var objToBaseVar = traversalInitializer.initializeObjToVarMap();
    VFGTraversal traversal = new VFGTraversal(maxMatchLayer, moonData);
    var traversalRet = traversal.traverse(objToBaseVar);
    travTimer.stop();
    System.out.println(travTimer);

    Stopwatch collTimer = Stopwatch.newAndStart("#Precision-Relevant Object Collection");
    ObjCollector objCollector = new ObjCollector(maxMatchLayer, moonData);
    var prObjs = objCollector.analyze(traversalRet);
    collTimer.stop();
    System.out.println(collTimer);

    System.out.println("MOON: Number of Precision-Relevant objects: " + prObjs.size());
    return prObjs;
  }
}
