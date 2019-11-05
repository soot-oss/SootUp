package de.upb.swt.soot.java.core.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
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
import de.upb.swt.soot.core.signatures.PackageName;

/** Represents the signature of a Java 9 package, referencing its module. */
public class ModulePackageName extends PackageName {

  private final ModuleSignature moduleSignature;

  /**
   * Internal: Constructs a Package Signature for Java 9 Packages. Instances should only be created
   * by a {@link IdentifierFactory}
   *
   * @param packageName the package's name
   * @param moduleSignature the module declaring the package
   */
  public ModulePackageName(final String packageName, final ModuleSignature moduleSignature) {
    super(packageName);
    this.moduleSignature = moduleSignature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ModulePackageName that = (ModulePackageName) o;
    return Objects.equal(moduleSignature, that.moduleSignature)
        && Objects.equal(getPackageName(), that.getPackageName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), moduleSignature);
  }

  /** The module in which this package resides. */
  public ModuleSignature getModuleSignature() {
    return moduleSignature;
  }
}
