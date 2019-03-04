package de.upb.soot.signatures;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018 Secure Software Engineering Department, University of Paderborn
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

/** Represents Java's primitive types. */
public class PrimitiveTypeSignature extends TypeSignature {

  public static final PrimitiveTypeSignature BYTE_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("byte");

  public static final PrimitiveTypeSignature SHORT_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("short");

  public static final PrimitiveTypeSignature INT_TYPE_SIGNATURE = new PrimitiveTypeSignature("int");

  public static final PrimitiveTypeSignature LONG_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("long");

  public static final PrimitiveTypeSignature FLOAT_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("float");

  public static final PrimitiveTypeSignature DOUBLE_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("double");

  public static final PrimitiveTypeSignature CHAR_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("char");

  public static final PrimitiveTypeSignature BOOLEAN_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("boolean");

  /** The primitive type's name. */
  public final String name;

  /**
   * Signatures of primitive types are unique and should not be created from the outside, thus the
   * constructor is private.
   *
   * @param name the primitive's name
   */
  private PrimitiveTypeSignature(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
