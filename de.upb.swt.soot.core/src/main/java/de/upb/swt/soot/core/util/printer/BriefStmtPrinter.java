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
      literal(".");
    }
    output.append(m.getSignature().getName());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    if (f.isStatic()) {
      output.append(f.getDeclaringClassType().getFullyQualifiedName());
      literal(".");
    }
    output.append(f.getSignature().getName());
  }

  @Override
  public void identityRef(IdentityRef r) {
    handleIndent();
    if (r instanceof JThisRef) {
      literal("@this");
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      literal("@parameter" + pr.getIndex());
    } else if (r instanceof JCaughtExceptionRef) {
      literal("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  private boolean eatSpace = false;

  @Override
  public void literal(String s) {
    handleIndent();
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
