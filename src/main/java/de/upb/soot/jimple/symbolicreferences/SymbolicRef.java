package de.upb.soot.jimple.symbolicreferences;

import de.upb.soot.signatures.ISignature;

/**
 * Class to represent symbolic references that must be dispatched
 * See <a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2">https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.3.2</a>
 */
public interface SymbolicRef<T> {

    T resolve();

    ISignature getSignature();
}
