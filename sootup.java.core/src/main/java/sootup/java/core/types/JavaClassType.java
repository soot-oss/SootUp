package sootup.java.core.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Christian Brüggemann and others
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

import com.google.common.base.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.java.core.signatures.ModulePackageName;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class JavaClassType extends ClassType {

  /**
   * Sometimes we need to know which class is a JDK class. There is no simple way to distinguish a
   * user class and a JDK class, here we use the package prefix as the heuristic.
   */
  private static final Pattern LIBRARY_CLASS_PATTERN =
      Pattern.compile(
          "^(?:java\\.|sun\\.|javax\\.|com\\.sun\\.|org\\.omg\\.|org\\.xml\\.|org\\.w3c\\.dom|jdk|com\\.oracle\\.|org\\.ietf\\.|org\\.jcp\\.)");

  @Nonnull private final String className;
  @Nonnull private final PackageName packageName;

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link IdentifierFactory}
   *
   * @param className the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageName the corresponding package
   */
  public JavaClassType(@Nonnull final String className, @Nonnull final PackageName packageName) {
    String realClassName = className;
    // TODO: [ms] we shouldnt do that inner class conversion here? -> IdentifierFactory
    if (realClassName.contains(".")) {
      realClassName = realClassName.replace('.', '$');
    }
    this.className = realClassName;
    this.packageName = packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JavaClassType that = (JavaClassType) o;
    return Objects.equal(className, that.className) && Objects.equal(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(className, packageName);
  }

  /**
   * The fully-qualified name of the class. Concat package and class name , e.g.,
   * "java.lang.System".
   *
   * @return fully-qualified name
   */
  public String getFullyQualifiedName() {
    StringBuilder sb = new StringBuilder();
    if (!packageName.getName().isEmpty()) {
      sb.append(packageName.getName());
      sb.append('.');
    }
    sb.append(className);
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String packageNameStr = packageName.toString();
    if (!packageNameStr.isEmpty()) {
      sb.append(packageName);
      if (!packageName.getName().isEmpty()) {
        sb.append('.');
      }
    }
    sb.append(className);
    return sb.toString();
  }

  /** The simple class name. */
  @Nonnull
  public String getClassName() {
    return className;
  }

  /** The package in which the class resides. */
  @Nonnull
  public PackageName getPackageName() {
    return packageName;
  }

  public boolean isBuiltInClass() {
    PackageName packageName = getPackageName();
    if (packageName instanceof ModulePackageName) {
      // if java modules (>= java9) are used: use JrtFileSystem for explicit.. otherwise use the
      // following heuristic
      String moduleName = ((ModulePackageName) packageName).getModuleSignature().toString();
      return moduleName.startsWith("java.") || moduleName.startsWith("jdk.");
    }
    return LIBRARY_CLASS_PATTERN.matcher(packageName.getName()).find();
  }
}
