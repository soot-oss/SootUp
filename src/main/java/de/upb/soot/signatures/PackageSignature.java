package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Andreas Dann
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

/**
 * Represents a Java Package.
 *
 * @author Andreas Dann
 */
public class PackageSignature {

  /** Represents the default package. */
  public static final PackageSignature DEFAULT_PACKAGE = new PackageSignature("");

  /** The name of the package. */
  public final String packageName;

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by a
   * {@link DefaultSignatureFactory}
   *
   * @param packageName
   *          the package's name
   */
  protected PackageSignature(final String packageName) {
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
    PackageSignature that = (PackageSignature) o;
    return Objects.equal(packageName, that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(packageName);
  }

  @Override
  public String toString() {
    return packageName;
  }
}
