package qilin.pta.toolkits.moon.graph;

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
import qilin.core.pag.ContextAllocNode;
import qilin.core.pag.ContextField;
import qilin.core.pag.SparkField;
import qilin.pta.toolkits.moon.support.PtrSetCache;
import qilin.util.collect.twokeymultimap.TwoKeyMultiHashMap;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class FieldPointsToGraph {

  private final SimpleGraph<AllocNode> fieldPointsToGraph = new SimpleGraph<>();
  private final TwoKeyMultiHashMap<AllocNode, AllocNode, SparkField> objToStoredToField =
      new TwoKeyMultiHashMap<>();
  private final TwoKeyMultiHashMap<AllocNode, SparkField, AllocNode> objToFieldToStored =
      new TwoKeyMultiHashMap<>();
  private final PTA pta;
  private final PtrSetCache ptrSetCache;

  public FieldPointsToGraph(PTA pta, PtrSetCache ptrSetCache) {
    this.pta = pta;
    this.ptrSetCache = ptrSetCache;
    init();
  }

  public void addFieldPointsToGraph(AllocNode baseObj, SparkField field, AllocNode obj) {
    if (baseObj instanceof ContextAllocNode contextAllocNode) {
      baseObj = contextAllocNode.base();
    }
    if (obj instanceof ContextAllocNode contextAllocNode) {
      obj = contextAllocNode.base();
    }
    fieldPointsToGraph.addEdge(baseObj, obj);
    objToStoredToField.put(baseObj, obj, field);
    objToFieldToStored.put(baseObj, field, obj);
  }

  public boolean hasFieldPointsTo(AllocNode baseObj, AllocNode obj) {
    return fieldPointsToGraph.hasEdge(baseObj, obj);
  }

  public Set<AllocNode> getFieldPointsTo(AllocNode baseObj, SparkField field) {
    return objToFieldToStored.get(baseObj, field);
  }

  public Set<SparkField> getAllFieldsOf(AllocNode baseObj) {
    return objToFieldToStored.get(baseObj).keySet();
  }

  public Set<AllocNode> getFieldPointsTo(AllocNode baseObj) {
    return fieldPointsToGraph.getSuccsOf(baseObj);
  }

  public Set<AllocNode> getFieldPointsFrom(AllocNode obj) {
    return fieldPointsToGraph.getPredsOf(obj);
  }

  private void init() {
    for (ContextField contextField : pta.getPag().getContextFields()) {
      Set<AllocNode> pts = ptrSetCache.ptsOf(contextField);
      if (isIgnored(contextField.getField().getType()) || pts.isEmpty()) continue;
      AllocNode baseObj = contextField.getBase();
      if (baseObj instanceof ContextAllocNode contextAllocNode) {
        baseObj = contextAllocNode.base();
      }
      if (baseObj.getMethod() == null) {
        continue;
      }
      AllocNode finalBaseObj = baseObj;
      SparkField field = contextField.getField();
      pts.forEach(o -> addFieldPointsToGraph(finalBaseObj, field, o));
    }
  }

  private boolean isIgnored(Type type) {
    if (type instanceof ArrayType arrayType) {
      type = arrayType.getBaseType();
    }
    return !(type instanceof ClassType);
  }
}
