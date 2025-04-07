package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Jan Martin Persch, Christian Brüggemann, Andreas Dann
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
import sootup.core.jimple.visitor.TypeVisitor;

/** Represents a signature for a <code>null</code>-reference. */
public class NullType extends ReferenceType {

  @NonNull private static final NullType INSTANCE = new NullType();

  @NonNull
  public static NullType getInstance() {
    return INSTANCE;
  }

  private NullType() {}

  @Override
  @NonNull
  public String toString() {
    return "null";
  }

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    v.caseNullType();
    return v;
  }

  @Override
  protected boolean isClassType() {
    return false;
  }

  @Override
  protected boolean isArrayType() {
    return false;
  }

  @Override
  protected boolean isNullType() {
    return true;
  }

  @Override
  protected ClassType asClassType() {
    return null;
  }

  @Override
  protected ArrayType asArrayType() {
    return null;
  }

  @Override
  protected NullType asNullType() {
    return this;
  }

  @Override
  protected Optional<ClassType> toClassType() {
    return Optional.empty();
  }

  @Override
  protected Optional<ArrayType> toArrayType() {
    return Optional.empty();
  }

  @Override
  protected Optional<NullType> toNullType() {
    return Optional.of(this);
  }

  @Override
  protected boolean isPrimitiveType() {
    return false;
  }

  @Override
  protected boolean isReferenceType() {
    return true;
  }

  @Override
  protected boolean isBottomType() {
    return false;
  }

  @Override
  protected boolean isTopType() {
    return false;
  }

  @Override
  protected boolean isUnknownType() {
    return false;
  }

  @Override
  protected boolean isVoidType() {
    return false;
  }

  @Override
  protected PrimitiveType asPrimitiveType() {
    return null;
  }

  @Override
  protected ReferenceType asReferenceType() {
    return this;
  }

  @Override
  protected Type asBottomType() {
    return null;
  }

  @Override
  protected Type asTopType() {
    return null;
  }

  @Override
  protected UnknownType asUnknownType() {
    return null;
  }

  @Override
  protected VoidType asVoidType() {
    return null;
  }

  @Override
  protected Optional<PrimitiveType> toPrimitiveType() {
    return Optional.empty();
  }

  @Override
  protected Optional<ReferenceType> toReferenceType() {
    return Optional.of(this);
  }

  @Override
  protected Optional<Type> toBottomType() {
    return Optional.empty();
  }

  @Override
  protected Optional<Type> toTopType() {
    return Optional.empty();
  }

  @Override
  protected Optional<UnknownType> toUnknownType() {
    return Optional.empty();
  }

  @Override
  protected Optional<VoidType> toVoidType() {
    return Optional.empty();
  }
}
