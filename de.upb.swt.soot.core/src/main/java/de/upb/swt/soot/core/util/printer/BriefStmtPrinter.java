package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;

/** StmtPrinter implementation for normal Jimple */
public class BriefStmtPrinter extends LabeledStmtPrinter {

  public BriefStmtPrinter() {}

  public BriefStmtPrinter(Body body) {
    super(body);
  }

  @Override
  public void method(SootMethod m) {
    handleIndent();
    if (m.isStatic()) {
      output.append(m.getDeclaringClassType().getFullyQualifiedName());
      output.append(".");
    }
    output.append(m.getSignature().getName());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    if (f.isStatic()) {
      output.append(f.getDeclaringClassType().getFullyQualifiedName());
      output.append(".");
    }
    output.append(f.getSignature().getName());
  }

  @Override
  public void identityRef(IdentityRef r) {
    handleIndent();
    if (r instanceof JThisRef) {
      output.append("@this");
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      output.append("@parameter" + pr.getIndex());
    } else if (r instanceof JCaughtExceptionRef) {
      output.append("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  private boolean eatSpace = false;

  @Override
  public void literal(String s) {
    if (eatSpace && s.equals(" ")) {
      eatSpace = false;
      return;
    }
    eatSpace = false;
    if (s.equals(Jimple.STATICINVOKE)
        || s.equals(Jimple.VIRTUALINVOKE)
        || s.equals(Jimple.INTERFACEINVOKE)) {
      eatSpace = true;
      return;
    }

    output.append(s);
  }
}
