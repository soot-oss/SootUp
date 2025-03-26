package sootup.core.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt
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
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.Signature;

/**
 * Represents the signature of a Class
 *
 * @author Markus Schmidt
 */
public abstract class ClassType extends ReferenceType implements Signature {

  public abstract String getFullyQualifiedName();

  public abstract String getClassName();

  public abstract PackageName getPackageName();

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    v.caseClassType(this);
    return v;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (getClass() != o.getClass()) {
      return false;
    }
    return getFullyQualifiedName().equals(((ClassType) o).getFullyQualifiedName());
  }

  @Override
  public String toString() {
    return getFullyQualifiedName();
  }
}
