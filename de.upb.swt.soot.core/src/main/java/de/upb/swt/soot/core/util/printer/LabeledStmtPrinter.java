package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import java.util.*;

public abstract class LabeledStmtPrinter extends AbstractStmtPrinter {
  /** branch targets * */
  protected Map<Stmt, String> labels;

  /**
   * for stmt references in Phi nodes (ms: and other occurences TODO: check and improve comment) *
   */
  protected Map<Stmt, String> references;

  public LabeledStmtPrinter(Body b) {
    super(b);
    initializeSootMethod(b);
  }

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
        output.append("[?= ").append(stmt).append(']');
      } else {
        output.append(label);
      }

    } else {

      String ref = references.get(stmt);

      if (startOfLine) {
        setIndent(-indentStep / 2);
        handleIndent();
        setIndent(indentStep / 2);

        output.append('(').append(ref).append(')');
      } else {
        output.append(stmt);
      }
    }
  }

  /** createLabelMaps */
  public void initializeSootMethod(Body body) {

    Collection<Stmt> stmts = body.getStmts();

    labels = new HashMap<>(stmts.size() * 2 + 1, 0.7f);
    references = new HashMap<>(stmts.size() * 2 + 1, 0.7f);

    // Create statement name table
    Set<Stmt> labelStmts = new HashSet<>();
    Set<Stmt> refStmts = new HashSet<>();

    Set<Stmt> trapStmts = new HashSet<>();
    body.getTraps()
        .forEach(
            trap -> {
              trapStmts.add(trap.getHandlerStmt());
              trapStmts.add(trap.getBeginStmt());
              trapStmts.add(trap.getEndStmt());
            });

    // Build labelStmts and refStmts
    // TODO: make 2 loops access body directly; maybe remove getAssociatedStmts()?
    final Collection<Stmt> targetStmtsOfBranches = body.getTargetStmtsInBody();
    for (Stmt stmt : targetStmtsOfBranches) {
      if (body.isStmtBranchTarget(stmt) || trapStmts.contains(stmt)) {
        labelStmts.add(stmt);
      } else {
        // i.e. traps?
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
    // TODO: [ms] improve: iterate over labelStmts/refStmts
    for (Stmt s : stmts) {
      if (labelStmts.contains(s)) {
        labels.put(s, String.format(formatString, ++labelCount));
      }

      if (refStmts.contains(s)) {
        references.put(s, Integer.toString(refCount++));
      }
    }
  }

  @Override
  public void methodSignature(MethodSignature methodSig) {
    if (useImports) {
      output.append('<');
      typeSignature(methodSig.getDeclClassType());
      output.append(": ");
      typeSignature(methodSig.getType());
      output.append(' ').append(methodSig.getName()).append('(');

      final List<Type> parameterTypes = methodSig.getSubSignature().getParameterTypes();
      for (Type parameterType : parameterTypes) {
        typeSignature(parameterType);
        output.append(',');
      }
      if (parameterTypes.size() > 0) {
        output.setLength(output.length() - 1);
      }
      output.append(")>");
    } else {
      output.append(methodSig.toString());
    }
  }

  @Override
  public void fieldSignature(FieldSignature fieldSig) {
    if (useImports) {
      output.append('<');
      typeSignature(fieldSig.getDeclClassType());
      output.append(": ");
      final FieldSubSignature subSignature = fieldSig.getSubSignature();
      typeSignature(subSignature.getType());
      output.append(' ').append(subSignature.getName()).append('>');
    } else {
      output.append(fieldSig.toString());
    }
  }
}
