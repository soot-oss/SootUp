package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import java.util.*;

public abstract class LabeledStmtPrinter extends AbstractStmtPrinter {
  /** branch targets * */
  protected Map<Stmt, String> labels;
  /** for stmt references in Phi nodes * */
  protected Map<Stmt, String> references;

  public LabeledStmtPrinter() {}

  public LabeledStmtPrinter(Body b) {
    createLabelMaps(b);
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
  public void typeSignature(Type t) {
    handleIndent();
    String s = t == null ? "<null>" : type(t);
    output.append(s);
  }

  @Override
  public void stmtRef(Stmt u, boolean branchTarget) {

    // normal case, ie labels
    if (branchTarget) {

      // is it a label? (otherwise its a target of a goto stmt)
      if (startOfLine) {
        decIndent();
        handleIndent();
        output.append(indentStep).append(indentStep);
        incIndent();
      }

      String label = labels.get(u);
      if (label == null || "<unnamed>".equals(label)) {
        label = "[?= " + u + "]";
      }
      output.append(label);
    }
    // TODO: [ms] still necessary? (-> shimple is not supported anymore)
    // refs to control flow predecessors (for Shimple)
    else {
      String ref = references.get(u);

      if (startOfLine) {
        decIndent();
        handleIndent();
        output.append(indentStep).append(indentStep);
        incIndent();

        output.append("(" + ref + ")");
      } else {
        output.append(ref);
      }
    }
  }

  public void createLabelMaps(Body body) {
    Collection<Stmt> stmts = body.getStmts();

    labels = new HashMap<>(stmts.size() * 2 + 1, 0.7f);
    references = new HashMap<>(stmts.size() * 2 + 1, 0.7f);

    // Create statement name table
    Set<Stmt> labelStmts = new HashSet<>();
    Set<Stmt> refStmts = new HashSet<>();

    // Build labelStmts and refStmts
    for (StmtBox box : body.getAllStmtBoxes()) {
      Stmt stmt = box.getStmt();

      if (box.isBranchTarget()) {
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

      output
          .append("<")
          .append(type(methodSig.getDeclClassType()))
          .append(": ")
          .append(type(methodSig.getType()))
          .append(" ")
          .append(methodSig.getName())
          .append("(");

      final List<Type> parameterTypes = methodSig.getSubSignature().getParameterTypes();
      for (Type parameterType : parameterTypes) {
        output.append(type(parameterType));
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
      output
          .append("<")
          .append(type(fieldSig.getDeclClassType()))
          .append(": ")
          .append(type(fieldSig.getSubSignature().getType()))
          .append(" ")
          .append(fieldSig.getSubSignature().getName())
          .append('>');
    } else {
      output.append(fieldSig.toString());
    }
  }
}
