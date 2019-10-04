package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.SootClass;
import java.util.List;

/**
 * Validates classes to make sure that the outer class chain is not recursive
 *
 * @author Steven Arzt
 */
public class OuterClassValidator implements ClassValidator {

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions) {

    // TODO: check code from old soot in the comment

    /*
     * Set<SootClass> outerClasses = new HashSet<SootClass>(); SootClass curClass = sc; while (curClass != null) { if
     * (!outerClasses.add(curClass)) { exceptions.add(new ValidationException(curClass, "Circular outer class chain"));
     * break; } curClass = curClass.hasOuterClass() ? curClass.getOuterClass() : null; }
     */
  }

  @Override
  public boolean isBasicValidator() {
    // TODO: check code from old soot n the comment
    return true;
  }
}
