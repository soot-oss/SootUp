package de.upb.soot.namespaces.classprovider.asm;

public class AsmMethodSource extends org.objectweb.asm.commons.JSRInlinerAdapter implements de.upb.soot.namespaces.classprovider.IMethodSource {

    public AsmMethodSource(org.objectweb.asm.MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(mv, access, name, desc, signature, exceptions);
    }

    @Override
    public de.upb.soot.core.Body getBody(de.upb.soot.core.SootMethod m, String phaseName) {

        //FIXME: one can adapt here the


        return null;
    }
}
