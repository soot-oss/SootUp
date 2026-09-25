package sootup.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Akshita Dubey, Sahil Agichani
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.views.View;

/** Validator that checks for impossible combinations of field modifiers */
public class FieldModifiersValidator implements ClassValidator {

  private final EnumSet<FieldModifier> modifiers;

  public FieldModifiersValidator(EnumSet<FieldModifier> modifiers) {
    validate(modifiers);
    this.modifiers = EnumSet.copyOf(modifiers);
  }

  public static FieldModifiersValidator of(FieldModifier... mods) {
    if (mods == null || mods.length == 0) {
      return new FieldModifiersValidator(EnumSet.noneOf(FieldModifier.class));
    }
    return new FieldModifiersValidator(EnumSet.copyOf(Arrays.asList(mods)));
  }

  public static FieldModifiersValidator from(Set<FieldModifier> mods) {
    return new FieldModifiersValidator(EnumSet.copyOf(mods));
  }

  public Set<FieldModifier> asSet() {
    return EnumSet.copyOf(modifiers);
  }

  public boolean contains(FieldModifier mod) {
    return modifiers.contains(mod);
  }

  private void validate(EnumSet<FieldModifier> mods) {
    long accessModifiers =
        mods.stream()
            .filter(
                m ->
                    m == FieldModifier.PUBLIC
                        || m == FieldModifier.PRIVATE
                        || m == FieldModifier.PROTECTED)
            .count();

    if (accessModifiers > 1) {
      throw new IllegalArgumentException("Only one of public, protected, private is allowed.");
    }
    // Add more rules here if needed
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    if (modifiers.contains(FieldModifier.PUBLIC)) builder.append("public ");
    else if (modifiers.contains(FieldModifier.PRIVATE)) builder.append("private ");
    else if (modifiers.contains(FieldModifier.PROTECTED)) builder.append("protected ");

    if (modifiers.contains(FieldModifier.STATIC)) builder.append("static ");
    if (modifiers.contains(FieldModifier.FINAL)) builder.append("final ");
    if (modifiers.contains(FieldModifier.VOLATILE)) builder.append("volatile ");
    if (modifiers.contains(FieldModifier.TRANSIENT)) builder.append("transient ");
    if (modifiers.contains(FieldModifier.ENUM)) builder.append("enum ");
    if (modifiers.contains(FieldModifier.SYNTHETIC)) builder.append("synthetic ");

    if (!builder.isEmpty()) {
      builder.setLength(builder.length() - 1); // remove trailing space
    }

    return builder.toString();
  }

  @Override
  public void validate(SootClass sc, List<ValidationException> exceptions, View view) {
    for (SootField sf : sc.getFields()) {
      if ((sf.isPrivate() || sf.isProtected()) && (sf.isPublic()) || sf.isProtected()) {
        exceptions.add(
            new ValidationException(
                sc,
                "Field $1 can only be either public, protected or private"
                    .replace("$1", sf.getName())));
      }

      if (sc.isInterface()) {
        if (!sf.isPublic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and public".replace("$1", sf.getName())));
        }
        if (!sf.isStatic()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and static".replace("$1", sf.getName())));
        }
        if (!sf.isFinal()) {
          exceptions.add(
              new ValidationException(
                  sc, "Field $1 must be an interface and final".replace("$1", sf.getName())));
        }
      }
    }
  }

  @Override
  public boolean isBasicValidator() {
    return true;
  }
}
