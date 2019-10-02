package de.upb.swt.soot.core.frontend;

import static com.google.common.base.Preconditions.checkNotNull;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.JavaClassType;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Basic class for storing information that is needed to reify a {@link SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public abstract class ClassSource extends AbstractClassSource {

  @Override
  public AbstractClass buildClass() {
    // TODO: [cb] Don't use a fixed SourceType here. [ms]: lift determination of SourceType up to
    // classSource->AnalysisInputLocation?
    return new SootClass(this, SourceType.Application);
  }

  /**
   * Creates and a {@link ClassSource} for a specific source file. The file should be passed as
   * {@link Path} and can be located in an arbitrary {@link java.nio.file.FileSystem}.
   * Implementations should use {@link java.nio.file.Files#newInputStream(Path, OpenOption...)} to
   * access the file.
   *
   * @param srcNamespace The {@link AnalysisInputLocation} that holds the given file
   * @param sourcePath Path to the source file of the to-be-created {@link ClassSource}. The given
   *     path has to exist and requires to be handled by this {@link ClassProvider}. Implementations
   *     might double check this if wanted.
   * @param classSignature the signature that has been used to resolve this class
   * @return A not yet resolved {@link ClassSource}, backed up by the given file A not yet resolved
   *     {@link ClassSource}, backed up by the given file
   */
  public ClassSource(
      AnalysisInputLocation srcNamespace, JavaClassType classSignature, Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
    checkNotNull(srcNamespace);
  }

  @Nonnull
  public abstract Collection<SootMethod> resolveMethods() throws ResolveException;

  @Nonnull
  public abstract Collection<SootField> resolveFields() throws ResolveException;

  @Nonnull
  public abstract Set<Modifier> resolveModifiers();

  @Nonnull
  public abstract Set<JavaClassType> resolveInterfaces();

  @Nonnull
  public abstract Optional<JavaClassType> resolveSuperclass();

  @Nonnull
  public abstract Optional<JavaClassType> resolveOuterClass();

  public abstract Position resolvePosition();
}
