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
import org.jspecify.annotations.NonNull;
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

  @NonNull private final ImmutableSet<FieldModifier> modifiers;

  /** Constructs a Soot field with the given name, type and modifiers. */
  public SootField(
      @NonNull FieldSignature signature,
      @NonNull Iterable<FieldModifier> modifiers,
      @NonNull Position position) {
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
  @NonNull
  public Set<FieldModifier> getModifiers() {
    return modifiers;
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(modifiers, getSignature());
  }

  @NonNull
  public Type getType() {
    return this.getSignature().getType();
  }

  @NonNull
  public SootField withSignature(@NonNull FieldSignature signature) {
    return new SootField(signature, getModifiers(), getPosition());
  }

  @NonNull
  public SootField withModifiers(@NonNull Iterable<FieldModifier> modifiers) {
    return new SootField(getSignature(), modifiers, getPosition());
  }

  /**
   * Creates a {@link SootField}
   *
   * @return A {@link SootField}
   */
  @NonNull
  public static SignatureStep builder() {
    return new SootFieldBuilder();
  }

  public interface SignatureStep {
    @NonNull ModifierStep withSignature(@NonNull FieldSignature value);
  }

  public interface ModifierStep {
    @NonNull BuildStep withModifier(@NonNull Iterable<FieldModifier> modifier);

    @NonNull
    default BuildStep withModifiers(@NonNull FieldModifier first, @NonNull FieldModifier... rest) {
      return withModifier(EnumSet.of(first, rest));
    }
  }

  public interface BuildStep {
    BuildStep withPosition(@NonNull Position pos);

    @NonNull SootField build();
  }

  /**
   * Defines a {@link SootField} builder to provide a fluent API.
   *
   * @author Jan Martin Persch
   */
  public static class SootFieldBuilder
      implements SignatureStep, ModifierStep, BuildStep, HasPosition {

    private FieldSignature signature;
    private Iterable<FieldModifier> modifiers;
    private Position position = NoPositionInformation.getInstance();

    @NonNull
    protected FieldSignature getSignature() {
      return signature;
    }

    @NonNull
    protected Iterable<FieldModifier> getModifiers() {
      return modifiers;
    }

    @NonNull
    @Override
    public Position getPosition() {
      return position;
    }

    @Override
    @NonNull
    public ModifierStep withSignature(@NonNull FieldSignature signature) {
      this.signature = signature;
      return this;
    }

    @Override
    @NonNull
    public BuildStep withModifier(@NonNull Iterable<FieldModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    @NonNull
    public BuildStep withPosition(@NonNull Position position) {
      this.position = position;
      return this;
    }

    @Override
    @NonNull
    public SootField build() {
      return new SootField(getSignature(), getModifiers(), getPosition());
    }
  }
}
