package sootup.java.bytecode.interceptors.typeresolving.types;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.signatures.PackageName;
import sootup.java.core.types.JavaClassType;

/**
 * This type is used for Type Inference. Object, Serializable, Cloneable are weak object types.
 *
 * @author Zun Wang
 */
public class WeakObjectType extends JavaClassType {

  public WeakObjectType(String className, PackageName packageName) {
    super(className, packageName);
    if (className.equals("Object") || className.equals("Cloneable")) {
      if (!packageName.toString().equals("java.lang")) {
        throw new RuntimeException(this + " is not an object with WeakObjectType");
      }
    } else if (className.equals("Serializable")) {
      if (!packageName.toString().equals("java.io")) {
        throw new RuntimeException(this + " is not an object with WeakObjectType");
      }
    } else {
      throw new RuntimeException(this + " is not an object with WeakObjectType");
    }
  }

  @Override
  public void accept(@Nonnull TypeVisitor v) {
    // todo: weak objects type case
  }
}
