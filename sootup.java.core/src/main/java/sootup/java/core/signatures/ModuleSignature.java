package sootup.java.core.signatures;

/*-
 * #%L
 * Soot
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
import sootup.core.IdentifierFactory;
import sootup.core.signatures.Signature;

// TODO Rename this too?

/** Represents a Java 9 module. */
public class ModuleSignature implements Signature {
  /**
   * The unnamed module. If a request is made to load a type whose package is not defined in any
   * module then the module system load it from the classpath. To ensure that every type is
   * associated with a module, the type is associated with the unnamed module. @see <a
   * href=http://openjdk.java.net/projects/jigsaw/spec/sotms/#the-unnamed-module>http://openjdk.java.net/projects/jigsaw/spec/sotms/#the-unnamed-module</a>
   */
  public static final ModuleSignature UNNAMED_MODULE = new ModuleSignature("");

  private final String moduleName;

  /**
   * Construct Module Signature of a Java 9 module. Instances should only be created a {@link
   * IdentifierFactory}
   *
   * @param moduleName module's name
   */
  public ModuleSignature(final String moduleName) {
    this.moduleName = moduleName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModuleSignature that = (ModuleSignature) o;
    return Objects.equal(moduleName, that.moduleName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(moduleName);
  }

  @Override
  public String toString() {
    return moduleName;
  }

  /** The name of the module. */
  public String getModuleName() {
    return moduleName;
  }

  public boolean isUnnamedModule() {
    return moduleName.isEmpty();
  }
}
