package de.upb.swt.soot.core.frontend;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Basic class for retrieving information that is needed to build a {@link SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public abstract class ClassSource extends AbstractClassSource {

  @Override
  @Nonnull
  public AbstractClass<? extends AbstractClassSource> buildClass(@Nonnull SourceType sourceType) {
    return new SootClass(this, sourceType);
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
      @Nonnull AnalysisInputLocation srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  /** Reads from the source to retrieve its methods. This may be an expensive operation. */
  @Nonnull
  public abstract Collection<SootMethod> resolveMethods() throws ResolveException;

  /** Reads from the source to retrieve its fields. This may be an expensive operation. */
  @Nonnull
  public abstract Collection<SootField> resolveFields() throws ResolveException;

  /** Reads from the source to retrieve its modifiers. This may be an expensive operation. */
  @Nonnull
  public abstract Set<Modifier> resolveModifiers();

  /**
   * Reads from the source to retrieve its directly implemented interfaces. This may be an expensive
   * operation.
   */
  @Nonnull
  public abstract Set<ClassType> resolveInterfaces();

  /**
   * Reads from the source to retrieve its superclass, if present. This may be an expensive
   * operation.
   */
  @Nonnull
  public abstract Optional<ClassType> resolveSuperclass();

  /**
   * Reads from the source to retrieve its outer class, if this is an inner class. This may be an
   * expensive operation.
   */
  @Nonnull
  public abstract Optional<ClassType> resolveOuterClass();

  /**
   * Reads from the source to retrieve its position in the source code. This may be an expensive
   * operation.
   */
  public abstract Position resolvePosition();
}
