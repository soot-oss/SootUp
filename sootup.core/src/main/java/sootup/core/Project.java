package sootup.core;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Ben Hermann, Markus Schmidt and others
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.model.SootClass;
import sootup.core.views.View;

/**
 * A Soot user should first define a Project instance to describe the outlines of an analysis run.
 * It is the starting point for all operations. You can have multiple instances of projects as there
 * is no information shared between them. All caches are always at the project level.
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public abstract class Project<S extends SootClass<?>, V extends View<? extends SootClass<?>>> {

  @Nonnull private final List<AnalysisInputLocation<? extends S>> inputLocations;
  @Nonnull private final SourceTypeSpecifier sourceTypeSpecifier;
  @Nonnull private final Language language;
  /**
   * Create a project from an arbitrary input location.
   *
   * @param language the language
   * @param inputLocation the input location
   * @param sourceTypeSpecifier the source type specifier
   */
  public Project(
      @Nonnull Language language,
      @Nonnull AnalysisInputLocation<? extends S> inputLocation,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this(language, Collections.singletonList(inputLocation), sourceTypeSpecifier);
  }

  /**
   * Create a project from an arbitrary list of input locations.
   *
   * @param language the language
   * @param inputLocations the input locations
   * @param sourceTypeSpecifier the source type specifier
   */
  public Project(
      @Nonnull Language language,
      @Nonnull List<AnalysisInputLocation<? extends S>> inputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this.language = language;
    List<AnalysisInputLocation<? extends S>> unmodifiableInputLocations =
        Collections.unmodifiableList(new ArrayList<>(inputLocations));

    this.sourceTypeSpecifier = sourceTypeSpecifier;
    this.inputLocations = unmodifiableInputLocations;
  }

  public void validate() {
    if (inputLocations.isEmpty()) {
      throw new IllegalArgumentException("The inputLocations collection must not be empty.");
    }
  }

  /**
   * Gets the inputLocations.
   *
   * @return
   */
  @Nonnull
  public List<AnalysisInputLocation<? extends S>> getInputLocations() {
    return inputLocations;
  }

  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return language.getIdentifierFactory();
  }

  @Nonnull
  public SourceTypeSpecifier getSourceTypeSpecifier() {
    return sourceTypeSpecifier;
  }

  @Nonnull
  public Language getLanguage() {
    return language;
  }

  /**
   * Create a view with a default cache.
   *
   * @return A view on the provided code
   */
  @Nonnull
  public abstract V createView();

  /**
   * Create a view with a provided cache.
   *
   * @return A view on the provided code
   */
  @Nonnull
  public abstract V createView(@Nonnull ClassCacheProvider<S> cacheProvider);

  /**
   * Creates an on-demand View that uses the default {@link ClassLoadingOptions} of each frontend.
   */

  /** Creates a View with custom {@link ClassLoadingOptions}. */
  @Nonnull
  public abstract V createView(
      @Nonnull ClassCacheProvider<S> cacheProvider,
      @Nonnull
          Function<AnalysisInputLocation<? extends S>, ClassLoadingOptions>
              classLoadingOptionsSpecifier);
}
