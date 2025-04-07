package sootup.interceptors.typeresolving.types;

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

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.*;

/**
 * This type is an imaginary/intermediate type which is used to calculate Type Inference
 *
 * @author Zun Wang
 */
public class BottomType extends Type {

  @NonNull private static final BottomType INSTANCE = new BottomType();

  @NonNull
  public static BottomType getInstance() {
    return INSTANCE;
  }

  private BottomType() {}

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "BottomType";
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
    return true;
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
    return null;
  }

  @Override
  protected Type asBottomType() {
    return this;
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
    return Optional.empty();
  }

  @Override
  protected Optional<Type> toBottomType() {
    return Optional.of(this);
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
