package de.upb.soot.namespaces;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.JavaClassType;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.util.NotYetImplementedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
public class CompositeNamespace implements INamespace {
  private @Nonnull List<INamespace> namespaces;

  /**
   * Creates a new instance of the {@link CompositeNamespace} class.
   *
   * @param namespaces The composited namespaces.
   * @throws IllegalArgumentException <i>namespaces</i> is empty.
   */
  public CompositeNamespace(@Nonnull Collection<? extends INamespace> namespaces) {
    List<INamespace> unmodifiableNamespaces =
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
  public @Nonnull Optional<ClassSource> getClassSource(@Nonnull JavaClassType signature) {
    List<ClassSource> result =
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
   * @return An instance of {@link IClassProvider} to be used.
   */
  @Override
  public @Nonnull IClassProvider getClassProvider() {
    return namespaces.stream()
        .findFirst()
        .map(INamespace::getClassProvider)
        .orElseThrow(() -> new RuntimeException("FATAL ERROR: No class provider found."));
  }

  @Override
  public @Nonnull Collection<ClassSource> getClassSources(@Nonnull SignatureFactory factory) {
    // TODO Auto-generated methodRef stub
    throw new NotYetImplementedException("Getting class sources is not implemented, yet.");
  }
}
