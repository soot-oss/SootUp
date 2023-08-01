package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import javax.annotation.Nonnull;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.types.ClassType;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class SootClassMember<S extends SootClassMemberSignature> {

  @Nonnull private final S signature;

  @Nonnull private final Position position;

  SootClassMember(@Nonnull S signature, @Nonnull Position position) {
    this.signature = signature;
    this.position = position;
  }

  /** Returns the SootClass declaring this one. */
  @Nonnull
  public ClassType getDeclaringClassType() {
    return this.signature.getDeclClassType();
  }

  /** Convenience method returning true if this class member is protected. */
  public abstract boolean isProtected();

  /** Convenience method returning true if this class member is private. */
  public abstract boolean isPrivate();

  /** Convenience method returning true if this class member is public. */
  public abstract boolean isPublic();

  /** Convenience method returning true if this class member is static. */
  public abstract boolean isStatic();

  /** Convenience method returning true if this field is final. */
  public abstract boolean isFinal();

  /** Returns a hash code for this method consistent with structural equality. */
  public abstract int equivHashCode();

  /** Returns the signature of this method. */
  @Override
  @Nonnull
  public String toString() {
    return signature.toString();
  }

  /** Returns the Soot signature of this method. Used to refer to methods unambiguously. */
  @Nonnull
  public S getSignature() {
    return signature;
  }

  @Nonnull
  public String getName() {

    return signature.getName();
  }

  @Nonnull
  public Position getPosition() {
    return position;
  }
}
