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
import org.jspecify.annotations.NonNull;
import sootup.core.graph.*;
import sootup.core.jimple.JimpleUtils;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
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

  private List<Trap> traps;

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
        output.append("[?= ").append(JimpleUtils.escape(stmt.toString())).append(']');
      } else {
        output.append(JimpleUtils.escape(label));
      }

    } else {

      String ref = references.get(stmt);

      if (startOfLine) {
        setIndent(-indentStep / 2);
        handleIndent();
        setIndent(indentStep / 2);

        output.append('(').append(JimpleUtils.escape(ref)).append(')');
      } else {
        output.append(JimpleUtils.escape(ref));
      }
    }
  }

  /**
   * createLabelMaps
   *
   * @return the linearized ControlFlowGraph
   */
  public Iterable<Stmt> initializeSootMethod(@NonNull ControlFlowGraph<?> controlFlowGraph) {
    this.graph = controlFlowGraph;
    JNopStmt needsNopAtEnd = buildTraps(controlFlowGraph);
    final Collection<Stmt> labeledStmts = getLabeledStmts(controlFlowGraph, this.traps);

    final int maxEstimatedSize = labeledStmts.size() + traps.size() * 3;
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
    for (Stmt stmt : labeledStmts) {
      if (trapStmts.contains(stmt) || controlFlowGraph.isStmtBranchTarget(stmt)) {
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
    final List<Stmt> linearizedControlFlowGraph = controlFlowGraph.getStmts();
    for (Stmt s : linearizedControlFlowGraph) {
      if (labelStmts.contains(s)) {
        labels.put(s, String.format(formatString, ++labelCount));
      }

      if (refStmts.contains(s)) {
        references.put(s, Integer.toString(refCount++));
      }
    }

    if (needsNopAtEnd != null) {
      linearizedControlFlowGraph.add(needsNopAtEnd);
      labels.put(needsNopAtEnd, String.format(formatString, ++labelCount));
    }

    return linearizedControlFlowGraph;
  }

  @Override
  public void methodSignature(MethodSignature methodSig) {
    output.append('<');
    typeSignature(methodSig.getDeclClassType());
    output.append(": ");
    typeSignature(methodSig.getType());
    output.append(' ').append(JimpleUtils.escape(methodSig.getName())).append('(');

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
    output.append(' ').append(JimpleUtils.escape(subSignature.getName())).append('>');
  }

  /**
   * returns a (reconstructed) list of traps like the traptable in the bytecode
   *
   * <p>Note: if you need exceptionional flow information in more augmented with the affected
   * blocks/stmts and not just a (reconstructed, possibly more verbose) traptable - have a look at
   * BasicBlock.getExceptionalSuccessor()
   */
  /** hint: little expensive getter - its more of a build/create - currently no overlaps */
  public JNopStmt buildTraps(ControlFlowGraph<?> controlFlowGraph) {
    this.traps = controlFlowGraph.getTraps();
    // a trap that extends to the end of the graph has an end stmt that is a synthetic stmt not
    // part of the graph - remember it as it needs a label in initializeSootMethod()
    JNopStmt needsNopAtEnd = null;
    for (Trap trap : this.traps) {
      if (!controlFlowGraph.containsNode(trap.getEndStmt())) {
        needsNopAtEnd = (JNopStmt) trap.getEndStmt();
      }
    }
    return needsNopAtEnd;
  }

  /**
   * Returns the result of iterating through all Stmts in this body. All Stmts thus found are
   * returned. Branching Stmts and statements which use PhiExpr will have Stmts; a Stmt contains a
   * Stmt that is either a target of a branch or is being used as a pointer to the end of a CFG
   * block.
   *
   * <p>This method was typically used for pointer patching, e.g. when the unit chain is cloned.
   *
   * @return A collection of all the Stmts that are targets of a BranchingStmt
   */
  @NonNull
  public Collection<Stmt> getLabeledStmts(ControlFlowGraph<?> controlFlowGraph, List<Trap> traps) {
    Set<Stmt> stmtList = new HashSet<>();
    Collection<Stmt> controlFlowGraphNodes = controlFlowGraph.getNodes();
    for (Stmt stmt : controlFlowGraphNodes) {
      if (stmt instanceof BranchingStmt) {
        if (stmt instanceof JIfStmt) {
          stmtList.add(
              (Stmt)
                  controlFlowGraph
                      .getBranchTargetsOf((JIfStmt) stmt)
                      .get(JIfStmt.FALSE_BRANCH_IDX));
        } else if (stmt instanceof JGotoStmt) {
          // [ms] bounds are validated in Body if its a valid ControlFlowGraph
          stmtList.add(
              (Stmt)
                  controlFlowGraph.getBranchTargetsOf((JGotoStmt) stmt).get(JGotoStmt.BRANCH_IDX));
        } else if (stmt instanceof JSwitchStmt) {
          stmtList.addAll(controlFlowGraph.getBranchTargetsOf((BranchingStmt) stmt));
        }
      }
    }

    for (Trap trap : traps) {
      stmtList.add(trap.getBeginStmt());
      stmtList.add(trap.getEndStmt());
      stmtList.add(trap.getHandlerStmt());
    }

    return stmtList;
  }

  public List<Trap> getTraps() {
    return traps;
  }
}
