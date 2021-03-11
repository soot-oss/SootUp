package de.upb.swt.soot.core.model;

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

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.signatures.*;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class represents a class/module lives in {@link View}. It may have different
 * implementations, since we want to support multiple languages. An abstract class must be uniquely
 * identified by its {@link Signature}.
 *
 * @author Linghui Luo
 */
public abstract class AbstractClass<T extends AbstractClassSource<?>> {

  protected final T classSource;

  public AbstractClass(T cs) {
    this.classSource = cs;
  }

  public T getClassSource() {
    return classSource;
  }

  public abstract String getName();

  public abstract Type getType();

  @Nonnull
  public Optional<? extends SootMethod> getMethod(@Nonnull MethodSubSignature subSignature) {
    return getMethods().stream()
        .filter(m -> m.getSignature().getSubSignature().equals(subSignature))
        .findAny();
  }

  @Nonnull
  public abstract Set<? extends SootMethod> getMethods();

  @Nonnull
  public Optional<? extends SootField> getField(@Nonnull FieldSubSignature subSignature) {
    return getFields().stream()
        .filter(f -> f.getSignature().getSubSignature().equals(subSignature))
        .findAny();
  }

  @Nonnull
  public abstract Set<? extends SootField> getFields();
}
