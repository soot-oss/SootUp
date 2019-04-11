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

import static de.upb.soot.util.Utils.ImmutableCollectors.toImmutableSet;
import static de.upb.soot.util.Utils.immutableEnumSetOf;
import static de.upb.soot.util.Utils.immutableSetOf;
import static de.upb.soot.util.Utils.initializedLazy;
import static de.upb.soot.util.Utils.iterableToStream;
import static de.upb.soot.util.Utils.synchronizedLazy;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.signatures.FieldSubSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.MethodSubSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.util.Utils;
import de.upb.soot.util.builder.AbstractBuilder;
import de.upb.soot.util.builder.BuilderException;
import de.upb.soot.util.concurrent.Lazy;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * Incomplete and inefficient implementation.
 *
 * Implementation notes:
 *
 * 1. The getFieldOf() methodRef is slow because it traverses the list of fields, comparing the names,
 * one by one.  If you establish a Dictionary of Name->Field, you will need to add a
 * notifyOfNameChange() methodRef, and register fields which belong to classes, because the hashtable
 * will need to be updated.  I will do this later. - kor  16-Sep-97
 *
 * 2. Note 1 is kept for historical (i.e. amusement) reasons.  In fact, there is no longer a list of fields;
 * these are kept in a Chain now.  But that's ok; there is no longer a getFieldOf() methodRef,
 * either.  There still is no efficient way to get a field by name, although one could establish
 * a Chain of EquivalentValue-like objects and do an O(1) search on that.  - plam 2-24-00
 */

