package sootup.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann and others
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

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.typehierarchy.ViewTypeHierarchy;

/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView implements View {
  @Nullable private TypeHierarchy typeHierarchy;

  public AbstractView() {}

  @Override
  @NonNull
  public TypeHierarchy getTypeHierarchy() {
    if (this.typeHierarchy == null) {
      typeHierarchy = new ViewTypeHierarchy(this);
    }
    return typeHierarchy;
  }

  /**
   * resolve and check for accessibility of the class from a given package * TODO: incorporate
   * AccessUtil @NonNull public synchronized Optional&lt;T&gt; getClass( @NonNull PackageName
   * entryPackage, @NonNull ClassType type) { Optional&lt;T&gt; aClass = getClass(type); if
   * (aClass.isPresent() &amp;&amp; AccessUtil.isAccessible(entryPackage, aClass.get()) ) { return
   * Optional.empty(); } return aClass; }
   */
  @Override
  @NonNull
  public Optional<? extends SootMethod> getMethod(@NonNull MethodSignature signature) {
    final Optional<? extends SootClass> aClass = getClass(signature.getDeclClassType());
    if (aClass.isEmpty()) {
      return Optional.empty();
    }
    return aClass.get().getMethod(signature.getSubSignature());
  }

  @Override
  @NonNull
  public Optional<? extends SootField> getField(@NonNull FieldSignature signature) {
    final Optional<? extends SootClass> aClass = getClass(signature.getDeclClassType());
    if (aClass.isEmpty()) {
      return Optional.empty();
    }
    return aClass.get().getField(signature.getSubSignature());
  }
}
