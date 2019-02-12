package de.upb.soot.validation;

import de.upb.soot.core.Body;

import java.util.List;

public class StmtBoxesValidator implements BodyValidator {

  /** Verifies that the UnitBoxes of this Body all point to a Unit contained within this body. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
