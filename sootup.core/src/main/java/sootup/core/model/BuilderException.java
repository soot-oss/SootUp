package sootup.core.model;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Jan Martin Persch
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
import javax.annotation.Nullable;

/**
 * Represents a build error.
 *
 * @author Jan Martin Persch
 */
public class BuilderException extends IllegalStateException {

  /**
   * Creates a new instance of the {@link BuilderException} class.
   *
   * @param builderClass The builder class.
   * @param buildableClass The class to build.
   */
  public BuilderException(@Nonnull Class<?> builderClass, @Nonnull Class<?> buildableClass) {
    this(builderClass, buildableClass, null);
  }

  /**
   * Creates a new instance of the {@link BuilderException} class.
   *
   * @param builderClass The builder class.
   * @param buildableClass The class to build.
   * @param cause The cause.
   */
  public BuilderException(
      @Nonnull Class<?> builderClass, @Nonnull Class<?> buildableClass, @Nullable Throwable cause) {
    super(makeMessage(builderClass, buildableClass, cause), cause);

    this._builderClass = builderClass;
    this._buildableClass = buildableClass;
  }

  @Nonnull private final Class<?> _builderClass;

  /**
   * Gets the builder class.
   *
   * @return The value to get.
   */
  @Nonnull
  public Class<?> getBuilderClass() {
    return this._builderClass;
  }

  @Nonnull private final Class<?> _buildableClass;

  /**
   * Gets the buildable class.
   *
   * @return The value to get.
   */
  @Nonnull
  public Class<?> getBuildableClass() {
    return this._buildableClass;
  }

  @Nonnull
  private static String makeMessage(
      @Nonnull Class<?> builderClass, @Nonnull Class<?> buildableClass, @Nullable Throwable cause) {
    return "A builder exception occurred"
        + (cause != null ? ":\n\n" + cause.getMessage() : ".")
        + "\n\n"
        + "Builder class:   "
        + builderClass.getName()
        + "\n"
        + "Buildable class: "
        + buildableClass.getName();
  }
}
