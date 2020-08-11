package de.upb.swt.soot.java.core.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.ModuleIdentifierFactory;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/** Represents the unique fully-qualified name of a Class (aka its signature). */
public class JavaClassType extends ClassType {

  /**
   * Sometimes we need to know which class is a JDK class. There is no simple way to distinguish a
   * user class and a JDK class, here we use the package prefix as the heuristic.
   */
  private static final Pattern LIBRARY_CLASS_PATTERN =
      Pattern.compile(
          "^(?:java\\.|sun\\.|javax\\.|com\\.sun\\.|org\\.omg\\.|org\\.xml\\.|org\\.w3c\\.dom)");

  private final String className;

  private final PackageName packageName;

  // TODO Can we hide this somehow from the public API surface?
  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link IdentifierFactory}
   *
   * @param className the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageName the corresponding package
   */
  public JavaClassType(final String className, final PackageName packageName) {
    String realClassName = className;
    if (realClassName.contains(".")) {
      realClassName = realClassName.replace(".", "$");
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
    if (!packageName.getPackageName().isEmpty()) {
      sb.append(packageName);
      sb.append('.');
    }
    sb.append(className);
    return sb.toString();
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }

  public Path toPath(FileType fileType) {
    return toPath(fileType, FileSystems.getDefault());
  }

  public Path toPath(FileType fileType, FileSystem fs) {
    String fileName = getFullyQualifiedName();
    // Todo: fix JavaClassType.toPath (possibly implement in Soot Java Bytecode module)
    //    for a java file the file name of the inner class is the name of outerclass
    //    e.g., for an inner class org.acme.Foo$Bar, the filename is org/acme/Foo.java
    //    if (fileType == FileType.JAVA && this.isInnerClass) {
    //      int idxInnerClassChar = fileName.indexOf("$");
    //      if (idxInnerClassChar != -1) {
    //        fileName = fileName.substring(0, idxInnerClassChar);
    //      }
    //    }
    return fs.getPath(fileName.replace('.', '/') + "." + fileType.getExtension());
  }

  public boolean isModuleInfo() {
    return this.className.equals(ModuleIdentifierFactory.MODULE_INFO_CLASS.className);
  }

  /** The simple class name. */
  public String getClassName() {
    return className;
  }

  /** The package in which the class resides. */
  public PackageName getPackageName() {
    return packageName;
  }

  public boolean isBuiltInClass() {
    // TODO: [ms] for java9 modules library check modules instead of that heuristic
    return LIBRARY_CLASS_PATTERN.matcher(getClassName()).find();
  }

  private static final class SplitPatternHolder {
    private static final char SPLIT_CHAR = '.';

    @Nonnull
    private static final Pattern SPLIT_PATTERN =
        Pattern.compile(Character.toString(SPLIT_CHAR), Pattern.LITERAL);
  }
}
