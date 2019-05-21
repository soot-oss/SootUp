package de.upb.soot.inputlocation;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.NotYetImplementedException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Composes a namespace out of other namespaces hence removing the necessity to adapt every API to
 * allow for multiple namespaces
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Jan Martin Persch
 */
public class CompositeNamespace implements AnalysisInputLocation {
  private @Nonnull List<AnalysisInputLocation> namespaces;

  /**
   * Creates a new instance of the {@link CompositeNamespace} class.
   *
   * @param namespaces The composited namespaces.
   * @throws IllegalArgumentException <i>namespaces</i> is empty.
   */
  public CompositeNamespace(@Nonnull Collection<? extends AnalysisInputLocation> namespaces) {
    List<AnalysisInputLocation> unmodifiableNamespaces =
        Collections.unmodifiableList(new ArrayList<>(namespaces));

    if (unmodifiableNamespaces.isEmpty()) {
      throw new IllegalArgumentException("The namespaces collection must not be empty.");
    }

    this.namespaces = unmodifiableNamespaces;
  }

  /**
   * Provides the first class source instance found in the namespaces represented.
   *
   * @param signature The class to be searched.
   * @return The {@link ClassSource} instance found or created... Or an empty Optional.
   */
  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(@Nonnull JavaClassType signature) {
    List<AbstractClassSource> result =
        namespaces.stream()
            .map(n -> n.getClassSource(signature))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (result.size() > 1) {
      // FIXME: [JMP] Is an empty result better than the first item in the list?
      // TODO: Warn here b/c of multiple results
      return Optional.empty();
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
    return namespaces.stream()
        .findFirst()
        .map(AnalysisInputLocation::getClassProvider)
        .orElseThrow(() -> new RuntimeException("FATAL ERROR: No class provider found."));
  }

  @Override
  public @Nonnull Collection<AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // TODO Auto-generated methodRef stub
    throw new NotYetImplementedException("Getting class sources is not implemented, yet.");
  }
}
