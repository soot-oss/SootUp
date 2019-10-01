package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.SootClass;
import java.util.List;

/**
 * Validator that checks for impossible combinations of class flags
 *
 * @author Steven Arzt
 */
public class ClassFlagsValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {
    // TODO: check code from old soot in the comment

    /*
     * if (sc.isInterface() && sc.isEnum()) { exceptions.add(new ValidationException(sc,
     * "Class is both an interface and an enum")); } if (sc.isSynchronized()) { exceptions.add(new ValidationException(sc,
     * "Classes cannot be synchronized")); }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
