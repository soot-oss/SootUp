package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class CheckVoidLocalesValidator implements BodyValidator {

  @Override
  public void validate(Body body, List<ValidationException> exception) {
    // TODO: check copied code from old soot
    /*
     * for (Local l : body.getLocals()) { if (l.getType() instanceof VoidType) { exception.add(new ValidationException(l,
     * "Local " + l + " in " + body.getMethod() + " defined with void type")); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
