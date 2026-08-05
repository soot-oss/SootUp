package qilin.pta.toolkits.moon.support;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ArrayElement;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.SparkField;
import qilin.util.collect.twokeymultimap.ConcurrentTwoKeyMultiMap;
import sootup.core.types.Type;

public class FieldRecorder {

  // this fork's ArrayElement is per-PAG (not a JVM-wide singleton), so it's threaded in here
  // rather than compared against a static ArrayElement.v().
  private final ArrayElement arrayElement;

  private final ConcurrentTwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode>
      varToFieldToStoreFromVar = new ConcurrentTwoKeyMultiMap<>();
  private final ConcurrentTwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode>
      varToFieldToLoadedToVar = new ConcurrentTwoKeyMultiMap<>();

  protected final SetMultimap<AllocNode, SparkField> objToFields =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  protected final SetMultimap<Type, SparkField> typeToFields =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  protected final SetMultimap<SparkField, AllocNode> fieldToBaseObjs =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

  public FieldRecorder(PTA pta) {
    this.arrayElement = pta.getPag().getArrayElement();
  }

  public void putLoad(LocalVarNode var, SparkField field, LocalVarNode loadedToVar) {
    if (field instanceof ArrayElement && !field.equals(arrayElement)) {
      throw new RuntimeException("ArrayElement should be the same object");
    }
    varToFieldToLoadedToVar.put(var, field, loadedToVar);
  }

  public void putStore(LocalVarNode var, SparkField field, LocalVarNode storeFromVar) {
    if (field instanceof ArrayElement && !field.equals(arrayElement)) {
      throw new RuntimeException("ArrayElement should be the same object");
    }
    varToFieldToStoreFromVar.put(var, field, storeFromVar);
  }

  public boolean hasStore(LocalVarNode var, AllocNode obj) {
    return hasUsage(varToFieldToStoreFromVar, var, obj);
  }

  public boolean hasLoad(LocalVarNode var, AllocNode obj) {
    return hasUsage(varToFieldToLoadedToVar, var, obj);
  }

  private boolean hasUsage(
      ConcurrentTwoKeyMultiMap<LocalVarNode, SparkField, LocalVarNode> usageMap,
      LocalVarNode var,
      AllocNode obj) {
    if (obj.getType() instanceof sootup.core.types.ArrayType) {
      return usageMap.containsKey(var);
    }
    if (!usageMap.containsKey(var)) return false;
    return usageMap.get(var).keySet().stream().anyMatch(sf -> sf instanceof qilin.core.pag.Field);
  }

  public Set<LocalVarNode> getStoredFromVars(LocalVarNode var, SparkField field) {
    if (field instanceof ArrayElement && !field.equals(arrayElement)) {
      throw new RuntimeException("ArrayElement should be the same object");
    }
    return varToFieldToStoreFromVar.get(var, field);
  }

  public Set<LocalVarNode> arrGetStoredFromVars(LocalVarNode var) {
    return getStoredFromVars(var, arrayElement);
  }

  public Set<LocalVarNode> getLoadedToVars(LocalVarNode var, SparkField field) {
    if (field instanceof ArrayElement && !field.equals(arrayElement)) {
      throw new RuntimeException("ArrayElement should be the same object");
    }
    return varToFieldToLoadedToVar.get(var, field);
  }

  public Set<SparkField> getLoadedFields(LocalVarNode var) {
    return varToFieldToLoadedToVar.get(var).keySet();
  }

  public Set<SparkField> getStoredFields(LocalVarNode var) {
    return varToFieldToStoreFromVar.get(var).keySet();
  }

  public SetMultimap<SparkField, LocalVarNode> getFieldAndStoredFromVars(LocalVarNode var) {
    return varToFieldToStoreFromVar.get(var);
  }

  public Set<SparkField> allFields(KeyTypeCollector keyTypeCollector) {
    return objToFields.values().parallelStream()
        .filter(f -> keyTypeCollector.isConcernedType(f.getType()))
        .collect(Collectors.toSet());
  }

  public void recordObjToField(AllocNode obj, SparkField field) {
    objToFields.put(obj, field);
    typeToFields.put(obj.getType(), field);
    fieldToBaseObjs.put(field, obj);
  }
}