/**
 * Soot's counterpart of the source languages class concept. Soot representation of a Java class.
 * They are usually created by a Scene, but can also be constructed manually through the given
 * constructors.
 *
 * @author Manuel Benz created on 06.06.18.
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public class SootClass extends AbstractClass implements Serializable {

  // implementation of StepBuilder Pattern to create SootClasses consistent
  // http://www.svlada.com/step-builder-pattern/

  /**
   * Creates a SootClass with a fluent interfaces and enforces at compile team a clean order to
   * ensure a consistent state of the soot class Therefore, a different Interface is returned after
   * each step.. (therby order is enforced)
   */
  public interface DanglingStep extends Build {
    HierachyStep dangling(ClassSource source, ClassType classType);
  }

  public interface HierachyStep extends Build {
    SignatureStep hierachy(
        @Nullable JavaClassType superclass,
        Set<JavaClassType> interfaces,
        EnumSet<Modifier> modifiers,
        @Nullable JavaClassType outerClass);
  }

  public interface SignatureStep extends Build {
    BodyStep signature(Set<SootField> fields, Set<IMethod> methods);
  }

  public interface BodyStep extends Build {
    Build bodies(String content);
  }

  public interface Build {
    SootClass build();
  }

  public static class SootClassSurrogateBuilder
      implements DanglingStep, HierachyStep, SignatureStep, BodyStep, Build {
    private ResolvingLevel resolvingLevel;
    private ClassType classType;
    private Position position;
    private Iterable<Modifier> modifiers;
    private Iterable<? extends IField> fields;
    private Iterable<? extends IMethod> methods;
    private Iterable<? extends JavaClassType> interfaces;

    @Nullable private JavaClassType superClass;
    @Nullable private JavaClassType outerClass;

    private ClassSource classSource;

    public SootClassSurrogateBuilder() {}

    @Override
    public HierachyStep dangling(ClassSource source, ClassType classType) {
      this.classSource = source;
      this.classType = classType;
      this.resolvingLevel = ResolvingLevel.DANGLING;
      return this;
    }

    // FIXME: decided what a Class at Hierachy Level must have resoled...
    @Override
    public SignatureStep hierachy(
        JavaClassType superclass,
        Set<JavaClassType> interfaces,
        EnumSet<Modifier> modifiers,
        JavaClassType outerClass) {

      this.superClass = superclass;
      this.interfaces = interfaces;
      this.modifiers = modifiers;
      this.resolvingLevel = ResolvingLevel.HIERARCHY;
      return this;
    }

    @Override
    public BodyStep signature(Set<SootField> fields, Set<IMethod> methods) {
      this.fields = fields;
      this.methods = methods;
      this.resolvingLevel = ResolvingLevel.SIGNATURES;
      return this;
    }

    @Override
    public Build bodies(String content) {
      return null;
    }

    @Override
    public SootClass build() {
      return new SootClass(this);
    }
  }

  public static DanglingStep surrogateBuilder() {
    return new SootClassSurrogateBuilder();
  }

  // FIXME: check if everything is here...
  public static SootClassSurrogateBuilder fromExisting(SootClass sootClass) {
    SootClassSurrogateBuilder builder = new SootClassSurrogateBuilder();
    builder.resolvingLevel = sootClass.resolvingLevel;
    builder.methods = sootClass.getMethods();
    builder.fields = sootClass.getFields();
    builder.modifiers = sootClass.modifiers;
    builder.classSource = sootClass.classSource;
    builder.classType = sootClass.classType;
    builder.interfaces = sootClass.interfaces;
    return builder;
  }

  // FIXME: add missing statements
  private SootClass(SootClassSurrogateBuilder builder) {
    super(builder.classSource);
    this.resolvingLevel = builder.resolvingLevel;
    this.classType = builder.classType;
    this.superClass = builder.superClass;
    this.interfaces = immutableSetOf(builder.interfaces);
    this.classSignature = builder.classSource.getClassType();
    this.outerClass = builder.outerClass;
    this.position = builder.position;
    this.modifiers = immutableEnumSetOf(builder.modifiers);
    this._lazyFields = synchronizedLazy(this::lazyFieldInitializer);
    this._lazyMethods = synchronizedLazy(this::lazyMethodInitializer);
  }

  private static final long serialVersionUID = -4145583783298080555L;

  private final ResolvingLevel resolvingLevel;
  private final ClassType classType;
  private final Position position;
  @Nonnull private final ImmutableSet<Modifier> modifiers;
  @Nonnull private final JavaClassType classSignature;
  @Nonnull private final ImmutableSet<JavaClassType> interfaces;

  @Nullable private final JavaClassType superClass;

  @Nullable private final JavaClassType outerClass;

  // TODO: [JMP] Create type signature for this dummy type and move it closer to its usage.
  @Nonnull public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = "soot.dummy.InvokeDynamic";

  public SootClass(
      ResolvingLevel resolvingLevel,
      ClassSource classSource,
      ClassType type,
      @Nullable JavaClassType superClass,
      @Nonnull Iterable<? extends JavaClassType> interfaces,
      @Nullable JavaClassType outerClass,
      Position position,
      Iterable<Modifier> modifiers) {
    this(
        resolvingLevel,
        classSource,
        type,
        superClass,
        interfaces,
        outerClass,
        null,
        null,
        position,
        modifiers);
  }

  public SootClass(
      ResolvingLevel resolvingLevel,
      ClassSource classSource,
      ClassType type,
      @Nullable JavaClassType superClass,
      @Nonnull Iterable<? extends JavaClassType> interfaces,
      @Nullable JavaClassType outerClass,
      @Nullable Iterable<? extends SootField> fields,
      @Nullable Iterable<? extends SootMethod> methods,
      Position position,
      Iterable<Modifier> modifiers) {
    super(classSource);

    this.resolvingLevel = resolvingLevel;
    this.classType = type;
    this.superClass = superClass;
    this.interfaces = immutableSetOf(interfaces);
    this.classSignature = classSource.getClassType();
    this.outerClass = outerClass;
    this.position = position;
    this.modifiers = immutableEnumSetOf(modifiers);

    this._lazyFields =
        fields == null
            ? synchronizedLazy(this::lazyFieldInitializer)
            : this.initializedFieldInitializer(fields);

    this._lazyMethods =
        methods == null
            ? synchronizedLazy(this::lazyMethodInitializer)
            : this.initializedFieldInitializer(methods);
  }

  @Nonnull
  private <M extends SootClassMember> Set<M> initializeClassMembers(
      @Nonnull Iterable<? extends M> items) {
    return iterableToStream(items).peek(it -> it.setDeclaringClass(this)).collect(toImmutableSet());
  }

  @Nonnull
  private <M extends SootClassMember> Lazy<Set<M>> initializedFieldInitializer(
      @Nonnull Iterable<? extends M> items) {
    return initializedLazy(this.initializeClassMembers(items));
  }

  @Nonnull
  private Set<SootField> lazyFieldInitializer() {
    Iterable<SootField> fields;

    try {
      fields = this.classSource.getContent().resolveFields(this.getType());
    } catch (ResolveException e) {
      fields = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return this.initializeClassMembers(fields);
  }

  @Nonnull
  private Set<SootMethod> lazyMethodInitializer() {
    Iterable<SootMethod> methods;

    try {
      methods = this.classSource.getContent().resolveMethods(this.getType());
    } catch (ResolveException e) {
      methods = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }

    return this.initializeClassMembers(methods);
  }

  //  // FIXME: error handling
  //  public void resolve(de.upb.soot.core.ResolvingLevel resolvingLevel) {
  //    try {
  //      this.getClassSource().getContent().resolve(resolvingLevel, getView());
  //    } catch (ResolveException e) {
  //      e.printStackTrace();
  //    }
  //  }

  @Nonnull private final Lazy<Set<SootMethod>> _lazyMethods;

  /** Gets the {@link IMethod methods} of this {@link SootClass} in an immutable set. */
  @Nonnull
  public Set<SootMethod> getMethods() {
    return this._lazyMethods.get();
  }

  @Nonnull private final Lazy<Set<SootField>> _lazyFields;

  /** Gets the {@link IField fields} of this {@link SootClass} in an immutable set. */
  @Override
  @Nonnull
  public Set<SootField> getFields() {
    return this._lazyFields.get();
  }

  /**
   * Checks if the class has at lease the resolving level specified. This check does nothing is the
   * class resolution process is not completed.
   *
   * @param level the resolution level, one of DANGLING, HIERARCHY, SIGNATURES, and BODIES
   * @throws java.lang.RuntimeException if the resolution is at an insufficient level
   */
  public void checkLevel(ResolvingLevel level) {
    // Fast check: e.g. FastHierarchy.canStoreClass calls this methodRef quite
    // often
    ResolvingLevel currentLevel = resolvingLevel();
    if (currentLevel.getLevel() >= level.getLevel()) {
      return;
    }

    //    if (!this.getView().doneResolving() ||
    // this.getView().getOptions().ignore_resolving_levels()) {
    //      return;
    //    }
    checkLevelIgnoreResolving(level);
  }

  /**
   * Checks if the class has at lease the resolving level specified. This check ignores the
   * resolution completeness.
   *
   * @param level the resolution level, one of DANGLING, HIERARCHY, SIGNATURES, and BODIES
   * @throws java.lang.RuntimeException if the resolution is at an insufficient level
   */
  public void checkLevelIgnoreResolving(ResolvingLevel level) {
    ResolvingLevel currentLevel = resolvingLevel();
    if (currentLevel.getLevel() < level.getLevel()) {
      String hint =
          "\nIf you are extending Soot, try to add the following call before calling soot.Main.main(..):\n"
              + "Scene.getInstance().addBasicClass("
              + classSignature
              + ","
              + level
              + ");\n"
              + "Otherwise, try whole-program mode (-w).";
      throw new RuntimeException(
          "This operation requires resolving level "
              + level
              + " but "
              + classSignature.getClassName()
              + " is at resolving level "
              + currentLevel
              + hint);
    }
  }

  public ResolvingLevel resolvingLevel() {
    return resolvingLevel;
  }

  /** Returns the number of fields in this class. */
  public int getFieldCount() {
    // FIXME "This has to be refactored later. I'm unsure whether we still need the resolving
    // levels."
    //   https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r267007069
    //   This also applies to every other commented-out occurrence of checkLevel(...) in this class.
    //    checkLevel(ResolvingLevel.SIGNATURES);
    return getFields().size();
  }

  /**
   * Returns the field of this class with the given name. Throws a RuntimeException if there is more
   * than one field with the given name. Returns null if no field with the given name exists.
   */
  @Nonnull
  public Optional<SootField> getField(String name) {
    //    checkLevel(ResolvingLevel.SIGNATURES);

    return this.getFields().stream()
        .filter(field -> field.getSignature().getName().equals(name))
        .reduce(
            (l, r) -> {
              throw new RuntimeException("ambiguous field: " + name);
            });
  }

  /**
   * Returns the field of this class with the given sub-signature. If such a field does not exist,
   * null is returned.
   */
  @Nonnull
  public Optional<SootField> getField(@Nonnull FieldSubSignature subSignature) {
    //    checkLevel(ResolvingLevel.SIGNATURES);

    return this.getFields().stream()
        .filter(field -> field.getSubSignature().equals(subSignature))
        .findAny();
  }

  /**
   * Attempts to retrieve the methodRef with the given signature, parameters and return type. If no
   * matching methodRef can be found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(MethodSignature signature) {
    //    checkLevel(ResolvingLevel.SIGNATURES);

    return this.getMethods().stream()
        .filter(method -> method.getSignature().equals(signature))
        .findAny();
  }

  /**
   * Attempts to retrieve the methodRef with the given name and parameters. This methodRef may throw
   * an AmbiguousMethodException if there is more than one methodRef with the given name and
   * parameter.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(String name, Iterable<? extends Type> parameterTypes) {
    //    checkLevel(ResolvingLevel.SIGNATURES);

    return this.getMethods().stream()
        .filter(
            method ->
                method.getSignature().getName().equals(name)
                    && Iterables.elementsEqual(parameterTypes, method.getParameterTypes()))
        .reduce(
            (l, r) -> {
              throw new RuntimeException("ambiguous methodRef: " + name);
            });
  }

  /**
   * Attempts to retrieve the methodRef with the given subSignature. This methodRef may throw an
   * AmbiguousMethodException if there are more than one methodRef with the given subSignature. If
   * no methodRef with the given is found, null is returned.
   */
  @Nonnull
  public Optional<SootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    //    checkLevel(ResolvingLevel.SIGNATURES);

    return this.getMethods().stream()
        .filter(method -> method.getSubSignature().equals(subSignature))
        .findAny();
  }

  /** Returns the modifiers of this class in an immutable set. */
  @Nonnull
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  /**
   * Returns the number of interfaces being directly implemented by this class. Note that direct
   * implementation corresponds to an "implements" keyword in the Java class file and that this
   * class may still be implementing additional interfaces in the usual sense by being a subclass of
   * a class which directly implements some interfaces.
   */
  public int getInterfaceCount() {
    //    checkLevel(ResolvingLevel.HIERARCHY);

    return interfaces.size();
  }

  /**
   * Returns a backed Chain of the interfaces that are directly implemented by this class. (see
   * getInterfaceCount())
   */
  public Set<JavaClassType> getInterfaces() {
    //    checkLevel(ResolvingLevel.HIERARCHY);

    return this.interfaces;
  }

  /** Does this class directly implement the given interface? (see getInterfaceCount()) */
  public boolean implementsInterface(JavaClassType classSignature) {
    //    checkLevel(ResolvingLevel.HIERARCHY);

    for (JavaClassType sc : interfaces) {
      if (sc.equals(classSignature)) {
        return true;
      }
    }
    return false;
  }

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Does this class have a
   * superclass? False implies that this is the java.lang.Object class. Note that interfaces are
   * subclasses of the java.lang.Object class.
   */
  public boolean hasSuperclass() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return superClass != null;
  }

  /**
   * WARNING: interfaces are subclasses of the java.lang.Object class! Returns the superclass of
   * this class. (see hasSuperclass())
   */
  public Optional<JavaClassType> getSuperclass() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return Optional.ofNullable(superClass);
  }

  public boolean hasOuterClass() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return outerClass != null;
  }

  /** This methodRef returns the outer class. */
  public @Nonnull Optional<JavaClassType> getOuterClass() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return Optional.ofNullable(outerClass);
  }

  public boolean isInnerClass() {
    return hasOuterClass();
  }

  /** Returns the ClassSignature of this class. */
  @Override
  public JavaClassType getType() {
    return classSignature;
  }

  /** Convenience methodRef; returns true if this class is an interface. */
  public boolean isInterface() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return Modifier.isInterface(this.getModifiers());
  }

  /** Convenience methodRef; returns true if this class is an enumeration. */
  public boolean isEnum() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return Modifier.isEnum(this.getModifiers());
  }

  /** Convenience methodRef; returns true if this class is synchronized. */
  public boolean isSynchronized() {
    //    checkLevel(ResolvingLevel.HIERARCHY);
    return Modifier.isSynchronized(this.getModifiers());
  }

  /** Returns true if this class is not an interface and not abstract. */
  public boolean isConcrete() {
    return !isInterface() && !isAbstract();
  }

  /** Convenience methodRef; returns true if this class is public. */
  public boolean isPublic() {
    return Modifier.isPublic(this.getModifiers());
  }

  /** Returns the name of this class. */
  @Override
  @Nonnull
  public String toString() {
    return classSignature.toString();
  }

  /** Returns true if this class is an application class. */
  public boolean isApplicationClass() {
    return classType.equals(ClassType.Application);
  }

  /** Returns true if this class is a library class. */
  public boolean isLibraryClass() {
    return classType.equals(ClassType.Library);
  }

  private static final class LibraryClassPatternHolder {
    /**
     * Sometimes we need to know which class is a JDK class. There is no simple way to distinguish a
     * user class and a JDK class, here we use the package prefix as the heuristic.
     */
    private static final Pattern LIBRARY_CLASS_PATTERN =
        Pattern.compile(
            "^(?:java\\.|sun\\.|javax\\.|com\\.sun\\.|org\\.omg\\.|org\\.xml\\.|org\\.w3c\\.dom)");
  }

  public boolean isJavaLibraryClass() {
    return LibraryClassPatternHolder.LIBRARY_CLASS_PATTERN
        .matcher(classSignature.getClassName())
        .find();
  }

  /** Returns true if this class is a phantom class. */
  public boolean isPhantomClass() {
    return classType.equals(ClassType.Phantom);
  }

  /** Convenience methodRef returning true if this class is private. */
  public boolean isPrivate() {
    return Modifier.isPrivate(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is protected. */
  public boolean isProtected() {
    return Modifier.isProtected(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is abstract. */
  public boolean isAbstract() {
    return Modifier.isAbstract(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is final. */
  public boolean isFinal() {
    return Modifier.isFinal(this.getModifiers());
  }

  /** Convenience methodRef returning true if this class is static. */
  public boolean isStatic() {
    return Modifier.isStatic(this.getModifiers());
  }

  protected int number = 0;

  // FIXME The following code is commented out due to incompatibility, but
  //   may still be needed.
  //   https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r266971653

  //  /**
  //   * An array containing some validators in order to validate the SootClass
  //   */
  //  private static final List<ClassValidator> validators
  //      = Arrays.asList(new OuterClassValidator(), new MethodDeclarationValidator(), new
  // ClassFlagsValidator());
  //
  //  /**
  //   * Validates this SootClass for logical errors. Note that this does not validate the methodRef
  // bodies, only the class
  //   * structure.
  //   */
  //  public void validate() {
  //    final List<ValidationException> exceptionList = new ArrayList<>();
  //    validate(exceptionList);
  //    if (!exceptionList.isEmpty()) {
  //      throw exceptionList.get(0);
  //    }
  //  }
  //
  //  /**
  //   * Validates this SootClass for logical errors. Note that this does not validate the methodRef
  // bodies, only the class
  //   * structure. All found errors are saved into the given list.
  //   */
  //  public void validate(List<ValidationException> exceptionList) {
  //    final boolean runAllValidators = this.getView().getOptions().debug() ||
  // this.getView().getOptions().validate();
  //    for (ClassValidator validator : validators) {
  //      if (!validator.isBasicValidator() && !runAllValidators) {
  //        continue;
  //      }
  //      validator.validate(this, exceptionList);
  //    }
  //  }

  @Nonnull
  public Position getPosition() {
    return this.position;
  }

  @Override
  public ClassSource getClassSource() {
    return classSource;
  }

  @Override
  @Nonnull
  public String getName() {
    return this.classSignature.getFullyQualifiedName();
  }

  @Nonnull
  public Optional<JavaClassType> getSuperclassSignature() {
    return Optional.ofNullable(superClass);
  }

  @Nonnull
  public Optional<JavaClassType> getOuterClassSignature() {
    return Optional.ofNullable(outerClass);
  }

  /**
   * Creates a {@link SootClass} builder.
   *
   * @return A {@link SootClass} builder.
   */
  @Nonnull
  public static Builder.ResolvingLevelStep builder() {
    return new SootClassBuilder();
  }

  /**
   * Defines a stepwise builder for the {@link SootClass} class.
   *
   * @see #builder()
   */
  public interface Builder {
    interface ResolvingLevelStep {
      /**
       * Sets the {@link ResolvingLevel}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ClassSourceStep withResolvingLevel(@Nonnull ResolvingLevel value);
    }

    interface ClassSourceStep {
      /**
       * Sets the {@link ClassSource}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ClassTypeStep withClassSource(@Nonnull ClassSource value);
    }

    interface ClassTypeStep {
      /**
       * Sets the {@link ClassType}.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      ModifiersStep withClassType(@Nonnull ClassType value);
    }

    interface ModifiersStep {
      /**
       * Sets the {@link SootMethod.Builder soot method builders}. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      SuperClassStep withModifiers(@Nonnull Iterable<Modifier> value);

      /**
       * Sets the {@link Modifier modifiers}.
       *
       * @param first The first value.
       * @param rest The rest values.
       * @return This fluent builder.
       */
      @Nonnull
      default SuperClassStep withModifiers(@Nonnull Modifier first, @Nonnull Modifier... rest) {
        return this.withModifiers(EnumSet.of(first, rest));
      }
    }

    interface SuperClassStep extends InterfacesStep {
      /**
       * Sets the {@link JavaClassType} of the super class. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      InterfacesStep withSuperClass(@Nonnull JavaClassType value);
    }

    interface InterfacesStep extends OuterClassStep {
      /**
       * Sets the {@link JavaClassType interface type signatures}. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      OuterClassStep withInterfaces(@Nonnull Iterable<? extends JavaClassType> value);

      /**
       * Sets the {@link JavaClassType interface type signatures}. This step is optional.
       *
       * @param values The values to set.
       * @return This fluent builder.
       */
      @Nonnull
      default OuterClassStep withInterfaces(@Nonnull JavaClassType... values) {
        return this.withInterfaces(Arrays.asList(values));
      }
    }

    interface OuterClassStep extends FieldsStep {
      /**
       * Sets the {@link JavaClassType} of the out class. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      FieldsStep withOuterClass(@Nonnull JavaClassType value);
    }

    interface FieldsStep extends MethodsStep {
      /**
       * Sets the {@link SootField soot field builders}. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      MethodsStep withFields(@Nonnull Iterable<? extends SootField> value);

      /**
       * Sets the {@link SootField soot field builders}. This step is optional.
       *
       * @param values The values to set.
       * @return This fluent builder.
       */
      @Nonnull
      default MethodsStep withFields(@Nonnull SootField... values) {
        return this.withFields(Arrays.asList(values));
      }
    }

    interface MethodsStep extends PositionStep {
      /**
       * Sets the {@link SootMethod soot method builders}. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      PositionStep withMethods(@Nonnull Iterable<? extends SootMethod> value);

      /**
       * Sets the {@link SootMethod soot method builders}. This step is optional.
       *
       * @param values The values to set.
       * @return This fluent builder.
       */
      @Nonnull
      default PositionStep withMethods(@Nonnull SootMethod... values) {
        return this.withMethods(Arrays.asList(values));
      }
    }

    interface PositionStep extends Builder {
      /**
       * Sets the {@link Position}. This step is optional.
       *
       * @param value The value to set.
       * @return This fluent builder.
       */
      @Nonnull
      Builder withPosition(@Nullable Position value);
    }

    /**
     * Builds the {@link SootClass}.
     *
     * @return The created {@link SootClass}.
     * @throws BuilderException A build error occurred.
     */
    @Nonnull
    SootClass build();
  }

  /**
   * Defines a {@link SootMethod} builder that provides a fluent API.
   *
   * @author Jan Martin Persch
   */
  protected static class SootClassBuilder extends AbstractBuilder<SootClass>
      implements Builder.ResolvingLevelStep,
          Builder.ClassSourceStep,
          Builder.ClassTypeStep,
          Builder.SuperClassStep,
          Builder.InterfacesStep,
          Builder.OuterClassStep,
          Builder.FieldsStep,
          Builder.MethodsStep,
          Builder.PositionStep,
          Builder.ModifiersStep,
          Builder {
    // region Fields

    // endregion /Fields/

    // region Constructor

    /** Creates a new instance of the {@link SootMethod.SootMethodBuilder} class. */
    protected SootClassBuilder() {
      super(SootClass.class);
    }

    // endregion /Constructor/

    // region Properties

    @Nullable private ResolvingLevel _resolvingLevel;

    /**
     * Gets the resolving level.
     *
     * @return The value to get.
     */
    @Nonnull
    public ResolvingLevel getResolvingLevel() {
      return ensureValue(this._resolvingLevel, "resolvingLevel");
    }

    /**
     * Sets the resolving level.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public ClassSourceStep withResolvingLevel(@Nonnull ResolvingLevel value) {
      this._resolvingLevel = value;

      return this;
    }

    @Nullable private ClassSource _classSource;

    /**
     * Gets the class source.
     *
     * @return The value to get.
     */
    @Nonnull
    public ClassSource getClassSource() {
      return ensureValue(this._classSource, "classSource");
    }

    /**
     * Sets the class source.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public ClassTypeStep withClassSource(@Nonnull ClassSource value) {
      this._classSource = value;

      return this;
    }

    @Nullable private ClassType _classType;

    /**
     * Gets the class type.
     *
     * @return The value to get.
     */
    @Nonnull
    public ClassType getClassType() {
      return ensureValue(this._classType, "classType");
    }

    /**
     * Sets the class type.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public ModifiersStep withClassType(@Nonnull ClassType value) {
      this._classType = value;

      return this;
    }

    @Nonnull private Iterable<Modifier> _modifiers = Collections.emptyList();

    /**
     * Gets the modifiers.
     *
     * @return The value to get.
     */
    @Nonnull
    public Iterable<Modifier> getModifiers() {
      return ensureValue(this._modifiers, "modifiers");
    }

    /**
     * Sets the modifiers.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public SuperClassStep withModifiers(@Nonnull Iterable<Modifier> value) {
      this._modifiers = value;

      return this;
    }

    @Nullable private JavaClassType _superClass;

    /**
     * Gets the super class.
     *
     * @return The value to get.
     */
    @Nullable
    public JavaClassType getSuperClass() {
      return this._superClass;
    }

    /**
     * Sets the super class.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public InterfacesStep withSuperClass(@Nullable JavaClassType value) {
      this._superClass = value;

      return this;
    }

    @Nonnull private Iterable<? extends JavaClassType> _interfaces = Collections.emptyList();

    /**
     * Gets the interfaces.
     *
     * @return The value to get.
     */
    @Nonnull
    public Iterable<? extends JavaClassType> getInterfaces() {
      return ensureValue(this._interfaces, "interfaces");
    }

    /**
     * Sets the interfaces.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public OuterClassStep withInterfaces(@Nonnull Iterable<? extends JavaClassType> value) {
      this._interfaces = value;

      return this;
    }

    @Nullable private JavaClassType _outerClass;

    /**
     * Gets the outer class.
     *
     * @return The value to get.
     */
    @Nullable
    public JavaClassType getOuterClass() {
      return this._outerClass;
    }

    /**
     * Sets the outer class.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public FieldsStep withOuterClass(@Nullable JavaClassType value) {
      this._outerClass = value;

      return this;
    }

    @Nonnull private Iterable<? extends SootField> _fields = Collections.emptyList();

    /**
     * Gets the fields.
     *
     * @return The value to get.
     */
    @Nonnull
    public Iterable<? extends SootField> getFields() {
      return ensureValue(this._fields, "fields");
    }

    /**
     * Sets the fields.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public MethodsStep withFields(@Nonnull Iterable<? extends SootField> value) {
      this._fields = value;

      return this;
    }

    @Nonnull private Iterable<? extends SootMethod> _methods = Collections.emptyList();

    /**
     * Gets the methods.
     *
     * @return The value to get.
     */
    @Nonnull
    public Iterable<? extends SootMethod> getMethods() {
      return ensureValue(this._methods, "methods");
    }

    /**
     * Sets the methods.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public PositionStep withMethods(@Nonnull Iterable<? extends SootMethod> value) {
      this._methods = value;

      return this;
    }

    @Nullable private Position _position;

    /**
     * Gets the position.
     *
     * @return The value to get.
     */
    @Nullable
    public Position getPosition() {
      return this._position;
    }

    /**
     * Sets the position.
     *
     * @param value The value to set.
     */
    @Override
    @Nonnull
    public Builder withPosition(@Nullable Position value) {
      this._position = value;

      return this;
    }

    // endregion /Properties/

    // region Methods

    @Override
    @Nonnull
    protected SootClass make() {
      return new SootClass(
          this.getResolvingLevel(),
          this.getClassSource(),
          this.getClassType(),
          this.getSuperClass(),
          this.getInterfaces(),
          this.getOuterClass(),
          this.getFields(),
          this.getMethods(),
          this.getPosition(),
          this.getModifiers());
    }

    // endregion /Methods/
  }
}
