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

import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.Type;
import sootup.core.util.ImmutableUtils;

/**
 * Soot's counterpart of the source language's field concept. Soot representation of a Java field.
 * Can be declared to belong to a SootClass.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootField extends SootClassMember<FieldSignature> implements Field {

  @Nonnull private final ImmutableSet<FieldModifier> modifiers;
  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(
      @Nonnull FieldSignature signature,
      @Nonnull Iterable<FieldModifier> modifiers,
      @Nonnull Position position) {
    super(signature, position);
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
  }

  @Override
  public boolean isProtected() {
    return FieldModifier.isProtected(this.getModifiers());
  }

  @Override
  public boolean isPrivate() {
    return FieldModifier.isPrivate(this.getModifiers());
  }

  @Override
  public boolean isPublic() {
    return FieldModifier.isPublic(this.getModifiers());
  }

  @Override
  public boolean isStatic() {
    return FieldModifier.isStatic(this.getModifiers());
  }

  @Override
  public boolean isFinal() {
    return FieldModifier.isFinal(this.getModifiers());
  }

  /**
   * Gets the modifiers of this class member in an immutable set.
   *
   * @see FieldModifier
   */
  @Nonnull
  public Set<FieldModifier> getModifiers() {
    return modifiers;
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(modifiers, getSignature());
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
      return FieldModifier.toString(this.getModifiers())
          + ' '
          + this.getSignature().getSubSignature();
    }
  }

  @Nonnull
  public String getDeclaration() {
    return getOriginalStyleDeclaration();
  }

  @Nonnull
  public SootField withSignature(@Nonnull FieldSignature signature) {
    return new SootField(signature, getModifiers(), getPosition());
  }

  @Nonnull
  public SootField withModifiers(@Nonnull Iterable<FieldModifier> modifiers) {
    return new SootField(getSignature(), modifiers, getPosition());
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
    BuildStep withModifier(@Nonnull Iterable<FieldModifier> modifier);

    @Nonnull
    default BuildStep withModifiers(@Nonnull FieldModifier first, @Nonnull FieldModifier... rest) {
      return withModifier(EnumSet.of(first, rest));
    }
  }

  public interface BuildStep {
    BuildStep withPosition(@Nonnull Position pos);

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
    private Iterable<FieldModifier> modifiers;
    private Position position = NoPositionInformation.getInstance();

    @Nonnull
    protected FieldSignature getSignature() {
      return signature;
    }

    @Nonnull
    protected Iterable<FieldModifier> getModifiers() {
      return modifiers;
    }

    @Nonnull
    public Position getPosition() {
      return position;
    }

    @Override
    @Nonnull
    public ModifierStep withSignature(@Nonnull FieldSignature signature) {
      this.signature = signature;
      return this;
    }

    @Override
    @Nonnull
    public BuildStep withModifier(@Nonnull Iterable<FieldModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    @Nonnull
    public BuildStep withPosition(@Nonnull Position position) {
      this.position = position;
      return this;
    }

    @Override
    @Nonnull
    public SootField build() {
      return new SootField(getSignature(), getModifiers(), getPosition());
    }
  }
}
