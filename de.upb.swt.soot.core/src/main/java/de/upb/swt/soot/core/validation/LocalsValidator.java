package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class LocalsValidator implements BodyValidator {

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    for (ValueBox vb : body.getUseBoxes()) {
      validateLocal(body, vb, exception);
    }
    for (ValueBox vb : body.getDefBoxes()) {
      validateLocal(body, vb, exception);
    }
  }

  private void validateLocal(Body body, ValueBox vb, List<ValidationException> exception) {
    Value value;
    if ((value = vb.getValue()) instanceof Local) {
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
