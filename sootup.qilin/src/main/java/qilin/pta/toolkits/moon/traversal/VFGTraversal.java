package qilin.pta.toolkits.moon.traversal;

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

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import qilin.core.pag.AllocNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.SparkField;
import qilin.pta.toolkits.common.OAG;
import qilin.pta.toolkits.moon.support.FieldFlowRecorder;
import qilin.pta.toolkits.moon.support.FieldRecorder;
import qilin.pta.toolkits.moon.support.MoonDataConstructor;
import sootup.core.model.SootMethod;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public class VFGTraversal {
  private final int objContextLen;
  private final MoonDataConstructor.MoonDataStructure moonData;

  public VFGTraversal(int objContextLen, MoonDataConstructor.MoonDataStructure moonData) {
    this.objContextLen = objContextLen;
    this.moonData = moonData;
  }

  public List<SetMultimap<AllocNode, TraversalResult>> traverse(
      SetMultimap<AllocNode, LocalVarNode> objToBaseVar) {
    List<SetMultimap<AllocNode, TraversalResult>> objToMatchRets = new ArrayList<>(objContextLen);
    for (int i = 0; i < objContextLen; i++) {
      objToMatchRets.add(
          Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet));
    }

    System.out.println(
        "Number of container objects after filtering: " + objToBaseVar.keySet().size());
    FieldRecorder fieldRecorder = moonData.fieldRecorder();
    FieldFlowRecorder fieldFlowRecorder = moonData.fieldFlowRecorder();
    OAG oag = moonData.oag();

    StoredVarTraverser storedVarTraverser = new StoredVarTraverser(moonData, objContextLen);
    objToBaseVar.keySet().parallelStream()
        .forEach(
            obj ->
                objToBaseVar
                    .get(obj)
                    .forEach(
                        baseVar -> {
                          if (notInAllocatorMethod(obj, baseVar) || baseVar.getMethod().isStatic())
                            return;
                          var fieldToStoredFromVars =
                              fieldRecorder.getFieldAndStoredFromVars(baseVar);
                          for (SparkField storedField : fieldToStoredFromVars.keySet()) {
                            for (LocalVarNode storedVar : fieldToStoredFromVars.get(storedField)) {
                              if (storedVar.getType() instanceof NullType
                                  || isIgnoredField(obj, storedField)
                                  || !fieldFlowRecorder.isConnceredField(obj, storedField))
                                continue;
                              // perform VFG traversal from storedVar
                              int toBeCheckedLen = determineToBeCheckedLen(obj, oag);
                              TraversalResult traversalResult =
                                  storedVarTraverser.findSourceOfVarStoredIn(
                                      obj, storedVar, objContextLen, storedField);
                              objToMatchRets.get(toBeCheckedLen - 1).put(obj, traversalResult);
                            }
                          }
                        }));
    return objToMatchRets;
  }

  private int determineToBeCheckedLen(AllocNode obj, OAG oag) {
    int toBeCheckedLen;
    if (this.objContextLen == 1) {
      toBeCheckedLen = 1;
    } else if (this.objContextLen == 2) {
      if (!(obj.getType() instanceof ArrayType) && oag.getPredsOf(obj).size() == 1)
        toBeCheckedLen = 2;
      else toBeCheckedLen = 1;
    } else {
      throw new RuntimeException("Unsupported objContextLen: " + this.objContextLen);
    }
    return toBeCheckedLen;
  }

  private boolean notInAllocatorMethod(AllocNode obj, LocalVarNode var) {
    SootMethod inMethod = var.getMethod();
    var objToInvokedMethods = moonData.objToIvkMtds();
    var oag = moonData.oag();
    if (!objToInvokedMethods.containsKey(obj) || objToInvokedMethods.get(obj).contains(inMethod))
      return false;
    Set<AllocNode> allocators = oag.getPredsOf(obj);
    return allocators.stream()
        .noneMatch(
            o ->
                objToInvokedMethods.containsKey(o)
                    && objToInvokedMethods.get(o).contains(inMethod));
  }

  private boolean isIgnoredField(AllocNode obj, SparkField field) {
    boolean isRefType;

    if (obj.getType() instanceof ArrayType arrayType) {
      isRefType = arrayType.getBaseType() instanceof ClassType;
    } else {
      Type type = field.getType();
      if (type instanceof PrimitiveType) isRefType = false;
      else if (type instanceof ClassType) isRefType = true;
      else if (type instanceof ArrayType arrayType) {
        isRefType = arrayType.getBaseType() instanceof ClassType;
      } else {
        throw new RuntimeException("Unexpected type: " + type);
      }
    }
    return !isRefType;
  }
}
