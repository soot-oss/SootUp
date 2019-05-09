package de.upb.soot.util.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a build error.
 *
 * @see AbstractBuilder
 * @author Jan Martin Persch
 */
public class BuilderException extends IllegalStateException {
  // region Fields

  private static final long serialVersionUID = 3392235468289192358L;

  // endregion /Fields/

  // region Constructor

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

  // endregion /Constructor/

  // region Properties

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

  // endregion /Properties/

  // region Methods

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

  // endregion /Methods/
}
