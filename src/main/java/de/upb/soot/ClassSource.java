/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package de.upb.soot;

import com.google.common.base.Preconditions;

import de.upb.soot.ns.INamespace;

/**
 * A class source is responsible for resolving a single class from a particular source format
 * (.class, .jimple, .java, etc.)
 */
public abstract class ClassSource {
  private final INamespace srcNS;
  protected String className;

  public ClassSource(String className, INamespace srcNS) {
    Preconditions.checkNotNull(className);
    Preconditions.checkNotNull(srcNS);

    this.srcNS = srcNS;
    this.className = className;
  }

  /**
   * Resolve the class into the SootClass sc. Returns a list of Strings or Types referenced by the
   * class.
   */
  //public abstract Dependencies resolve(SootClass sc);

  public void close() {}

  public String getName() {
    return className;
  }
}
