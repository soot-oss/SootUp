package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt, Linghui Luo
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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.StmtPrinter;

public class JavaSootMethod extends SootClassMember<MethodSignature>
    implements SootMethod, HasAnnotation {

  @NonNull private final ImmutableSet<MethodModifier> modifiers;

  /** Declared exceptions thrown by this method. Created upon demand. */
  @NonNull protected final ImmutableList<ClassType> exceptions;

  /** Tells this method how to find out where its body lives. */
  @NonNull protected final BodySource bodySource;

  @NonNull private final Supplier<Body> _lazyBody = Suppliers.memoize(this::lazyBodyInitializer);

  @NonNull private final Iterable<AnnotationUsage> annotations;

  @NonNull private final ImmutableList<Set<AnnotationUsage>> parameterAnnotations;

  /** Constructs a SootMethod object with the given attributes. */
  public JavaSootMethod(
      @NonNull BodySource source,
      @NonNull MethodSignature methodSignature,
      @NonNull Iterable<MethodModifier> modifiers,
      @NonNull Iterable<ClassType> thrownExceptions,
      @NonNull Position position) {
    super(methodSignature, position);
    this.bodySource = source;
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
    this.exceptions = ImmutableUtils.immutableListOf(thrownExceptions);
    this.annotations = ImmutableUtils.emptyImmutableList();
    this.parameterAnnotations = ImmutableList.of();
  }

  public JavaSootMethod(
      @NonNull BodySource source,
      @NonNull MethodSignature methodSignature,
      @NonNull Iterable<MethodModifier> modifiers,
      @NonNull Iterable<ClassType> thrownExceptions,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Position position) {
    super(methodSignature, position);
    this.bodySource = source;
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
    this.exceptions = ImmutableUtils.immutableListOf(thrownExceptions);
    this.annotations = annotations;
    this.parameterAnnotations = ImmutableList.of();
  }

  public JavaSootMethod(
      @NonNull BodySource source,
      @NonNull MethodSignature methodSignature,
      @NonNull Iterable<MethodModifier> modifiers,
      @NonNull Iterable<ClassType> thrownExceptions,
      @NonNull Iterable<AnnotationUsage> annotations,
      @NonNull Position position,
      @NonNull Iterable<Set<AnnotationUsage>> parameterAnnotations) {
    super(methodSignature, position);
    this.bodySource = source;
    this.modifiers = ImmutableUtils.immutableEnumSetOf(modifiers);
    this.exceptions = ImmutableUtils.immutableListOf(thrownExceptions);
    this.annotations = annotations;
    this.parameterAnnotations = ImmutableList.copyOf(parameterAnnotations);
  }

  @NonNull
  private Body lazyBodyInitializer() {
    if (!isConcrete()) {
      throw new ResolveException(
          "There is no corresponding body if the method is not concrete i.e."
              + getSignature()
              + " is abstract or native.",
          Paths.get(""));
    }

    try {
      return bodySource.resolveBody(getModifiers());
    } catch (ResolveException | IOException e) {
      throw new ResolveException(
          "Could not resolve a corresponding body for " + getSignature(), Paths.get(""), e);
    }
  }

  @Override
  public boolean isProtected() {
    return MethodModifier.isProtected(this.getModifiers());
  }

  @Override
  public boolean isPrivate() {
    return MethodModifier.isPrivate(this.getModifiers());
  }

  @Override
  public boolean isPublic() {
    return MethodModifier.isPublic(this.getModifiers());
  }

  @Override
  public boolean isStatic() {
    return MethodModifier.isStatic(this.getModifiers());
  }

  @Override
  public boolean isFinal() {
    return MethodModifier.isFinal(this.getModifiers());
  }

  /**
   * Gets the modifiers of this class member in an immutable set.
   *
   * @see MethodModifier
   */
  @NonNull
  public Set<MethodModifier> getModifiers() {
    return modifiers;
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(modifiers, getSignature());
  }

  /** Returns true if this method is not abstract or native, i.e. this method can have a body. */
  public boolean isConcrete() {
    return !isAbstract() && !isNative();
  }

  @NonNull
  public Type getReturnType() {
    return getSignature().getType();
  }

  /** Returns the number of parameters taken by this method. */
  public int getParameterCount() {
    return getSignature().getParameterCount();
  }

  @NonNull
  public Type getParameterType(int n) {
    return getSignature().getParameterType(n);
  }

  @NonNull
  public List<Set<AnnotationUsage>> getParameterAnnotations() {
    return this.parameterAnnotations;
  }

  @NonNull
  public MethodSubSignature getSubSignature() {
    return getSignature().getSubSignature();
  }

  @NonNull
  public List<Type> getParameterTypes() {
    return getSignature().getParameterTypes();
  }

  @NonNull
  public ClassType getDeclClassType() {
    return getSignature().getDeclClassType();
  }

  /** Returns the SootClass declaring this one. */
  @NonNull
  @Override
  public ClassType getDeclaringClassType() {
    return super.getDeclaringClassType();
  }

  @NonNull
  public String getName() {
    return getSignature().getName();
  }

  /** Retrieves the active body for this method. */
  @NonNull
  public Body getBody() {
    return this._lazyBody.get();
  }

  /** Returns true if this method has a body. */
  public boolean hasBody() {
    return isConcrete();
  }

  @NonNull
  public BodySource getBodySource() {
    return bodySource;
  }

  @NonNull
  public List<ClassType> getExceptionSignatures() {
    return exceptions;
  }

  /** Convenience method returning true if this method is abstract. */
  public boolean isAbstract() {
    return MethodModifier.isAbstract(this.getModifiers());
  }

  /** Convenience method returning true if this method is native. */
  public boolean isNative() {
    return MethodModifier.isNative(this.getModifiers());
  }

  /** Convenience method returning true if this method is synchronized. */
  public boolean isSynchronized() {
    return MethodModifier.isSynchronized(this.getModifiers());
  }

  /**
   * @return yes if this is the main method
   */
  public boolean isMain(@NonNull IdentifierFactory idf) {
    return isPublic() && isStatic() && idf.isMainSubSignature(getSignature().getSubSignature());
  }

  /**
   * @return true if the method is a constructor
   */
  public boolean isConstructor(@NonNull IdentifierFactory idf) {
    return idf.isConstructorSignature(getSignature());
  }

  /**
   * @return true if the method is the default constructor
   */
  public boolean isDefaultConstructor(@NonNull IdentifierFactory idf) {
    return isConstructor(idf) && getParameterCount() == 0;
  }

  /**
   * @return returns default value of annotation. May be null, if there is no default value
   */
  @NonNull
  public Optional<Object> getDefaultValue() {
    return Optional.ofNullable(this.bodySource.resolveAnnotationsDefaultValue());
  }

  /**
   * Returns the declaration of this method, as used at the top of textual body representations
   * (before the {}'s containing the code for representation.)
   */
  public void toString(@NonNull StmtPrinter printer) {

    // print modifiers
    final Set<MethodModifier> modifiers = getModifiers();
    printer.modifier(MethodModifier.toString(modifiers));
    if (!modifiers.isEmpty()) {
      printer.literal(" ");
    }

    // print returnType + name + ( parameterList )
    final MethodSubSignature subSignature = getSignature().getSubSignature();
    subSignature.toString(printer);

    // Print exceptions
    Iterator<ClassType> exceptionIt = getExceptionSignatures().iterator();
    if (exceptionIt.hasNext()) {
      printer.literal(" throws ");
      printer.typeSignature(exceptionIt.next());

      while (exceptionIt.hasNext()) {
        printer.literal(", ");
        printer.typeSignature(exceptionIt.next());
      }
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getBodySource(), getBodySource().getSignature(), getModifiers(), getParameterTypes());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SootMethod)) {
      return false;
    }
    return getBodySource() == ((SootMethod) obj).getBodySource()
        && getBodySource().getSignature() == ((SootMethod) obj).getBodySource().getSignature()
        && getModifiers() == ((SootMethod) obj).getModifiers()
        && getParameterTypes() == ((SootMethod) obj).getParameterTypes();
  }

  @NonNull
  public Iterable<AnnotationUsage> getAnnotations() {
    return annotations;
  }

  /**
   * Creates a new SootMethod based on a new {@link OverridingBodySource}. This is useful to change
   * selected parts of a {@link SootMethod} without recreating a {@link BodySource} completely.
   * {@link OverridingBodySource} allows for replacing the body of a method.
   */
  @NonNull
  @Override
  public JavaSootMethod withOverridingMethodSource(
      @NonNull Function<OverridingBodySource, OverridingBodySource> overrider) {
    return new JavaSootMethod(
        overrider.apply(new OverridingBodySource(bodySource)),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withSource(@NonNull BodySource source) {
    return new JavaSootMethod(
        source, getSignature(), getModifiers(), exceptions, getAnnotations(), getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withModifiers(@NonNull Iterable<MethodModifier> modifiers) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        modifiers,
        getExceptionSignatures(),
        getAnnotations(),
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withThrownExceptions(@NonNull Iterable<ClassType> thrownExceptions) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        thrownExceptions,
        getAnnotations(),
        getPosition());
  }

  @NonNull
  public JavaSootMethod withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
    return new JavaSootMethod(
        bodySource,
        getSignature(),
        getModifiers(),
        getExceptionSignatures(),
        annotations,
        getPosition());
  }

  @NonNull
  @Override
  public JavaSootMethod withBody(@NonNull Body body) {
    return new JavaSootMethod(
        new OverridingBodySource(bodySource).withBody(body),
        getSignature(),
        getModifiers(),
        exceptions,
        getAnnotations(),
        getPosition());
  }

  /**
   * Defines a {@link JavaSootMethod.JavaSootMethodBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootMethodBuilder {

    @Nullable private BodySource source;
    @Nullable private MethodSignature methodSignature;
    @NonNull private Iterable<MethodModifier> modifiers = Collections.emptyList();
    @NonNull private Iterable<ClassType> thrownExceptions = Collections.emptyList();
    @NonNull private Position position = NoPositionInformation.getInstance();
    @Nullable private Iterable<AnnotationUsage> annotations;

    private JavaSootMethodBuilder() {}

    public static CompleteStep builder() {
      return new JavaSootMethodBuilder.Steps();
    }

    public interface MethodSourceStep {
      CompleteStep withSource(@NonNull BodySource value);
    }

    public interface SignatureStep {
      CompleteStep withSignature(@NonNull MethodSignature value);
    }

    public interface ModifierStep {
      CompleteStep withModifier(@NonNull Iterable<MethodModifier> modifier);

      default ThrownExceptionsStep withModifiers(
          @NonNull MethodModifier first, @NonNull MethodModifier... rest) {
        return withModifier(EnumSet.of(first, rest));
      }
    }

    public interface ThrownExceptionsStep {
      CompleteStep withThrownExceptions(@NonNull Iterable<ClassType> value);
    }

    public interface AnnotationsStep {
      CompleteStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations);
    }

    public interface PositionStep {
      CompleteStep withPosition(@NonNull Position position);
    }

    public interface BuildStep {
      JavaSootMethod build();
    }

    public interface CompleteStep
        extends MethodSourceStep,
            SignatureStep,
            ModifierStep,
            ThrownExceptionsStep,
            AnnotationsStep,
            PositionStep,
            BuildStep {}

    private static class Steps implements CompleteStep {
      private final JavaSootMethodBuilder instance = new JavaSootMethodBuilder();

      @Override
      public CompleteStep withSource(@NonNull BodySource source) {
        instance.source = source;
        return this;
      }

      @Override
      public CompleteStep withSignature(@NonNull MethodSignature methodSignature) {
        instance.methodSignature = methodSignature;
        return this;
      }

      @Override
      public CompleteStep withModifier(@NonNull Iterable<MethodModifier> modifiers) {
        instance.modifiers = modifiers;
        return this;
      }

      @Override
      public CompleteStep withModifiers(
          @NonNull MethodModifier first, @NonNull MethodModifier... rest) {
        return withModifier(EnumSet.of(first, rest));
      }

      @Override
      public CompleteStep withThrownExceptions(@NonNull Iterable<ClassType> thrownExceptions) {
        instance.thrownExceptions = thrownExceptions;
        return this;
      }

      @Override
      public CompleteStep withAnnotation(@NonNull Iterable<AnnotationUsage> annotations) {
        instance.annotations = annotations;
        return this;
      }

      @Override
      public CompleteStep withPosition(@NonNull Position position) {
        instance.position = position;
        return this;
      }

      @Override
      public JavaSootMethod build() {
        return new JavaSootMethod(
            instance.source,
            instance.methodSignature,
            instance.modifiers,
            instance.thrownExceptions,
            instance.annotations != null ? instance.annotations : Collections.emptyList(),
            instance.position);
      }
    }
  }
}
