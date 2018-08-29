package de.upb.soot.namespaces;

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

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;

import java.util.Collection;
import java.util.Optional;

/**
 * Public interface to a namespace. Namespaces are sources for {@link SootClass}es, e.g. Java Classpath, Android APK, JAR
 * file, etc.
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface INamespace {
  /**
   * Searches the namespace and sub-namespaces for all contained classes.
   * 
   * @return A collection of not-yet-resolved {@link SootClass}es
   */
  Collection<SootClass> getClasses();

  /**
   * Searches the namespace and all sub-namespaces for a {@link SootClass} matching the given {@link ClassSignature}.
   *
   * @param classSignature
   *          The {@link ClassSignature} denoting the searched {@link SootClass}
   * @return An optional containing the found class or empty if the class does not reside in this namespace
   */
  Optional<SootClass> getClass(ClassSignature classSignature);
}
