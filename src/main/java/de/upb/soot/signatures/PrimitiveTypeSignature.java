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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Optional;

/** Represents Java's primitive types. */
public class PrimitiveTypeSignature extends TypeSignature {
  
  @Nonnull private static final PrimitiveTypeSignature BYTE_TYPE_SIGNATURE = new PrimitiveTypeSignature("byte");

  @Nonnull private static final PrimitiveTypeSignature SHORT_TYPE_SIGNATURE = new PrimitiveTypeSignature("short");

  @Nonnull private static final PrimitiveTypeSignature INT_TYPE_SIGNATURE = new PrimitiveTypeSignature("int");

  @Nonnull private static final PrimitiveTypeSignature LONG_TYPE_SIGNATURE = new PrimitiveTypeSignature("long");

  @Nonnull private static final PrimitiveTypeSignature FLOAT_TYPE_SIGNATURE = new PrimitiveTypeSignature("float");

  @Nonnull private static final PrimitiveTypeSignature DOUBLE_TYPE_SIGNATURE = new PrimitiveTypeSignature("double");

  @Nonnull private static final PrimitiveTypeSignature CHAR_TYPE_SIGNATURE = new PrimitiveTypeSignature("char");

  @Nonnull private static final PrimitiveTypeSignature BOOLEAN_TYPE_SIGNATURE = new PrimitiveTypeSignature("boolean");

  @Nonnull private static final ImmutableMap<String, PrimitiveTypeSignature> CACHED_SIGNATURES;
  
  static {
    CACHED_SIGNATURES =
      ImmutableMap.<String, PrimitiveTypeSignature>builder()
        .put(BYTE_TYPE_SIGNATURE.getName(), BYTE_TYPE_SIGNATURE)
        .put(SHORT_TYPE_SIGNATURE.getName(), SHORT_TYPE_SIGNATURE)
        .put(INT_TYPE_SIGNATURE.getName(), INT_TYPE_SIGNATURE)
        .put(LONG_TYPE_SIGNATURE.getName(), LONG_TYPE_SIGNATURE)
        .put(FLOAT_TYPE_SIGNATURE.getName(), FLOAT_TYPE_SIGNATURE)
        .put(DOUBLE_TYPE_SIGNATURE.getName(), DOUBLE_TYPE_SIGNATURE)
        .put(CHAR_TYPE_SIGNATURE.getName(), CHAR_TYPE_SIGNATURE)
        .put(BOOLEAN_TYPE_SIGNATURE.getName(), BOOLEAN_TYPE_SIGNATURE)
        .build();
  }
  
  /**
   * Signatures of primitive types are unique and should not be created from the outside, thus the
   * constructor is private.
   *
   * @param name the primitive's name
   */
  private PrimitiveTypeSignature(@Nonnull String name) {
    this._name = name;
  }

  @Nonnull private final String _name;
  
  /**
   * Gets the primitive type's name.
   * 
   * @return The value to get.
   */
  @Nonnull
  public String getName()
  {
  	return this._name;
  }
  
  @Override
  @Nonnull
  public String toString() {
    return _name;
  }
  
  @Override
  @Nonnull
  public String toQuotedString() {
    return "'" + this._name + "'";
  }
  
  @Nonnull
  public static ImmutableCollection<PrimitiveTypeSignature> all() {
    return CACHED_SIGNATURES.values();
  }
  
  @Nonnull
  public static PrimitiveTypeSignature of(@Nonnull String name) {
    return find(name).orElseThrow(() -> new IllegalArgumentException("Name of primitive type not found."));
  }
  
  @Nonnull
  public static Optional<PrimitiveTypeSignature> find(@Nonnull String name) {
    return Optional.ofNullable(CACHED_SIGNATURES.get(name));
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getByteSignature() {
    return BYTE_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getShortSignature() {
    return SHORT_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getIntSignature() {
    return INT_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getLongSignature() {
    return LONG_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getFloatSignature() {
    return FLOAT_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getDoubleSignature() {
    return DOUBLE_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getCharSignature() {
    return CHAR_TYPE_SIGNATURE;
  }
  
  @Nonnull
  public static PrimitiveTypeSignature getBooleanSignature() {
    return BOOLEAN_TYPE_SIGNATURE;
  }
}
