package sootup.core.signatures;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 linghui Luo, Jan Martin Persch
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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Hash-conses (interns) the identifier objects of {@code sootup.core.signatures}: for a given value
 * there is at most one live instance, so equal signatures are the same object and may be compared
 * with {@code ==}.
 *
 * <p>Interning happens on the constructed object itself, so identity follows each type's own {@code
 * equals}/{@code hashCode} - there is no separate cache key that could drift out of sync with them.
 * The interners hold their entries weakly, so a signature that nothing references any more is
 * collected as usual.
 *
 * <p>This class exists in {@code sootup.core} because the constructors it calls are
 * package-private: it is the only way to create these types, which is what makes the interning
 * invariant hold. Most callers should not use it directly but go through an {@link
 * IdentifierFactory}, which is the language-aware entry point. {@code sootup.core} itself has no
 * {@link IdentifierFactory} implementation available (that lives in the language modules, which
 * depend on this one), so code inside this module - its tests in particular - uses these methods
 * instead.
 */
public final class SignatureInterner {

  private SignatureInterner() {}

  @NonNull private static final Interner<PackageName> PACKAGE_NAMES = Interners.newWeakInterner();

  @NonNull
  private static final Interner<MethodSubSignature> METHOD_SUB_SIGNATURES =
      Interners.newWeakInterner();

  @NonNull
  private static final Interner<FieldSubSignature> FIELD_SUB_SIGNATURES =
      Interners.newWeakInterner();

  @NonNull
  private static final Interner<MethodSignature> METHOD_SIGNATURES = Interners.newWeakInterner();

  @NonNull
  private static final Interner<FieldSignature> FIELD_SIGNATURES = Interners.newWeakInterner();

  /** Returns the unique {@link PackageName} for the given package name. */
  @NonNull
  public static PackageName getPackageName(@NonNull String packageName) {
    return PACKAGE_NAMES.intern(new PackageName(packageName));
  }

  /**
   * Interns an already constructed {@link PackageName}. Subclasses of {@link PackageName} live in
   * the language modules and construct themselves; they pass the result through here so that they
   * share the interning invariant.
   */
  @NonNull
  public static <T extends PackageName> T internPackageName(@NonNull T packageName) {
    @SuppressWarnings("unchecked")
    T interned = (T) PACKAGE_NAMES.intern(packageName);
    return interned;
  }

  /**
   * Returns the unique {@link MethodSubSignature} for the given name, return type and parameters.
   */
  @NonNull
  public static MethodSubSignature getMethodSubSignature(
      @NonNull String name,
      @NonNull Type returnType,
      @NonNull Iterable<? extends Type> parameterTypes) {
    return METHOD_SUB_SIGNATURES.intern(new MethodSubSignature(name, parameterTypes, returnType));
  }

  /** Returns the unique {@link FieldSubSignature} for the given name and type. */
  @NonNull
  public static FieldSubSignature getFieldSubSignature(@NonNull String name, @NonNull Type type) {
    return FIELD_SUB_SIGNATURES.intern(new FieldSubSignature(name, type));
  }

  /** Returns the unique {@link MethodSignature} for the given declaring class and sub-signature. */
  @NonNull
  public static MethodSignature getMethodSignature(
      @NonNull ClassType declaringClass, @NonNull MethodSubSignature subSignature) {
    return METHOD_SIGNATURES.intern(new MethodSignature(declaringClass, subSignature));
  }

  /**
   * Returns the unique {@link MethodSignature} for the given declaring class, name, parameters and
   * return type.
   */
  @NonNull
  public static MethodSignature getMethodSignature(
      @NonNull ClassType declaringClass,
      @NonNull String methodName,
      @NonNull Iterable<Type> parameters,
      @NonNull Type returnType) {
    return getMethodSignature(
        declaringClass, getMethodSubSignature(methodName, returnType, parameters));
  }

  /** Returns the unique {@link FieldSignature} for the given declaring class and sub-signature. */
  @NonNull
  public static FieldSignature getFieldSignature(
      @NonNull ClassType declaringClass, @NonNull FieldSubSignature subSignature) {
    return FIELD_SIGNATURES.intern(new FieldSignature(declaringClass, subSignature));
  }

  /** Returns the unique {@link FieldSignature} for the given declaring class, name and type. */
  @NonNull
  public static FieldSignature getFieldSignature(
      @NonNull ClassType declaringClass, @NonNull String name, @NonNull Type type) {
    return getFieldSignature(declaringClass, getFieldSubSignature(name, type));
  }
}
