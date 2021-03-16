package de.upb.swt.soot.callgraph.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Kadiray Karakaya and others
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
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.signatures.MethodSignature;

/** Method Signature with its calling CallGraphEdgeType */
public class CalleeMethodSignature {

  private CallGraphEdgeType edgeType;
  private MethodSignature methodSignature;

  /**
   * The unit at which the call occurs; may be null for calls not occurring at a specific statement
   * (eg. calls in native code)
   */
  private Stmt sourceStmt;

  public CalleeMethodSignature(
      MethodSignature methodSignature, CallGraphEdgeType edgeType, Stmt sourceStmt) {
    this.methodSignature = methodSignature;
    this.edgeType = edgeType;
    this.sourceStmt = sourceStmt;
  }

  public MethodSignature getMethodSignature() {
    return methodSignature;
  }

  public CallGraphEdgeType getEdgeType() {
    return edgeType;
  }

  public Stmt getSourceStmt() {
    return sourceStmt;
  }
}
