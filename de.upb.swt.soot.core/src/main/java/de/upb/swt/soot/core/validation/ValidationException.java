package de.upb.swt.soot.core.validation;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;

public class ValidationException extends RuntimeException {

  public ValidationException(SootMethod sm, String void_parameter_types_are_invalid) {
    // TODO: auto generated stub

  }

  public ValidationException(SootClass curClass, String circular_outer_class_chain) {
    // TODO: auto generated stub

  }

  public ValidationException(Local ls, String s) {
    // TODO: auto generated stub

  }

  public ValidationException(Value value, String s) {
    // TODO: auto generated stub

  }

  public ValidationException(Local l, String s, String s1) {
    // TODO: auto generated stub

  }

  public ValidationException(SootMethod method, String s, String s1) {
    // TODO: auto generated stub

  }
}
