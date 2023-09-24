package sootup.core.signatures;

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
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;

/**
 * Represents a Java Package.
 *
 * @author Andreas Dann
 */
public class PackageName {

  /** Represents the default package. */
  public static final PackageName DEFAULT_PACKAGE = new PackageName("");

  public static final String PACKAGE_INFO = "package-info";

  private final String packageName;

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link IdentifierFactory }
   *
   * @param packageName the package's name
   */
  public PackageName(final String packageName) {
    this.packageName = packageName;
  }

  /** The name of the package. */
  @Nonnull
  @Deprecated // "use getName()"
  public String getPackageName() {
    return packageName;
  }

  @Nonnull
  public String getName() {
    return packageName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PackageName)) {
      return false;
    }
    PackageName that = (PackageName) o;
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
