package de.upb.soot.namespaces;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.util.NotYetImplementedException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * An implementation of the {@link INamespace} interface for the Java source code path.
 *
 * @author Linghui Luo
 */
public class JavaSourcePathNamespace extends AbstractNamespace {

  @Nonnull private final Set<String> sourcePath;

  /**
   * Create a {@link JavaSourcePathNamespace} which locates java source code in the given source
   * path.
   *
   * @param sourcePath the source code path to search in
   */
  public JavaSourcePathNamespace(@Nonnull Set<String> sourcePath) {
    // FIXME: [JMP] Is `null` intended here?
    super(null);

    this.sourcePath = sourcePath;
  }

  @Override
  @Nonnull
  public Collection<ClassSource> getClassSources(@Nonnull SignatureFactory factory) {
    // TODO Auto-generated methodRef stub
    throw new NotYetImplementedException("Getting class sources is not implemented, yet.");
  }

  @Override
  @Nonnull
  public Optional<ClassSource> getClassSource(@Nonnull JavaClassSignature classSignature) {
    // TODO Auto-generated methodRef stub
    throw new NotYetImplementedException("Getting class source is not implemented, yet.");
  }
}
