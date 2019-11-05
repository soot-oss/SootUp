package de.upb.swt.soot.core.model;
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

import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Soot's counterpart of the source language's field concept. Soot representation of a Java field.
 * Can be declared to belong to a SootClass.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootField extends SootClassMember<FieldSignature> implements Field {

  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(@Nonnull FieldSignature signature, @Nonnull Iterable<Modifier> modifiers) {
    super(signature, modifiers);
  }

  @Nonnull
  public Type getType() {
    return this.getSignature().getType();
  }

  @Nonnull
  private String getOriginalStyleDeclaration() {
    if (this.getModifiers().isEmpty()) {
      return this.getSignature().getSubSignature().toString();
    } else {
      return Modifier.toString(this.getModifiers()) + ' ' + this.getSignature().getSubSignature();
    }
  }

  @Nonnull
  @Override
  public FieldSubSignature getSubSignature() {
    return (FieldSubSignature) super.getSubSignature();
  }

  @Nonnull
  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }

  @Nonnull
  public SootField withSignature(FieldSignature signature) {
    return new SootField(signature, getModifiers());
  }

  @Nonnull
  public SootField withModifiers(Iterable<Modifier> modifiers) {
    return new SootField(getSignature(), modifiers);
  }

  /**
   * Creates a {@link SootField} builder.
   *
   * @return A {@link SootField} builder.
   */
  @Nonnull
  public static Builder.SignatureStep builder() {
    return new SootFieldBuilder();
  }

  /**
   * Defines a stepwise builder for the {@link SootField} class.
   *
   * @see #builder()
   * @author Jan Martin Persch
   */
  public interface Builder extends SootClassMember.Builder<FieldSignature, SootField> {
    interface SignatureStep {
      /**
       * Sets the {@link FieldSignature}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ModifiersStep withSignature(@Nonnull FieldSignature value);
    }

    interface ModifiersStep extends SootClassMember.Builder.ModifiersStep<Builder> {}

    /**
     * Builds the {@link SootField}.
     *
     * @return The created {@link SootField}.
     * @throws BuilderException A build error occurred.
     */
    @Nonnull
    SootField build();
  }

  /**
   * Defines a {@link SootMethod} builder that provides a fluent API.
   *
   * @author Jan Martin Persch
   */
  protected static class SootFieldBuilder extends SootClassMemberBuilder<FieldSignature, SootField>
      implements Builder.SignatureStep, Builder.ModifiersStep, Builder {

    /** Creates a new instance of the {@link SootMethod.SootMethodBuilder} class. */
    SootFieldBuilder() {
      super(SootField.class);
    }

    @Nullable private FieldSignature _signature;

    /**
     * Gets the field sub-signature.
     *
     * @return The value to get.
     */
    @Nonnull
    protected FieldSignature getSignature() {
      return ensureValue(this._signature, "signature");
    }

    /**
     * Sets the field sub-signature.
     *
     * @param value The value to set.
     */
    @Nonnull
    public ModifiersStep withSignature(@Nonnull FieldSignature value) {
      this._signature = value;

      return this;
    }

    @Nullable private Iterable<Modifier> _modifiers;

    /**
     * Gets the modifiers.
     *
     * @return The value to get.
     */
    @Nonnull
    protected Iterable<Modifier> getModifiers() {
      return ensureValue(this._modifiers, "modifiers");
    }

    /**
     * Sets the modifiers.
     *
     * @param value The value to set.
     */
    @Nonnull
    public Builder withModifiers(@Nonnull Iterable<Modifier> value) {
      this._modifiers = value;

      return this;
    }

    @Override
    @Nonnull
    public SootField build() {
      return new SootField(this.getSignature(), this.getModifiers());
    }
  }
}
