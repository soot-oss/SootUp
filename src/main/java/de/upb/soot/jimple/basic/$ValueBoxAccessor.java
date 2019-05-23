package de.upb.soot.jimple.basic;

/** This class is for internal use only. It will be removed in the future. */
@Deprecated
public class $ValueBoxAccessor {
  // This class deliberately starts with a $-sign to discourage usage
  // of this Soot implementation detail. Some IDEs such as IntelliJ
  // don't suggest these classes in autocomplete.

  public static void setValue(ValueBox box, Value value) {
    box.setValue(value);
  }
}
