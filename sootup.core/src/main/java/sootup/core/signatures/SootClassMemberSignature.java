package sootup.core.signatures;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Jan Martin Persch, Christian Brüggemann and others
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

import com.google.common.base.Objects;
import javax.annotation.Nonnull;
import sootup.core.model.SootClassMember;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Abstract class for the signature of a {@link SootClassMember}
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public abstract class SootClassMemberSignature<V extends SootClassMemberSubSignature>
    implements Signature {

  /** The signature of the declaring class. */
  @Nonnull private final ClassType declClassSignature;

  @Nonnull private final V subSignature;

  public SootClassMemberSignature(@Nonnull ClassType klass, @Nonnull V subSignature) {
    this.declClassSignature = klass;
    this.subSignature = subSignature;
  }

  @Nonnull
  public V getSubSignature() {
    return subSignature;
  }

  /** The signature of the declaring class. */
  @Nonnull
  public ClassType getDeclClassType() {
    return declClassSignature;
  }

  @Nonnull
  public Type getType() {
    return subSignature.getType();
  }

  @Nonnull
  public String getName() {
    return subSignature.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SootClassMemberSignature<V> that = (SootClassMemberSignature<V>) o;
    return Objects.equal(declClassSignature, that.declClassSignature)
        && Objects.equal(subSignature, that.subSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(declClassSignature, subSignature);
  }

  @Override
  @Nonnull
  public String toString() {
    return "<" + declClassSignature + ": " + getSubSignature() + '>';
  }
}
