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

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.java.core.types.JavaClassType;

/** An immutable entry of a classfile InnerClasses attribute. */
public final class JavaInnerClassInfo {
  @NonNull private final JavaClassType innerClass;
  @Nullable private final JavaClassType outerClass;
  @Nullable private final String innerName;
  private final int accessFlags;

  /**
   * @param innerClass the class described by this entry
   * @param outerClass the declaring class for a member class, or {@code null} for a local or
   *     anonymous class
   * @param innerName the simple name, or {@code null} for an anonymous class
   * @param accessFlags the unmodified inner_class_access_flags bits
   */
  public JavaInnerClassInfo(
      @NonNull JavaClassType innerClass,
      @Nullable JavaClassType outerClass,
      @Nullable String innerName,
      int accessFlags) {
    this.innerClass = innerClass;
    this.outerClass = outerClass;
    this.innerName = innerName;
    this.accessFlags = accessFlags;
  }

  @NonNull
  public JavaClassType getInnerClass() {
    return innerClass;
  }

  /** Returns the recorded outer class, absent for local and anonymous classes. */
  @NonNull
  public Optional<JavaClassType> getOuterClass() {
    return Optional.ofNullable(outerClass);
  }

  /** Returns the simple name, or an empty optional for an anonymous class. */
  @NonNull
  public Optional<String> getInnerName() {
    return Optional.ofNullable(innerName);
  }

  /** Returns the raw inner_class_access_flags bits, including member visibility and static. */
  public int getAccessFlags() {
    return accessFlags;
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof JavaInnerClassInfo)) {
      return false;
    }
    JavaInnerClassInfo that = (JavaInnerClassInfo) other;
    return accessFlags == that.accessFlags
        && innerClass.equals(that.innerClass)
        && Objects.equals(outerClass, that.outerClass)
        && Objects.equals(innerName, that.innerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(innerClass, outerClass, innerName, accessFlags);
  }
}
