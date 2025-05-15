package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.ImmutableUtils;

public class JavaSootField extends SootClassMember<FieldSignature>
    implements SootField, HasAnnotation {

  @NonNull private final ImmutableSet<FieldModifier> modifiers;
  @NonNull private final Iterable<AnnotationUsage> annotations;

  /** Constructs a Soot field with the given name, type and modifiers. */
  public JavaSootField(
      @NonNull FieldSignature signature,
      @NonNull Iterable<FieldModifier> modifiers,
      @NonNull Position position) {
    super(signature, position);
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
    this.annotations = ImmutableUtils.emptyImmutableList();
  }

  /**
   * Constructs a Soot field with the given name, type and modifiers.
   *
   * @param signature the signature of the field
   * @param modifiers modifier of the field
   * @param annotations annotations of the field
   * @param position position of the field
   */
  public JavaSootField(
      @NonNull FieldSignature signature,
      @NonNull Iterable<FieldModifier> modifiers,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Position position) {
    super(signature, position);
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
    this.annotations = annotations;
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
  public JavaSootField withSignature(@NonNull FieldSignature signature) {
    return new JavaSootField(signature, getModifiers(), getPosition());
  }

  @NonNull
  public JavaSootField withModifiers(@NonNull Iterable<FieldModifier> modifiers) {
    return new JavaSootField(getSignature(), modifiers, getPosition());
  }

  /** Returns the SootClass declaring this one. */
  @NonNull
  public ClassType getDeclaringClassType() {
    return super.getDeclaringClassType();
  }

  /**
   * Creates a {@link SootField}
   *
   * @return A {@link SootField}
   */
  @NonNull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootFieldBuilder();
  }

  public interface SignatureStep {
    JavaSootField.ModifierStep withSignature(@NonNull FieldSignature value);
  }

  public interface ModifierStep {
    JavaSootField.BuildStep withModifier(@NonNull Iterable<FieldModifier> modifier);

    default JavaSootField.BuildStep withModifiers(
        @NonNull FieldModifier first, @NonNull FieldModifier... rest) {
      return withModifier(EnumSet.of(first, rest));
    }
  }

  public interface BuildStep {
    JavaSootField.BuildStep withPosition(@NonNull Position pos);

    @NonNull SootField build();
  }

  /**
   * Defines a {@link SootField} builder to provide a fluent API.
   *
   * @author Jan Martin Persch
   */
  public static class SootFieldBuilder
      implements JavaSootField.SignatureStep,
          JavaSootField.ModifierStep,
          JavaSootField.BuildStep,
          HasPosition {

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
    public JavaSootField.ModifierStep withSignature(@NonNull FieldSignature signature) {
      this.signature = signature;
      return this;
    }

    @Override
    public JavaSootField.BuildStep withModifier(@NonNull Iterable<FieldModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }

    @Override
    public JavaSootField.BuildStep withPosition(@NonNull Position position) {
      this.position = position;
      return this;
    }

    @Override
    @NonNull
    public SootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getPosition());
    }
  }

  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  @NonNull
  public JavaSootField withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
    return new JavaSootField(getSignature(), getModifiers(), annotations, getPosition());
  }

  public interface AnnotationOrSignatureStep extends SignatureStep {
    BuildStep withAnnotation(Iterable<AnnotationUsage> annotations);
  }

  /**
   * Defines a {@link JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootFieldBuilder extends SootFieldBuilder
      implements AnnotationOrSignatureStep {

    private Iterable<AnnotationUsage> annotations = null;

    @NonNull
    public Iterable<AnnotationUsage> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @NonNull
    public BuildStep withAnnotation(Iterable<AnnotationUsage> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @NonNull
    public JavaSootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getAnnotations(), getPosition());
    }
  }
}
