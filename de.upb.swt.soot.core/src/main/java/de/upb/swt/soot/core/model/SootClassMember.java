package de.upb.swt.soot.core.model;
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

import com.google.common.collect.ImmutableSet;
import de.upb.swt.soot.core.signatures.AbstractClassMemberSubSignature;
import de.upb.swt.soot.core.signatures.SootClassMemberSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class SootClassMember<S extends SootClassMemberSignature> {

  @Nonnull private final S signature;
  @Nonnull private final ImmutableSet<Modifier> modifiers;

  SootClassMember(@Nonnull S signature, @Nonnull Iterable<Modifier> modifiers) {
    this.signature = signature;
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
  }

  /** Returns the SootClass declaring this one. */
  @Nonnull
  public ClassType getDeclaringClassType() {
    return this.signature.getDeclClassType();
  }

  /** Convenience methodRef returning true if this class member is protected. */
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class member is private. */
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class member is public. */
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class member is static. */
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  /** Convenience methodRef returning true if this field is final. */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  /**
   * Gets the modifiers of this class member in an immutable set.
   *
   * @see Modifier
   */
  @Nonnull
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  /** Returns a hash code for this method consistent with structural equality. */
  public int equivHashCode() {
    return Objects.hash(modifiers, signature);
  }

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
  public AbstractClassMemberSubSignature getSubSignature() {
    return signature.getSubSignature();
  }

  @Nonnull
  public String getName() {
    return this.signature.getName();
  }
}
