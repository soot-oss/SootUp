package sootup.core.typehierarchy;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
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
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** For internal use only. */
public interface MutableTypeHierarchy extends TypeHierarchy {

  /**
   * Adds the type to the hierarchy, updating all structures to reflect its presence as if it had
   * been a member of the hierarchy from the beginning.
   *
   * <p>For internal use only.
   */
  void addType(SootClass sootClass);

  /**
   * Notifies the hierarchy that the class of a type it already contains was replaced by a class
   * with the same superclass and interfaces, e.g. because a method was added. The hierarchy itself
   * does not change, but cached class objects must not be returned anymore.
   *
   * <p>For internal use only.
   */
  void updateType(SootClass sootClass);

  /**
   * Removes the declaration of the given type, e.g. because its class was removed from the view.
   * The hierarchy then answers like one built without that class: a type that other types still
   * name as their superclass or interface remains as such a reference, any other type is gone.
   *
   * <p>For internal use only.
   */
  void removeType(ClassType type);
}
