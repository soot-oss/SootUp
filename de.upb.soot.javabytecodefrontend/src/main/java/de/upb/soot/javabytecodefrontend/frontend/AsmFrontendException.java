package de.upb.soot.javabytecodefrontend.frontend;

import de.upb.soot.frontends.ResolveException;
import javax.annotation.Nullable;

/** Exception thrown in the front-End */
public class AsmFrontendException extends ResolveException {
  public AsmFrontendException(@Nullable String message) {
    super(message);
  }

  public AsmFrontendException(String message, Exception e) {
    super(message, e);
  }
}
