package sootup.core.util;

import java.util.EnumSet;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.MethodModifier;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Palaniappan Muthuraman
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
