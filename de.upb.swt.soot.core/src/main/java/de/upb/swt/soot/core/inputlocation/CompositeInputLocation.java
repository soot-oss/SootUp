package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Composes an input location out of other inputLocations hence removing the necessity to adapt
 * every API to allow for multiple inputLocations
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Jan Martin Persch
 */
public class CompositeInputLocation implements AnalysisInputLocation {
  private @Nonnull List<AnalysisInputLocation> inputLocations;

  /**
   * Creates a new instance of the {@link CompositeInputLocation} class.
   *
   * @param inputLocations The composited input locations.
   * @throws IllegalArgumentException <i>inputLocations</i> is empty.
   */
  public CompositeInputLocation(
      @Nonnull Collection<? extends AnalysisInputLocation> inputLocations) {
    List<AnalysisInputLocation> unmodifiableInputLocations =
        Collections.unmodifiableList(new ArrayList<>(inputLocations));

    if (unmodifiableInputLocations.isEmpty()) {
      throw new IllegalArgumentException("The collection of inputLocations must not be empty.");
    }

    this.inputLocations = unmodifiableInputLocations;
  }

  /**
   * Provides the first class source instance found in the inputLocations represented.
   *
   * @param signature The class to be searched.
   * @return The {@link ClassSource} instance found or created... Or an empty Optional.
   */
  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(@Nonnull JavaClassType signature) {
    List<AbstractClassSource> result =
        inputLocations.stream()
            .map(n -> n.getClassSource(signature))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (result.size() > 1) {
      throw new ResolveException("Class " + signature + " found in multiple inputLocations.");
    }
    return result.stream().findFirst();
  }

  /**
   * Provides the class provider of the first namespace in the composition.
   *
   * @return An instance of {@link ClassProvider} to be used.
   */
  @Override
  public @Nonnull ClassProvider getClassProvider() {
    return inputLocations.stream()
        .findFirst()
        .map(AnalysisInputLocation::getClassProvider)
        .orElseThrow(() -> new ResolveException("FATAL ERROR: No class provider found."));
  }

  @Override
  public @Nonnull Collection<AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return inputLocations.stream()
        .flatMap(n -> n.getClassSources(identifierFactory).stream())
        .collect(Collectors.toList());
  }
}
