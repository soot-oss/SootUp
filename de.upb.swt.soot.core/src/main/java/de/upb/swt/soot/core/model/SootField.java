package de.upb.swt.soot.core.model;

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

import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.FieldSubSignature;
import de.upb.swt.soot.core.types.Type;
import java.util.EnumSet;
import javax.annotation.Nonnull;

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
  public SootField withSignature(@Nonnull FieldSignature signature) {
    return new SootField(signature, getModifiers());
  }

  @Nonnull
  public SootField withModifiers(@Nonnull Iterable<Modifier> modifiers) {
    return new SootField(getSignature(), modifiers);
  }

  /**
   * Creates a {@link SootField}
   *
   * @return A {@link SootField}
   */
  @Nonnull
  public static SignatureStep builder() {
    return new SootFieldBuilder();
  }

  public interface SignatureStep {
    @Nonnull
    ModifierStep withSignature(@Nonnull FieldSignature value);
  }

  public interface ModifierStep {
    @Nonnull
    BuildStep withModifier(@Nonnull Iterable<Modifier> modifier);

    @Nonnull
    default BuildStep withModifiers(@Nonnull Modifier first, @Nonnull Modifier... rest) {
      return withModifier(EnumSet.of(first, rest));
    }
  }

  public interface BuildStep {
    @Nonnull
    SootField build();
  }

  /**
   * Defines a {@link SootField} builder to provide a fluent API.
   *
   * @author Jan Martin Persch
   */
  public static class SootFieldBuilder implements SignatureStep, ModifierStep, BuildStep {

    private FieldSignature signature;
    private Iterable<Modifier> modifiers;

    @Nonnull
    protected FieldSignature getSignature() {
      return signature;
    }

    @Nonnull
    protected Iterable<Modifier> getModifiers() {
      return modifiers;
    }

    @Override
    @Nonnull
    public ModifierStep withSignature(@Nonnull FieldSignature signature) {
      this.signature = signature;
      return this;
    }

    @Override
    @Nonnull
    public BuildStep withModifier(@Nonnull Iterable<Modifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    @Nonnull
    public SootField build() {
      return new SootField(getSignature(), getModifiers());
    }
  }
}
