package de.upb.swt.soot.callgraph.spark.pag.nodes;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;
import java.util.HashMap;
import java.util.Map;

public class AllocationNode extends Node {
  /*
  Each allocation node has an associated type, and all objects that it represents are
  expected to have exactly this type at run-time (not a subtype)
   */
  private Object newExpr;
  private SootMethod method;
  private Map<Field, AllocationDotField> fields;

  public AllocationNode(Type type, Object newExpr, SootMethod method) {
    this.type = type;
    this.newExpr = newExpr;
    this.method = method;
  }

  public Object getNewExpr() {
    return newExpr;
  }

  public SootMethod getMethod() {
    return method;
  }

  public void addField(AllocationDotField allocationDotField, Field field) {
    if (fields == null) {
      fields = new HashMap<>();
    }
    fields.put(field, allocationDotField);
  }

  public AllocationDotField dot(Field field) {
    return fields == null ? null : fields.get(field);
  }

  @Override
  public String toString() {
    return "AllocationNode{"
        + "newExpr="
        + newExpr
        + ", method="
        + method
        + ", fields="
        + fields
        + '}';
  }
}
