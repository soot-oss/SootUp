package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann and others
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

import com.google.common.collect.Iterables;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.signatures.*;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.Signature;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Abstract class represents a class/module lives in {@link View}. It may have different
 * implementations, since we want to support multiple languages. An abstract class must be uniquely
 * identified by its {@link Signature}.
 *
 * @author Linghui Luo
 */
public abstract class AbstractClass<T extends AbstractClassSource<?>> {

  @Nonnull protected final T classSource;

  public AbstractClass(@Nonnull T cs) {
    this.classSource = cs;
  }

  @Nonnull
  public T getClassSource() {
    return classSource;
  }

  @Nonnull
  public abstract String getName();

  @Nonnull
  public abstract Type getType();

  @Nonnull
  public abstract Set<? extends SootField> getFields();

  @Nonnull
  public abstract Set<? extends SootMethod> getMethods();

  /**
   * Attempts to retrieve the method with the given subSignature. This method may throw an
   * AmbiguousStateException if there are more than one method with the given subSignature. If no
   * method with the given is found, null is returned.
   */
  @Nonnull
  public Optional<? extends SootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    return getMethods().stream()
        .filter(method -> method.getSignature().getSubSignature().equals(subSignature))
        .findAny();
  }

  /** Attemtps to retrieve the field with the given FieldSubSignature. */
  @Nonnull
  public Optional<? extends SootField> getField(@Nonnull FieldSubSignature subSignature) {
    return getFields().stream()
        .filter(f -> f.getSignature().getSubSignature().equals(subSignature))
        .findAny();
  }

  /**
   * Returns the field of this class with the given name. Throws a ResolveException if there is more
   * than one field with the given name. Returns null if no field with the given name exists.
   */
  @Nonnull
  public Optional<? extends SootField> getField(@Nonnull String name) {
    return getFields().stream()
        .filter(field -> field.getSignature().getName().equals(name))
        .reduce(
            (l, r) -> {
              throw new ResolveException(
                  "ambiguous field: " + name + " in " + getClassSource().getClassType(),
                  getClassSource().getSourcePath());
            });
  }

  /**
   * Attempts to retrieve the method with the given name and parameters. This method may throw an
   * ResolveException if there is more than one method with the given name and parameter.
   */
  @Nonnull
  public Optional<? extends SootMethod> getMethod(
      @Nonnull String name, @Nonnull Iterable<? extends Type> parameterTypes) {
    return this.getMethods().stream()
        .filter(
            method ->
                method.getSignature().getName().equals(name)
                    && Iterables.elementsEqual(parameterTypes, method.getParameterTypes()))
        .reduce(
            (l, r) -> {
              throw new ResolveException(
                  "ambiguous method: " + name + " in " + getClassSource().getClassType(),
                  getClassSource().getSourcePath());
            });
  }
}
