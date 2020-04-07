package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class LocalsValidator implements BodyValidator {

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    for (Value v : body.getUseBoxes()) {
      validateLocal(body, v, exception);
    }
    for (Value v : body.getDefBoxes()) {
      validateLocal(body, v, exception);
    }
  }

  private void validateLocal(Body body, Value v, List<ValidationException> exception) {
    Value value;
    if ((value = v) instanceof Local) {
      if (!body.getLocals().contains(value)) {
        exception.add(
                new ValidationException(
                        value, "Local not in chain : " + value + " in " + body.getMethod()));
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
