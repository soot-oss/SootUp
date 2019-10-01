package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/** Implement this interface if you want to provide your own body Validator */
public interface BodyValidator {
  /**
   * Validates the given body and saves all validation errors in the given list.
   *
   * @param body the body to check
   * @param exceptions the list of exceptions
   */
  void validate(Body body, List<ValidationException> exceptions);

  /**
   * Basic validators run essential checks and are run always if validate is called.<br>
   * If this methodRef returns false and the caller of the validator respects this property,<br>
   * the checks will only be run if the debug or validation option is activated.
   *
   * @return whether this validator is a basic validator
   */
  boolean isBasicValidator();
}
