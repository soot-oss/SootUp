package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

/**
 * This validator checks whether the jimple traps are correct. It does not perform the same checks
 * as {link soot.validation.TrapsValidator}
 *
 * @see JimpleTrapValidator#validate(Body, List)
 * @author Marc Miltenberger
 */
public class JimpleTrapValidator implements BodyValidator {

  /** Checks whether all Caught-Exception-References are associated to traps. */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * Set<Unit> caughtUnits = new HashSet<Unit>(); for (Trap trap : body.getTraps()) {
     * caughtUnits.add(trap.getHandlerUnit());
     *
     * if (!(trap.getHandlerUnit() instanceof IdentityStmt)) { exceptions.add(new ValidationException(trap,
     * "Trap handler does not start with caught " + "exception reference")); } else { JIdentityStmt is = (JIdentityStmt)
     * trap.getHandlerUnit(); if (!(is.getRightOp() instanceof CaughtExceptionRef)) { exceptions.add(new
     * ValidationException(trap, "Trap handler does not start with caught " + "exception reference")); } } } for (Unit u :
     * body.getUnits()) { if (u instanceof JIdentityStmt) { JIdentityStmt id = (JIdentityStmt) u; if (id.getRightOp()
     * instanceof CaughtExceptionRef) { if (!caughtUnits.contains(id)) { exceptions.add(new ValidationException(id,
     * "Could not find a corresponding trap using this statement as handler", "Body of methodRef " +
     * body.getMethod().getSignature() + " contains a caught exception reference," +
     * "but not a corresponding trap using this statement as handler")); } } } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
