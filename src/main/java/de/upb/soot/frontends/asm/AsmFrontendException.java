package de.upb.soot.frontends.asm;

import java.util.Optional;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.classprovider.ResolveException;

/**
 * Excepetion thrown in the front-End
 */
public class AsmFrontendException extends ResolveException {
    public AsmFrontendException(String s) {
    }
}
