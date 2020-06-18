package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;

/** StmtPrinter implementation for normal (full) Jimple */
public class NormalStmtPrinter extends LabeledStmtPrinter {

  public NormalStmtPrinter(Body b) {
    super(b);
  }

  @Override
  public void method(SootMethod m) {
    handleIndent();
    output.append(m.getSignature());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    output.append(f.getSignature());
  }

  @Override
  public void identityRef(IdentityRef r) {
    handleIndent();
    if (r instanceof JThisRef) {
      output.append("@this: ");
      typeSignature(r.getType());
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      output.append("@parameter" + pr.getIndex() + ": ");
      typeSignature(r.getType());
    } else if (r instanceof JCaughtExceptionRef) {
      output.append("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public void literal(String s) {
    output.append(s);
  }
}
