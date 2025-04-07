package sootup.core.types;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Christian Brüggemann, Jan Martin Persch
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

/** Represents Java's 'void' type as methods return's type. */
public class VoidType extends Type {

  @NonNull private static final VoidType INSTANCE = new VoidType();

  @NonNull
  public static VoidType getInstance() {
    return INSTANCE;
  }

  private VoidType() {}

  @Override
  @NonNull
  public String toString() {
    return "void";
  }

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    v.caseVoidType();
    return v;
  }

  @Override
  protected boolean isPrimitiveType() {
    return false;
  }

  @Override
  protected boolean isReferenceType() {
    return false;
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
    return true;
  }

  @Override
  protected PrimitiveType asPrimitiveType() {
    return null;
  }

  @Override
  protected ReferenceType asReferenceType() {
    return null;
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
    return this;
  }

  @Override
  protected Optional<PrimitiveType> toPrimitiveType() {
    return Optional.empty();
  }

  @Override
  protected Optional<ReferenceType> toReferenceType() {
    return Optional.empty();
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
    return Optional.of(this);
  }
}
