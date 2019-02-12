package de.upb.soot.jimple.symbolicreferences;

import de.upb.soot.signatures.ISignature;

/**
 * Class to represent symbolic references that must be dispatched
 * See <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2">https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2</a>
 * @author Andreas Dann
 * /

/**
 * FIXME: this classes hold a *Signature
 * The method "resolve" implements the resolution process of the JVM (similar to the SootMethodRef and SootFieldRef in old Soot)
 * We should check if it makes sense to embed this "resolution logic" into the corresponding jimple classes, e.g.,
 * JVirtualInvokeExpr, JStaticInvokeExpr, JInstanceFieldRef,...
 *
 *
 *
 * @param <T>
 */
public interface SymbolicRef<T> {

    T resolve();

    ISignature getSignature();
}
