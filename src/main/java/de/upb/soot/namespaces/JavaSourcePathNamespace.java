package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * An implementation of the {@link INamespace} interface for the Java source code path.
 * 
 * @author Linghui Luo
 *
 */
public class JavaSourcePathNamespace extends AbstractNamespace {

  private final String sourcePath;
  /**
   * Create a {@link JavaSourcePathNamespace} which locates java source code in the given source path.
   * 
   * @param sourcePath
   *          the source code path to search in
   */
  public JavaSourcePathNamespace(String sourcePath) {
    super(null);
    this.sourcePath = sourcePath;
  }

  @Override
  protected Collection<AbstractClassSource> getClassSources(SignatureFactory factory) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<AbstractClassSource> getClassSource(ClassSignature classSignature) {
    // TODO Auto-generated method stub
    return null;
  }
}
