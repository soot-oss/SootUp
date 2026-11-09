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
import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.ArrayElement;
import qilin.core.pag.PAG;
import qilin.core.pag.SparkField;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class ContainerCollector {

  private final PAG pag;
  private final FieldRecorder fieldRecorder;
  private final FieldFlowRecorder fieldReachabilityRecorder;
  protected final SetMultimap<AllocNode, SparkField> containerToFields =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);
  private final KeyTypeCollector keyTypeCollector;

  public ContainerCollector(
      PTA pta,
      FieldRecorder fieldRecorder,
      FieldFlowRecorder fieldReachabilityRecorder,
      KeyTypeCollector keyTypeCollector) {
    this.pag = pta.getPag();
    this.fieldRecorder = fieldRecorder;
    this.fieldReachabilityRecorder = fieldReachabilityRecorder;
    this.keyTypeCollector = keyTypeCollector;
  }

  public void collect() {
    ArrayElement arrayElement = pag.getArrayElement();
    Set<AllocNode> toBeDetermined = ConcurrentHashMap.newKeySet();
    pag.getAllocNodes().parallelStream()
        .forEach(
            heap -> {
              if (qilinHack(heap)) {
                containerToFields.put(heap, arrayElement);
                return;
              }
              Type type = heap.getType();
              if (type instanceof ArrayType arrayType) {
                JNewArrayExpr arrayExpr = (JNewArrayExpr) heap.getNewExpr();
                Immediate arrLen = arrayExpr.getSize();
                if (!(arrLen instanceof IntConstant intArrLen) || intArrLen.getValue() > 0) {
                  if (keyTypeCollector.isConcernedType(arrayType))
                    containerToFields.put(heap, arrayElement);
                }
              } else if (type instanceof ClassType classType) {
                if (keyTypeCollector.isConcernedType(classType) && heap.getMethod() != null) {
                  toBeDetermined.add(heap);
                }
              }
            });

    toBeDetermined.parallelStream()
        .forEach(
            heap -> {
              Set<SparkField> fields = fieldRecorder.objToFields.get(heap);
              for (SparkField field : fields) {
                if (!keyTypeCollector.isConcernedType(field.getType())) continue;
                if (fieldReachabilityRecorder.isConnceredField(heap, field)) {
                  containerToFields.put(heap, field);
                }
              }
            });
  }

  private boolean qilinHack(AllocNode heap) {
    // this is the same hack as DebloaterX[OOPSLA'23], due to the defect in Qilin framework
    if (heap.getMethod() == null) return false;
    String sig = heap.getMethod().getSignature().toString();
    return sig.startsWith(
            "<java.util.Arrays: java.lang.Object[] copyOf(java.lang.Object[],int,java.lang.Class)>")
        || sig.startsWith(
            "<java.util.AbstractCollection: java.lang.Object[] toArray(java.lang.Object[])>");
  }
}
