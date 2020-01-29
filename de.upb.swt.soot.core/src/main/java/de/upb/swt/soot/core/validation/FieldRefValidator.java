package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.model.Body;
import java.util.List;

public class FieldRefValidator implements BodyValidator {

  /** Checks the consistency of field references. */
  @Override
  public void validate(Body body, List<ValidationException> exceptions) {
    // TODO: check copied code from old soot
    /*
     * SootMethod methodRef = body.getMethod(); if (methodRef.isAbstract()) { return; }
     *
     * Chain<Unit> units = body.getUnits().getNonPatchingChain();
     *
     * for (Unit unit : units) { Stmt s = (Stmt) unit; if (!s.containsFieldRef()) { continue; } FieldRef fr =
     * s.getFieldRef();
     *
     * if (fr instanceof JStaticFieldRef) { JStaticFieldRef v = (JStaticFieldRef) fr; try { SootField field = v.getField();
     * if (field == null) { exceptions.add(new UnitValidationException(unit, body, "Resolved field is null: " +
     * fr.toString())); } else if (!field.isStatic() && !field.isPhantom()) { exceptions .add(new
     * UnitValidationException(unit, body, "Trying to get a static field which is non-static: " + v)); } } catch
     * (ResolutionFailedException e) { exceptions.add(new UnitValidationException(unit, body,
     * "Trying to get a static field which is non-static: " + v)); } } else if (fr instanceof InstanceFieldRef) {
     * InstanceFieldRef v = (InstanceFieldRef) fr;
     *
     * try { SootField field = v.getField(); if (field == null) { exceptions.add(new UnitValidationException(unit, body,
     * "Resolved field is null: " + fr.toString())); } else if (field.isStatic() && !field.isPhantom()) { exceptions.add(new
     * UnitValidationException(unit, body, "Trying to get an instance field which is static: " + v)); } } catch
     * (ResolutionFailedException e) { exceptions.add(new UnitValidationException(unit, body,
     * "Trying to get an instance field which is static: " + v)); } } else { throw new RuntimeException("unknown field ref");
     * } }
     */
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
