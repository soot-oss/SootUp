package sootup.core.util;

import java.util.EnumSet;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.MethodModifier;

public class Modifiers {
  public static EnumSet<MethodModifier> getMethodModifiers(int access) {
    EnumSet<MethodModifier> modifierEnumSet = EnumSet.noneOf(MethodModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (MethodModifier modifier : MethodModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<ClassModifier> getClassModifiers(int access) {
    EnumSet<ClassModifier> modifierEnumSet = EnumSet.noneOf(ClassModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (ClassModifier modifier : ClassModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }

  public static EnumSet<FieldModifier> getFieldModifiers(int access) {
    EnumSet<FieldModifier> modifierEnumSet = EnumSet.noneOf(FieldModifier.class);

    // add all modifiers for which (access & ABSTRACT) =! 0
    for (FieldModifier modifier : FieldModifier.values()) {
      if ((access & modifier.getBytecode()) != 0) {
        modifierEnumSet.add(modifier);
      }
    }
    return modifierEnumSet;
  }
}
