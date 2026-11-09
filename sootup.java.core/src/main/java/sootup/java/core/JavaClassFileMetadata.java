package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 the SootUp contributors
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

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.types.JavaClassType;

/** Immutable source and nesting attributes of a Java classfile. */
public final class JavaClassFileMetadata {
  private static final JavaClassFileMetadata EMPTY =
      new JavaClassFileMetadata(null, null, null, null, List.of());

  @Nullable private final String sourceFile;
  @Nullable private final String sourceDebugExtension;
  @Nullable private final JavaClassType enclosingClass;
  @Nullable private final MethodSignature enclosingMethod;
  @NonNull private final List<JavaInnerClassInfo> innerClasses;

  /**
   * @param sourceFile the SourceFile attribute, or {@code null} if absent
   * @param sourceDebugExtension the decoded, unparsed SourceDebugExtension attribute, or {@code
   *     null} if absent
   * @param enclosingClass the owner from EnclosingMethod, or {@code null} if that attribute is
   *     absent
   * @param enclosingMethod the enclosing method or constructor; {@code null} when the attribute is
   *     absent or its method_index is zero (an initializer)
   * @param innerClasses InnerClasses entries in classfile order; a defensive copy is made
   * @throws IllegalArgumentException if the enclosing method does not belong to the enclosing class
   */
  public JavaClassFileMetadata(
      @Nullable String sourceFile,
      @Nullable String sourceDebugExtension,
      @Nullable JavaClassType enclosingClass,
      @Nullable MethodSignature enclosingMethod,
      @NonNull List<JavaInnerClassInfo> innerClasses) {
    if (enclosingMethod != null && !enclosingMethod.getDeclClassType().equals(enclosingClass)) {
      throw new IllegalArgumentException("The enclosing method must belong to the enclosing class");
    }
    this.sourceFile = sourceFile;
    this.sourceDebugExtension = sourceDebugExtension;
    this.enclosingClass = enclosingClass;
    this.enclosingMethod = enclosingMethod;
    this.innerClasses = List.copyOf(innerClasses);
  }

  @NonNull
  public static JavaClassFileMetadata empty() {
    return EMPTY;
  }

  @NonNull
  public Optional<String> getSourceFile() {
    return Optional.ofNullable(sourceFile);
  }

  /** Returns decoded SourceDebugExtension text (for example SMAP), without interpreting it. */
  @NonNull
  public Optional<String> getSourceDebugExtension() {
    return Optional.ofNullable(sourceDebugExtension);
  }

  /** Returns the EnclosingMethod owner, including when the class is declared in an initializer. */
  @NonNull
  public Optional<JavaClassType> getEnclosingClass() {
    return Optional.ofNullable(enclosingClass);
  }

  /** Returns the enclosing method or constructor; initializers have no method signature. */
  @NonNull
  public Optional<MethodSignature> getEnclosingMethod() {
    return Optional.ofNullable(enclosingMethod);
  }

  /** Returns an immutable list of all InnerClasses entries in their classfile order. */
  @NonNull
  public List<JavaInnerClassInfo> getInnerClasses() {
    return innerClasses;
  }
}
