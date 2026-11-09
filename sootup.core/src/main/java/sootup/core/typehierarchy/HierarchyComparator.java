package sootup.core.typehierarchy;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2023 Jonas Klauke
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

import java.util.Comparator;
import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;

/**
 * Comparator to sort ClassTypes which are ideally connected in a Hierarchy. subtypes will be before
 * super classes in the resulting order.
 */
public class HierarchyComparator implements Comparator<ClassType> {

  TypeHierarchy typeHierarchy;

  public HierarchyComparator(@NonNull TypeHierarchy hierarchy) {
    this.typeHierarchy = hierarchy;
  }

  @Override
  public int compare(ClassType classType1, ClassType classType2) {
    // classType1 is a subclass type of classType2
    if (typeHierarchy.isSubtype(classType2, classType1)) {
      return -1;
    }
    // classType1 is a subclass type of classType2
    if (typeHierarchy.isSubtype(classType1, classType2)) {
      return 1;
    }
    // classType1 and classType2 are on the same hierarchy level
    return 0;
  }
}
