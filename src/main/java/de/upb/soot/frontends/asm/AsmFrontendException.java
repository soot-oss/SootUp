package de.upb.soot.frontends.asm;

import de.upb.soot.namespaces.classprovider.ResolveException;

/**
 * Exception thrown in the front-End
 */
public class AsmFrontendException extends ResolveException {
    public AsmFrontendException(String message) {
        super(message);
    }
}
