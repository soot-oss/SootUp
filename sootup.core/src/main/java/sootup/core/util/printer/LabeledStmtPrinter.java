package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003-2020 Ondrej Lhotak, Linghui Luo, Markus Schmidt
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

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;

public abstract class LabeledStmtPrinter extends AbstractStmtPrinter {
  /** branch targets * */
  protected Map<Stmt, String> labels;

  /**
   * for stmt references in Phi nodes (ms: and other occurences TODO: check and improve comment) *
   */
  protected Map<Stmt, String> references;

  public LabeledStmtPrinter() {}

  public Map<Stmt, String> getLabels() {
    return labels;
  }

  public Map<Stmt, String> getReferences() {
    return references;
  }

  @Override
  public abstract void literal(String s);

  @Override
  public abstract void method(SootMethod m);

  @Override
  public abstract void field(SootField f);

  @Override
  public abstract void identityRef(IdentityRef r);

  @Override
  public void stmtRef(Stmt stmt, boolean branchTarget) {

    // normal case, ie labels
    if (branchTarget) {

      setIndent(-indentStep / 2);
      handleIndent();
      setIndent(indentStep / 2);

      String label = labels.get(stmt);
      if (label == null) {
        output.append("[?= ").append(Jimple.escape(stmt.toString())).append(']');
      } else {
        output.append(Jimple.escape(label));
      }

    } else {

      String ref = references.get(stmt);

      if (startOfLine) {
        setIndent(-indentStep / 2);
        handleIndent();
        setIndent(indentStep / 2);

        output.append('(').append(Jimple.escape(ref)).append(')');
      } else {
        output.append(Jimple.escape(ref));
      }
    }
  }

  /**
   * createLabelMaps
   *
   * @return the linearized StmtGraph
   */
  public Iterable<Stmt> initializeSootMethod(@Nonnull StmtGraph<?> stmtGraph) {
    this.graph = stmtGraph;
    final List<Stmt> linearizedStmtGraph = getStmts(stmtGraph);
    return linearizedStmtGraph;
  }

  @Nonnull
  public List<Stmt> getStmts(@Nonnull StmtGraph<?> stmtGraph) {
    final Collection<Stmt> targetStmtsOfBranches = stmtGraph.getLabeledStmts();
    final List<Trap> traps = stmtGraph.getTraps();

    final int maxEstimatedSize = targetStmtsOfBranches.size() + traps.size() * 3;
    labels = new HashMap<>(maxEstimatedSize, 1);
    references = new HashMap<>(maxEstimatedSize, 1);

    // Create statement name table
    Set<Stmt> labelStmts = new HashSet<>();
    Set<Stmt> refStmts = new HashSet<>();

    Set<Stmt> trapStmts = new HashSet<>();
    traps.forEach(
        trap -> {
          trapStmts.add(trap.getHandlerStmt());
          trapStmts.add(trap.getBeginStmt());
          trapStmts.add(trap.getEndStmt());
        });

    // Build labelStmts and refStmts -> is stmt head of a block (as its a branch target/trapHandler
    // or is the begin of a trap-range) or does it mark the end of a trap range
    // does it need a label
    for (Stmt stmt : targetStmtsOfBranches) {
      if (stmtGraph.isStmtBranchTarget(stmt) || trapStmts.contains(stmt)) {
        labelStmts.add(stmt);
      } else {
        refStmts.add(stmt);
      }
    }

    // left side zero padding for all labels
    // this simplifies debugging the jimple code in simple editors, as it
    // avoids the situation where a label is the prefix of another label
    final int maxDigits = 1 + (int) Math.log10(labelStmts.size());
    final String formatString = "label%0" + maxDigits + "d";

    int labelCount = 0;
    int refCount = 0;

    // Traverse the stmts and assign a label if necessary
    final List<Stmt> linearizedStmtGraph = stmtGraph.getStmts();
    for (Stmt s : linearizedStmtGraph) {
      if (labelStmts.contains(s)) {
        labels.put(s, String.format(formatString, ++labelCount));
      }

      if (refStmts.contains(s)) {
        references.put(s, Integer.toString(refCount++));
      }
    }
    return linearizedStmtGraph;
  }

  @Override
  public void methodSignature(MethodSignature methodSig) {
    output.append('<');
    typeSignature(methodSig.getDeclClassType());
    output.append(": ");
    typeSignature(methodSig.getType());
    output.append(' ').append(Jimple.escape(methodSig.getName())).append('(');

    final List<Type> parameterTypes = methodSig.getSubSignature().getParameterTypes();
    final int parameterTypesSize = parameterTypes.size();
    if (parameterTypesSize > 0) {
      typeSignature(parameterTypes.get(0));
      for (int i = 1; i < parameterTypesSize; i++) {
        output.append(',');
        typeSignature(parameterTypes.get(i));
      }
    }
    output.append(")>");
  }

  @Override
  public void fieldSignature(FieldSignature fieldSig) {
    output.append('<');
    typeSignature(fieldSig.getDeclClassType());
    output.append(": ");
    final FieldSubSignature subSignature = fieldSig.getSubSignature();
    typeSignature(subSignature.getType());
    output.append(' ').append(Jimple.escape(subSignature.getName())).append('>');
  }
}
