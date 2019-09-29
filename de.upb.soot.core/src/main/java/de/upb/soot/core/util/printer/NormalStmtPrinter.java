package de.upb.soot.core.util.printer;

import de.upb.soot.core.jimple.common.ref.IdentityRef;
import de.upb.soot.core.jimple.common.ref.JCaughtExceptionRef;
import de.upb.soot.core.jimple.common.ref.JParameterRef;
import de.upb.soot.core.jimple.common.ref.JThisRef;
import de.upb.soot.core.model.Body;
import de.upb.soot.core.model.SootField;
import de.upb.soot.core.model.SootMethod;
import de.upb.soot.core.signatures.FieldSignature;
import de.upb.soot.core.signatures.MethodSignature;
import de.upb.soot.core.types.Type;

/** StmtPrinter implementation for normal (full) Jimple */
public class NormalStmtPrinter extends LabeledStmtPrinter {

  public NormalStmtPrinter(Body b) {
    super(b);
  }

  @Override
  public void typeSignature(Type t) {
    handleIndent();
    String s = t == null ? "<null>" : t.toString();
    output.append(s);
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
      literal("@this: ");
      typeSignature(r.getType());
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      literal("@parameter" + pr.getIndex() + ": ");
      typeSignature(r.getType());
    } else if (r instanceof JCaughtExceptionRef) {
      literal("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public void literal(String s) {
    handleIndent();
    output.append(s);
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
