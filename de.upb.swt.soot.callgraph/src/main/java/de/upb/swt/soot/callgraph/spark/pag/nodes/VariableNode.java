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

import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Represents a simple variable node (Green) in the pointer assignment graph. */
public class VariableNode extends Node implements Comparable {

  protected Object variable;
  protected Map<Field, FieldReferenceNode> fields;
  protected int finishingNumber = 0;
  private boolean isInterProcTarget = false;

  public VariableNode(PointerAssignmentGraph pag, Object variable, Type type) {
    super(pag, type);
    this.variable = variable;
    this.fields = new HashMap<>();
    pag.getVariableNodes().add(this);

    int maxFinishingNumber = pag.getMaxFinishingNumber() + 1;
    pag.setMaxFinishingNumber(maxFinishingNumber);
    setFinishingNumber(maxFinishingNumber);
  }

  public void addField(Field field, FieldReferenceNode fieldNode) {
    fields.put(field, fieldNode);
  }

  public Object getVariable() {
    return variable;
  }

  public FieldReferenceNode getField(Field field) {
    return fields.get(field);
  }

  public Collection<FieldReferenceNode> getAllFieldReferences() {
    if (fields == null) {
      return Collections.emptyList();
    }
    return fields.values();
  }

  @Override
  public String toString() {
    return "VariableNode{" + "variable=" + variable + ", fields=" + fields + '}';
  }

  @Override
  public int compareTo(Object o) {
    VariableNode other = (VariableNode) o;
    if (other.finishingNumber == finishingNumber && other != this) {
      throw new RuntimeException(
          MessageFormat.format(
              "Comparison Error:\n"
                  + "This is: {0} with number {1}\n"
                  + "Other is: {2} with number {3}",
              this, this.finishingNumber, other, other.finishingNumber));
    }
    return other.finishingNumber - this.finishingNumber;
  }

  public void setFinishingNumber(int number) {
    finishingNumber = number;
    if (number > pag.getMaxFinishingNumber()) {
      pag.setMaxFinishingNumber(number);
    }
  }
  /**
   * Designates this node as the potential target of a interprocedural assignment edge which may be
   * added during on-the-fly call graph updating.
   */
  public void setInterProcTarget() {
    isInterProcTarget = true;
  }

  /**
   * Returns true if this node is the potential target of a interprocedural assignment edge which
   * may be added during on-the-fly call graph updating.
   */
  public boolean isInterProcTarget() {
    return isInterProcTarget;
  }
}
