package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.SootClass;
import java.util.List;

/**
 * Validates classes to make sure that all methodRef signatures are valid
 *
 * @author Steven Arzt
 */
public class MethodDeclarationValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {
    // TODO: check code from old soot in the comment

    /*
     * if (sc.isConcrete()) { for (SootMethod sm : sc.getMethods()) { for (Type tp : sm.getParameterTypes()) { if (tp ==
     * null) { exceptions.add(new ValidationException(sm, "Null parameter types are invalid")); } if (tp instanceof VoidType)
     * { exceptions.add(new ValidationException(sm, "Void parameter types are invalid")); } if (!tp.isAllowedInFinalCode()) {
     * exceptions.add(new ValidationException(sm, "Parameter type not allowed in final code")); } } } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
