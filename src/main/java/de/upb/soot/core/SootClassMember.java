package de.upb.soot.core;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import static de.upb.soot.util.Utils.immutableEnumSetOf;

import com.google.common.collect.ImmutableSet;
import de.upb.soot.signatures.AbstractClassMemberSignature;
import de.upb.soot.signatures.AbstractClassMemberSubSignature;
import de.upb.soot.util.builder.AbstractBuilder;
import de.upb.soot.util.builder.BuilderException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides methods common to Soot objects belonging to classes, namely SootField and SootMethod.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class SootClassMember implements Serializable {
  private static final long serialVersionUID = -7201796736790814208L;

  @Nonnull private final AbstractClassMemberSignature _signature;
  @Nonnull private final ImmutableSet<Modifier> _modifiers;

  /** Constructor. */
  public SootClassMember(
      @Nonnull AbstractClassMemberSignature signature, @Nonnull Iterable<Modifier> modifiers) {
    this._signature = signature;
    this._modifiers = immutableEnumSetOf(modifiers);
  }

  @Nullable private volatile SootClass _declaringClass;

  /** Returns the SootClass declaring this one. */
  @Nonnull
  public SootClass getDeclaringClass() {
    SootClass owner = this._declaringClass;

    if (owner == null) {
      throw new IllegalStateException(
          "The declaring class of this soot class member has not been set yet.");
    }

    return owner;
  }

  protected final synchronized void setDeclaringClass(@Nonnull SootClass value) {
    if (this._declaringClass != null) {
      throw new IllegalStateException(
          "The declaring class of this soot class member has already been set.");
    }

    if (!value.getType().equals(this.getSignature().getDeclClassSignature())) {
      throw new IllegalArgumentException(
          "The signature of the specified declaring class does not match to the declaring class "
              + "signature of this soot class member");
    }

    this._declaringClass = value;
  }

  /** Returns true when this object is from a phantom class. */
  public boolean isPhantom() {
    return this.getDeclaringClass().isPhantomClass();
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
    return _modifiers;
  }

  /** Returns a hash code for this methodRef consistent with structural equality. */
  // TODO: check whether modifiers.hashcode() does what its meant for; former: "modifiers"/int bit
  // flags representing the set
  public int equivHashCode() {
    return _modifiers.hashCode() * 17 + _signature.hashCode();
  }

  /** Returns the signature of this methodRef. */
  @Override
  @Nonnull
  public String toString() {
    return _signature.toString();
  }

  /** Returns the Soot signature of this methodRef. Used to refer to methods unambiguously. */
  @Nonnull
  public AbstractClassMemberSignature getSignature() {
    return _signature;
  }

  @Nonnull
  public AbstractClassMemberSubSignature getSubSignature() {
    return _signature.getSubSignature();
  }

  @Nonnull
  public String getName() {
    return this._signature.getName();
  }

  /**
   * Defines the base interface for {@link SootClassMember} builders.
   *
   * @param <T> The type of the class to build.
   * @author Jan Martin Persch
   */
  public interface Builder<T extends SootClassMember> {

    interface ModifiersStep<B> {
      /**
       * Sets the {@link Modifier modifiers}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      B withModifiers(@Nonnull Iterable<Modifier> value);

      /**
       * Sets the {@link Modifier modifiers}.
       *
       * @param first The first value.
       * @param rest The rest values.
       * @return This fluent builder.
       */
      @Nonnull
      default B withModifiers(@Nonnull Modifier first, @Nonnull Modifier... rest) {
        return this.withModifiers(EnumSet.of(first, rest));
      }
    }

    /**
     * Builds the {@link SootMethod}.
     *
     * @return The created {@link SootMethod}.
     * @throws BuilderException A build error occurred.
     */
    @Nonnull
    T build();
  }

  /**
   * Defines base class for {@link SootClassMember} builders.
   *
   * @author Jan Martin Persch
   */
  protected abstract static class SootClassMemberBuilder<T extends SootClassMember>
      extends AbstractBuilder<T> {
    // region Fields

    // endregion /Fields/

    // region Constructor

    /**
     * Creates a new instance of the {@link SootMethod.SootMethodBuilder} class.
     *
     * @param buildableClass The type of the class to build.
     */
    protected SootClassMemberBuilder(@Nonnull Class<T> buildableClass) {
      super(buildableClass);
    }

    // endregion /Constructor/

    // region Properties

    // endregion /Properties/

    // region Methods

    // endregion /Methods/
  }
}
