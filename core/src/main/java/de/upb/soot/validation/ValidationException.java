package de.upb.soot.validation;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;

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
