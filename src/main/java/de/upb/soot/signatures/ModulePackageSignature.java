package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 1997 - 2018 Secure Software Engineering Department, University of Paderborn
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

/** Represents the signature of a Java 9 package, referencing its module. */
public class ModulePackageSignature extends PackageSignature {

  /** The module in which this package resides. */
  public final ModuleSignature moduleSignature;

  /**
   * Internal: Constructs a Package Signature for Java 9 Packages. Instances should only be created by a
   * {@link SignatureFactory}
   *
   * @param packageName
   *          the package's name
   * @param moduleSignature
   *          the module declaring the package
   */
  protected ModulePackageSignature(final String packageName, final ModuleSignature moduleSignature) {
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
    ModulePackageSignature that = (ModulePackageSignature) o;
    return Objects.equal(moduleSignature, that.moduleSignature) && Objects.equal(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), moduleSignature);
  }
}
