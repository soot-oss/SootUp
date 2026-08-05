package qilin.pta.toolkits.zipper.flowgraph;

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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ConstantNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.MethodPAG;
import qilin.core.pag.PagNode;
import qilin.pta.toolkits.common.OAG;
import qilin.util.PTAUtils;
import qilin.util.graph.TopologicalSorter;
import qilin.util.queue.QueueReader;
import qilin.util.sets.PointsToSet;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;

/** A tailored Object Allocation Graph (OAG) for Zipper PTA. */
public class ZOAG extends OAG {

  private final SetMultimap<AllocNode, AllocNode> obj2Allocatees =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  private final SetMultimap<Type, AllocNode> type2Allocatees =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final SetMultimap<Type, AllocNode> type2Objs =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  public ZOAG(PTA prepta) {
    super(prepta);
    for (AllocNode obj : this.pta.getPag().getAllocNodes()) {
      type2Objs.put(obj.getType(), obj);
    }
    build();
    computeAllocatees();
  }

  public SetMultimap<Type, AllocNode> getType2Objs() {
    return type2Objs;
  }

  private void computeAllocatees() {
    // compute allocatees of objects
    ZSCCMergedGraph<AllocNode> mg = new ZSCCMergedGraph<>(this);
    TopologicalSorter<ZMergedNode<AllocNode>> sorter = new TopologicalSorter<>();
    Interner<Set<AllocNode>> canonicalizer = Interners.newStrongInterner();
    for (ZMergedNode<AllocNode> node : sorter.reverse_sort(mg)) {
      Set<AllocNode> allocattes = canonicalizer.intern(getAllocatees(node, mg));
      for (AllocNode obj : node.getContent()) {
        obj2Allocatees.putAll(obj, allocattes);
      }
    }

    // compute allocatees of types
    for (Type type : type2Objs.keySet()) {
      var objs = type2Objs.get(type);
      Set<AllocNode> allocatees = new HashSet<>();
      for (AllocNode obj : objs) {
        allocatees.addAll(getAllocateesOf(obj));
      }
      type2Allocatees.putAll(type, canonicalizer.intern(allocatees));
    }
  }

  public Set<AllocNode> getAllocateesOf(Type type) {
    return type2Allocatees.get(type);
  }

  private Set<AllocNode> getAllocatees(ZMergedNode<AllocNode> node, ZSCCMergedGraph<AllocNode> mg) {
    Set<AllocNode> allocatees = new HashSet<>();
    for (ZMergedNode<AllocNode> n : mg.succsOf(node)) {
      // direct allocatees
      allocatees.addAll(n.getContent());
      // indirect allocatees
      AllocNode o = n.getContent().get(0);
      allocatees.addAll(getAllocateesOf(o));
    }

    AllocNode obj = node.getContent().get(0);
    if (node.getContent().size() > 1 || succsOf(obj).contains(obj)) { // self-loop
      // The merged node is a true SCC
      allocatees.addAll(node.getContent());
    }

    return allocatees;
  }

  private Set<AllocNode> getAllocateesOf(AllocNode obj) {
    return obj2Allocatees.get(obj);
  }

  @Override
  protected void buildOAG() {
    Map<LocalVarNode, Set<AllocNode>> pts = PTAUtils.calcStaticThisPTS(this.pta);
    for (SootMethod method : this.pta.getNakedReachableMethods()) {
      if (!pta.getPag().hasBody(method)) {
        continue;
      }
      MethodPAG srcmpag = pta.getPag().getMethodPAG(method);
      MethodNodeFactory srcnf = srcmpag.nodeFactory();
      LocalVarNode thisRef = (LocalVarNode) srcnf.caseThis();
      QueueReader<PagNode> reader = srcmpag.getInternalReader().clone();
      while (reader.hasNext()) {
        PagNode from = reader.next(), to = reader.next();
        if (from instanceof AllocNode tgt) {
          if (PTAUtils.isFakeMainMethod(method)) {
            // special treatment for fake main
            AllocNode src = pta.getRootNode();
            addEdgeWithFilter(src, tgt);
          } else if (method.isStatic()) {
            pts.getOrDefault(thisRef, Collections.emptySet())
                .forEach(src -> addEdgeWithFilter(src, tgt));
          } else {
            PointsToSet thisPts = pta.reachingObjects(thisRef).toCIPointsToSet();
            for (Iterator<AllocNode> it = thisPts.iterator(); it.hasNext(); ) {
              AllocNode src = it.next();
              addEdgeWithFilter(src, tgt);
            }
          }
        }
      }
    }
  }

  private void addEdgeWithFilter(AllocNode from, AllocNode to) {
    if (from.getType() instanceof ArrayType) return;
    if (from instanceof ConstantNode || to instanceof ConstantNode) return;
    Type ftype = from.getType();
    Type ttype = to.getType();
    if (ftype.equals(PTAUtils.STRING) || ttype.equals(PTAUtils.STRING)) return;
    if (ftype.equals(PTAUtils.STRING_BUILDER) || ttype.equals(PTAUtils.STRING_BUILDER)) return;
    if (ftype.equals(PTAUtils.STRING_BUFFER) || ttype.equals(PTAUtils.STRING_BUFFER)) return;

    var view = pta.getView();
    if (PTAUtils.canStoreType(view, ftype, PTAUtils.THROWABLE)
        || PTAUtils.canStoreType(view, ttype, PTAUtils.THROWABLE)) return;

    addEdge(from, to);
  }
}
