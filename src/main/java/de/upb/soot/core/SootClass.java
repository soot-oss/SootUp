package de.upb.soot.core;

import de.upb.soot.jimple.common.type.Type;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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

import de.upb.soot.namespaces.classprovider.ClassSource;

/**
 * Soot's counterpart of the source languages class concept.
 *
 * @author Manuel Benz created on 06.06.18
 */
public class SootClass {
  public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = null;
  public static final String HIERARCHY = null;

  public SootClass(ClassSource cs) {
  }

  public void checkLevelIgnoreResolving(String hierarchy2) {
    // TODO Auto-generated method stub

  }

  public boolean isInterface() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isPhantom() {
    // TODO Auto-generated method stub
    return false;
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public SootClass getSuperclassUnsafe() {
    // TODO Auto-generated method stub
    return null;
  }

  public Type getType() {
    // TODO Auto-generated method stub
    return null;
  }

}
