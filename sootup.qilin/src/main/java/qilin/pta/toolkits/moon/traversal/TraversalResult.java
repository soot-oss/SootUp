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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import sootup.core.model.SootMethod;

public class TraversalResult {
  private final List<Set<AllocNode>> matchedContextObjsOfThisAsParam;
  private final List<Set<AllocNode>> matchedContextObjsOfParam;
  private final List<Set<AllocNode>> newlyAllocObjs;
  private final int recordSize;
  private boolean metParamOfAllocatedMethod;
  private final Set<SootMethod> visitedMethods = new HashSet<>();
  private final SparkField field;

  public TraversalResult(int matchLayer, SparkField field) {
    this.field = field;
    matchLayer++;
    recordSize = matchLayer;
    matchedContextObjsOfThisAsParam = new ArrayList<>(matchLayer);
    matchedContextObjsOfParam = new ArrayList<>(matchLayer);
    newlyAllocObjs = new ArrayList<>(matchLayer);
    for (int i = 0; i < matchLayer; i++) {
      matchedContextObjsOfThisAsParam.add(null);
      matchedContextObjsOfParam.add(null);
      newlyAllocObjs.add(null);
    }
  }

  public SparkField getField() {
    return field;
  }

  public Set<SootMethod> getVisitedMethods() {
    return visitedMethods;
  }

  public void addVisitedMethod(SootMethod method) {
    visitedMethods.add(method);
  }

  public void hasMetParamOfAllocatedMethod() {
    this.metParamOfAllocatedMethod = true;
  }

  public boolean isMetParamOfAllocatedMethod() {
    return metParamOfAllocatedMethod;
  }

  public int getRecordSize() {
    return recordSize;
  }

  public void addNewlyAllocObj(AllocNode newlyAllocObj, int index) {
    if (this.newlyAllocObjs.get(index) == null) {
      Set<AllocNode> newlyAllocObjs = new HashSet<>();
      newlyAllocObjs.add(newlyAllocObj);
      this.newlyAllocObjs.set(index, newlyAllocObjs);
    } else {
      this.newlyAllocObjs.get(index).add(newlyAllocObj);
    }
  }

  public void addMatchedContextObjsByThisAsParam(
      Set<AllocNode> matchedContextObjsOfThis, int index) {
    if (matchedContextObjsOfThis.isEmpty()) return;
    if (this.matchedContextObjsOfThisAsParam.get(index) == null) {
      this.matchedContextObjsOfThisAsParam.set(index, new HashSet<>(matchedContextObjsOfThis));
    } else {
      this.matchedContextObjsOfThisAsParam.get(index).addAll(matchedContextObjsOfThis);
    }
  }

  public void addMatchedContextObjsOfParam(Set<AllocNode> matchedContextObjsOfParam, int index) {
    if (matchedContextObjsOfParam.isEmpty()) return;
    if (this.matchedContextObjsOfParam.get(index) == null) {
      this.matchedContextObjsOfParam.set(index, new HashSet<>(matchedContextObjsOfParam));
    } else {
      this.matchedContextObjsOfParam.get(index).addAll(matchedContextObjsOfParam);
    }
  }

  public Set<AllocNode> getMatchedContextObjsOfThis(int index) {
    if (index >= recordSize) return Collections.emptySet();
    Set<AllocNode> matchedContextObjsOfThis = this.matchedContextObjsOfThisAsParam.get(index);
    return matchedContextObjsOfThis == null
        ? Collections.emptySet()
        : Set.copyOf(matchedContextObjsOfThis);
  }

  public Set<AllocNode> getMatchedContextObjsOfParam(int index) {
    if (index >= recordSize) return Collections.emptySet();
    Set<AllocNode> matchedContextObjsOfParam = this.matchedContextObjsOfParam.get(index);
    return matchedContextObjsOfParam == null
        ? Collections.emptySet()
        : Set.copyOf(matchedContextObjsOfParam);
  }

  public Set<AllocNode> getNewlyAllocObjs(int index) {
    if (index >= recordSize) return Collections.emptySet();
    Set<AllocNode> newlyAllocObjs = this.newlyAllocObjs.get(index);
    return newlyAllocObjs == null ? Collections.emptySet() : Set.copyOf(newlyAllocObjs);
  }

  public boolean hasSpecificContextObj(AllocNode ctxObj) {
    return this.matchedContextObjsOfParam.stream().anyMatch(s -> s != null && s.contains(ctxObj))
        || this.matchedContextObjsOfThisAsParam.stream()
            .anyMatch(s -> s != null && s.contains(ctxObj));
  }

  public boolean hasSpecificNewlyAllocObj(AllocNode newlyAllocObj) {
    return this.newlyAllocObjs.stream().anyMatch(s -> s != null && s.contains(newlyAllocObj));
  }

  public boolean hasAnyOfNewlyAllocObjs(Set<AllocNode> newlyAllocObjs) {
    return this.newlyAllocObjs.stream()
        .anyMatch(s -> s != null && !Collections.disjoint(s, newlyAllocObjs));
  }

  public boolean hasAnyOfContextObjsOfThis() {
    return this.matchedContextObjsOfThisAsParam.stream().anyMatch(s -> s != null && !s.isEmpty());
  }

  public Set<AllocNode> getAllNewlyAllocObjs() {
    Set<AllocNode> allNewlyAllocObjs = new HashSet<>();
    this.newlyAllocObjs.stream().filter(s -> s != null).forEach(allNewlyAllocObjs::addAll);
    return allNewlyAllocObjs;
  }

  public boolean hasContextObjsOnLayerOf(int checkLayer) {
    return !getMatchedContextObjsOfParam(checkLayer).isEmpty()
        || !getMatchedContextObjsOfThis(checkLayer).isEmpty();
  }

  public boolean hasNewlyAllocObjs() {
    return newlyAllocObjs.stream()
        .anyMatch(allocNodes -> allocNodes != null && !allocNodes.isEmpty());
  }
}
