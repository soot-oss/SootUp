package de.upb.soot.namespaces.classprovider;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 22.05.2018 Manuel Benz
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

import static com.google.common.base.Preconditions.checkNotNull;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

/**
 * Basic class for storing information that is needed to resolve a {@link de.upb.soot.core.SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 **/
public abstract class ClassSource {
  private final INamespace srcNamespace;
  protected ClassSignature classSignature;

  /**
   * Creates a {@link ClassSource} which resides in the given {@link INamespace}.
   * 
   * @param srcNamespace
   *          The {@link INamespace} that handles this source
   * @param classSignature
   *          The {@link ClassSignature} of the to-be-resolved {@link de.upb.soot.core.SootClass}
   */
  public ClassSource(INamespace srcNamespace, ClassSignature classSignature) {
    checkNotNull(srcNamespace);

    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
  }

  public ClassSignature getClassSignature() {
    return classSignature;
  }
}
