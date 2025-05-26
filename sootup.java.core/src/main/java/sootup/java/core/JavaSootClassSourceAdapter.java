package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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

import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.ResolveException;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;

public class JavaSootClassSourceAdapter extends JavaSootClassSource {

  private final OverridingJavaClassSource overridingClassSource;

  public JavaSootClassSourceAdapter(@NonNull OverridingJavaClassSource overridingClassSource) {
    super(overridingClassSource);
    this.overridingClassSource = overridingClassSource;
  }

  public static JavaSootClassSource adapt(@NonNull OverridingJavaClassSource source) {
    return new JavaSootClassSourceAdapter(source);
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    // Implement this based on how/if annotations are stored in your OverridingClassSource.
    // Placeholder: return empty set if not supported
    return Collections.emptySet();
  }

  @Override
  public @NonNull Collection<? extends SootMethod> resolveMethods() throws ResolveException {
    return overridingClassSource.resolveMethods();
  }

  @Override
  public @NonNull Collection<? extends SootField> resolveFields() throws ResolveException {
    return overridingClassSource.resolveFields();
  }

  @Override
  public @NonNull Set<ClassModifier> resolveModifiers() {
    return overridingClassSource.resolveModifiers();
  }

  @Override
  public @NonNull Set<? extends ClassType> resolveInterfaces() {
    return overridingClassSource.resolveInterfaces();
  }

  @Override
  public @NonNull Optional<? extends ClassType> resolveSuperclass() {
    return overridingClassSource.resolveSuperclass();
  }

  @Override
  public @NonNull Optional<? extends ClassType> resolveOuterClass() {
    return overridingClassSource.resolveOuterClass();
  }

  @Override
  public @NonNull Position resolvePosition() {
    return overridingClassSource.resolvePosition();
  }
}
