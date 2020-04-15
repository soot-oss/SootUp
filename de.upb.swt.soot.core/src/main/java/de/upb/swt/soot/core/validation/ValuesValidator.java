package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class ValuesValidator implements BodyValidator {

  /** Verifies that a Value is not used in more than one place. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {
    // TODO: check code from old soot below
    /*
     * Set<ValueBox> set = newSetFromMap(new IdentityHashMap<ValueBox, Boolean>());
     *
     * for (ValueBox vb : body.getUseAndDefBoxes()) { if (set.add(vb)) { continue; }
     *
     * exception.add(new ValidationException(vb, "Aliased value box : " + vb + " in " + body.getMethod()));
     *
     * for (Unit u : body.getUnits()) { System.err.println(u); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return false;
  }
}
