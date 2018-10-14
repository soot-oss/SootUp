package de.upb.soot.util.printer;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class LabeledStmtPrinter extends AbstractStmtPrinter {
  /** branch targets **/
  protected Map<IStmt, String> labels;
  /** for unit references in Phi nodes **/
  protected Map<IStmt, String> references;

  protected String labelIndent = "\u0020\u0020\u0020\u0020\u0020";

  public LabeledStmtPrinter(Body b) {
    createLabelMaps(b);
  }

  public Map<IStmt, String> labels() {
    return labels;
  }

  public Map<IStmt, String> references() {
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
  public abstract void type(Type t);

  @Override
  public void stmtRef(IStmt u, boolean branchTarget) {
    String oldIndent = getIndent();

    // normal case, ie labels
    if (branchTarget) {
      setIndent(labelIndent);
      handleIndent();
      setIndent(oldIndent);
      String label = labels.get(u);
      if (label == null || "<unnamed>".equals(label)) {
        label = "[?= " + u + "]";
      }
      output.append(label);
    }
    // refs to control flow predecessors (for Shimple)
    else {
      String ref = references.get(u);

      if (startOfLine) {
        String newIndent = "(" + ref + ")" + indent.substring(ref.length() + 2);

        setIndent(newIndent);
        handleIndent();
        setIndent(oldIndent);
      } else {
        output.append(ref);
      }
    }
  }

  private void createLabelMaps(Body body) {
    LinkedHashSet<IStmt> units = body.getStmts();

    labels = new HashMap<IStmt, String>(units.size() * 2 + 1, 0.7f);
    references = new HashMap<IStmt, String>(units.size() * 2 + 1, 0.7f);

    // Create statement name table
    Set<IStmt> labelStmts = new HashSet<IStmt>();
    Set<IStmt> refStmts = new HashSet<IStmt>();

    // Build labelStmts and refStmts
    for (StmtBox box : body.getAllStmtBoxes()) {
      IStmt stmt = box.getStmt();

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
    for (IStmt s : units) {
      if (labelStmts.contains(s)) {
        labels.put(s, String.format(formatString, ++labelCount));
      }

      if (refStmts.contains(s)) {
        references.put(s, Integer.toString(refCount++));
      }
    }
  }

}
