package sootup.spark.node;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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

import org.jspecify.annotations.NonNull;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;

/**
 * A class that only exists inside SPARK, e.g. the owner of its global scope and of the pseudo field
 * of array elements. It never equals a class of the analyzed program, even one with the same name:
 * {@link ClassType#equals} compares the implementation class.
 */
public final class SyntheticClassType extends ClassType {

  /** The class of SPARK's global scope and of the array element field. */
  public static final SyntheticClassType GLOBAL = new SyntheticClassType("GLOBAL");

  @NonNull private final String name;

  private SyntheticClassType(@NonNull String name) {
    this.name = name;
  }

  @Override
  public String getFullyQualifiedName() {
    return name;
  }

  @Override
  public String getClassName() {
    return name;
  }

  @Override
  public PackageName getPackageName() {
    return PackageName.DEFAULT_PACKAGE;
  }
}
