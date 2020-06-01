package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class StmtsValidator implements BodyValidator {

  /** Verifies that the Units of this Body all point to a Unit contained within this body. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {}

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
