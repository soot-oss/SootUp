package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.signatures.MethodSignature;

public class AsmMethodSource extends org.objectweb.asm.commons.JSRInlinerAdapter
    implements de.upb.soot.namespaces.classprovider.IMethodSource {

  public AsmMethodSource(org.objectweb.asm.MethodVisitor mv, int access, String name, String desc, String signature,
      String[] exceptions) {
    super(mv, access, name, desc, signature, exceptions);
  }

  @Override
  public de.upb.soot.core.Body getBody(de.upb.soot.core.SootMethod m) {

    // FIXME: one can adapt here the original method body...

    return null;
  }

  @Override
  public MethodSignature getSignature() {
    // TODO Auto-generated method stub
    return null;
  }
}
