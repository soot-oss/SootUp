package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.PackageName;

/*
 * Actually, we could get rid of this class but removing it potentially breaks
 * 3rd party code. Hence, keep it for now.
 */
public class JavaPackageName extends PackageName {

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link IdentifierFactory }
   *
   * @param packageName the package's name
   */
  public JavaPackageName(@Nonnull String packageName) {
    super(packageName);
  }
}
