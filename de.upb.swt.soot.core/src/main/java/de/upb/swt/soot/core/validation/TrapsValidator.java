package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class TrapsValidator implements BodyValidator {

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  @Override
  public void validate(Body body, List<ValidationException> exception) {

    // TODO: check code from old soot below
    /*
     * PatchingChain<Unit> units = body.getUnits();
     *
     * for (Trap t : body.getTraps()) { if (!units.contains(t.getBeginUnit())) { exception.add(new
     * ValidationException(t.getBeginUnit(), "begin not in chain" + " in " + body.getMethod())); }
     *
     * if (!units.contains(t.getEndUnit())) { exception.add(new ValidationException(t.getEndUnit(), "end not in chain" +
     * " in " + body.getMethod())); }
     *
     * if (!units.contains(t.getHandlerUnit())) { exception.add(new ValidationException(t.getHandlerUnit(),
     * "handler not in chain" + " in " + body.getMethod())); } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
