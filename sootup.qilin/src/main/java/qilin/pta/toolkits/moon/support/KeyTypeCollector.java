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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import qilin.core.PTA;
import qilin.core.pag.AllocNode;
import qilin.core.pag.SparkField;
import qilin.util.PTAUtils;
import sootup.core.model.SootClass;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

public class KeyTypeCollector {

  private final Set<Type> polyTypes = new HashSet<>();
  private final FieldRecorder fieldRecorder;
  private final View view;

  public KeyTypeCollector(PTA pta, FieldRecorder fieldRecorder) {
    this.fieldRecorder = fieldRecorder;
    this.view = pta.getView();
  }

  private boolean isPolyType(Type type) {
    if (type.equals(PTAUtils.OBJECT)) {
      return true;
    }
    if (type instanceof ClassType classType) {
      Optional<? extends SootClass> oSootClass = view.getClass(classType);
      if (oSootClass.isEmpty()) {
        return false;
      }
      SootClass sootClass = oSootClass.get();
      String name = sootClass.getName();
      String shortName = name.substring(name.lastIndexOf('.') + 1);
      return sootClass.isAbstract() || sootClass.isInterface() || shortName.startsWith("Abstract");
    }
    return false;
  }

  public boolean isConcernedType(Type type) {
    if (type instanceof ArrayType arrayType) {
      type = arrayType.getElementType();
    }
    return isPolyType(type) || this.polyTypes.contains(type);
  }

  private Set<SparkField> fieldsOf(AllocNode node) {
    return fieldRecorder.objToFields.get(node);
  }

  private Set<SparkField> fieldsOf(Type type) {
    if (type instanceof ClassType classType) {
      if (!fieldRecorder.typeToFields.containsKey(type)) {
        for (AllocNode heap : fieldRecorder.objToFields.keySet()) {
          if (PTAUtils.canStoreType(view, heap.getType(), classType)) {
            for (SparkField sparkField : fieldRecorder.objToFields.get(heap)) {
              if (sparkField instanceof qilin.core.pag.Field qField) {
                ClassType declType = qField.getField().getDeclaringClassType();
                if (PTAUtils.canStoreType(view, classType, declType)) {
                  fieldRecorder.typeToFields.put(type, sparkField);
                }
              } else {
                throw new RuntimeException(sparkField + ";" + sparkField.getClass());
              }
            }
          }
        }
      }
      return fieldRecorder.typeToFields.get(type);
    } else {
      return Collections.emptySet();
    }
  }

  public void run(Collection<AllocNode> allObjs) {
    Set<Type> types = new HashSet<>();
    for (AllocNode heap : allObjs) {
      if (heap.getMethod() == null) continue;
      Type type = heap.getType();
      if (type instanceof ArrayType at) {
        Type et = at.getElementType();
        if (isPolyType(et)) {
          polyTypes.add(et);
        } else {
          types.add(et);
        }
      } else {
        for (SparkField field : fieldsOf(heap)) {
          Type ft = field.getType();
          if (ft instanceof ArrayType fat) {
            ft = fat.getElementType();
          }
          if (isPolyType(ft)) {
            polyTypes.add(ft);
            polyTypes.add(type);
          } else {
            types.add(type);
            types.add(ft);
          }
        }
      }
    }
    boolean continueUpdating = true;
    while (continueUpdating) {
      continueUpdating = false;
      for (Type type : types) {
        for (SparkField field : fieldsOf(type)) {
          Type ft = field.getType();
          if (isConcernedType(ft)) {
            if (polyTypes.add(type)) {
              continueUpdating = true;
            }
          }
        }
      }
    }
  }
}
