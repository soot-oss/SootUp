package de.upb.soot.util.printer;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.ref.JCaughtExceptionRef;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.Type;

/** IStmtPrinter implementation for normal Jimple */
public class BriefStmtPrinter extends LabeledStmtPrinter {
  public BriefStmtPrinter(Body body) {
    super(body);
  }

  @Override
  public void startStmt(IStmt u) {
    super.startStmt(u);
  }

  @Override
  public void method(SootMethod m) {
    handleIndent();
    if (m.isStatic()) {
      output.append(m.getDeclaringClass().getName());
      literal(".");
    }
    output.append(m.getSignature().getName());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    if (f.isStatic()) {
      output.append(f.getDeclaringClass().getName());
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

  @Override
  public void typeSignature(Type t) {
    handleIndent();
    output.append(t.toString());
  }

  @Override
  public void methodSignature(MethodSignature sig) {
    output.append(sig.toString());
  }

  @Override
  public void fieldSignature(FieldSignature fieldSig) {
    output.append(fieldSig.toString());
  }
}
