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

import com.google.common.collect.ComparisonChain;
import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.*;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Trap;
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
  public Iterable<Stmt> initializeSootMethod(@NonNull StmtGraph<?> stmtGraph) {
    this.graph = stmtGraph;
    JNopStmt needsNopAtEnd = buildTraps(stmtGraph);
    final Collection<Stmt> labeledStmts = getLabeledStmts(stmtGraph, this.traps);

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
      if (trapStmts.contains(stmt) || stmtGraph.isStmtBranchTarget(stmt)) {
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

    if (needsNopAtEnd != null) {
      linearizedStmtGraph.add(needsNopAtEnd);
      labels.put(needsNopAtEnd, String.format(formatString, ++labelCount));
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

  /**
   * returns a (reconstructed) list of traps like the traptable in the bytecode
   *
   * <p>Note: if you need exceptionional flow information in more augmented with the affected
   * blocks/stmts and not just a (reconstructed, possibly more verbose) traptable - have a look at
   * BasicBlock.getExceptionalSuccessor()
   */
  /** hint: little expensive getter - its more of a build/create - currently no overlaps */
  public JNopStmt buildTraps(StmtGraph stmtGraph) {
    // [ms] try to incorporate it into the serialisation of jimple printing so the other half of
    // iteration information is not wasted..
    BlockGraphIteratorAndTrapAggregator it = new BlockGraphIteratorAndTrapAggregator(stmtGraph);
    // it.getTraps() is valid/completely build when the iterator is done.
    Map<Stmt, Integer> stmtsBlockIdx = new IdentityHashMap<>();
    int i = 0;
    // collect BlockIdx positions to sort the traps according to the numbering
    while (it.hasNext()) {
      final BasicBlock<?> nextBlock = it.next();
      stmtsBlockIdx.put(nextBlock.getHead(), i);
      stmtsBlockIdx.put(nextBlock.getTail(), i);
      i++;
    }
    final List<Trap> traps = it.getTraps();
    boolean b = it.getLastStmt() != null;
    if (b) {
      stmtsBlockIdx.put(it.getLastStmt(), i);
    }
    traps.sort(getTrapComparator(stmtsBlockIdx));
    this.traps = traps;
    return it.getLastStmt();
  }

  /** Comparator which sorts the trap output in getTraps() */
  public Comparator<Trap> getTrapComparator(@NonNull Map<Stmt, Integer> stmtsBlockIdx) {
    return (a, b) ->
        ComparisonChain.start()
            .compare(stmtsBlockIdx.get(a.getBeginStmt()), stmtsBlockIdx.get(b.getBeginStmt()))
            .compare(stmtsBlockIdx.get(a.getEndStmt()), stmtsBlockIdx.get(b.getEndStmt()))
            // [ms] would be nice to have the traps ordered by exception hierarchy as well
            .compare(a.getExceptionType().toString(), b.getExceptionType().toString())
            .result();
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
  public Collection<Stmt> getLabeledStmts(StmtGraph stmtGraph, List<Trap> traps) {
    Set<Stmt> stmtList = new HashSet<>();
    Collection<Stmt> stmtGraphNodes = stmtGraph.getNodes();
    for (Stmt stmt : stmtGraphNodes) {
      if (stmt instanceof BranchingStmt) {
        if (stmt instanceof JIfStmt) {
          stmtList.add(
              (Stmt) stmtGraph.getBranchTargetsOf((JIfStmt) stmt).get(JIfStmt.FALSE_BRANCH_IDX));
        } else if (stmt instanceof JGotoStmt) {
          // [ms] bounds are validated in Body if its a valid StmtGraph
          stmtList.add(
              (Stmt) stmtGraph.getBranchTargetsOf((JGotoStmt) stmt).get(JGotoStmt.BRANCH_IDX));
        } else if (stmt instanceof JSwitchStmt) {
          stmtList.addAll(stmtGraph.getBranchTargetsOf((BranchingStmt) stmt));
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
