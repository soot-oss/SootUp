package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Soot's counterpart of the source language's field concept. Soot representation of a Java field.
 * Can be declared to belong to a SootClass.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public interface SootField extends Field {

  boolean isProtected();

  boolean isPrivate();

  boolean isPublic();

  boolean isStatic();

  boolean isFinal();

  Set<FieldModifier> getModifiers();

  Type getType();

  SootField withSignature(@NonNull FieldSignature signature);

  SootField withModifiers(@NonNull Iterable<FieldModifier> modifiers);

  ClassType getDeclaringClassType();

  public String getName();
}
