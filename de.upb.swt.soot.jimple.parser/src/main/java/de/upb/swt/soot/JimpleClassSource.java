package de.upb.swt.soot;

import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

public class JimpleClassSource extends SootClassSource {
  /**
   * Creates and a {@link SootClassSource} for a specific source file. The file should be passed as
   * {@link Path} and can be located in an arbitrary {@link FileSystem}. Implementations should use
   * {@link Files#newInputStream(Path, OpenOption...)} to access the file.
   *
   * @param srcNamespace The {@link AnalysisInputLocation} that holds the given file
   * @param classSignature the signature that has been used to resolve this class
   * @param sourcePath Path to the source file of the to-be-created {@link SootClassSource}. The
   *     given path has to exist and requires to be handled by this {@link ClassProvider}.
   *     Implementations might double check this if wanted.
   * @return A not yet resolved {@link SootClassSource}, backed up by the given file A not yet
   *     resolved {@link SootClassSource}, backed up by the given file
   */
  public JimpleClassSource(
      @Nonnull AnalysisInputLocation srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  @Nonnull
  @Override
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    // TODO: implement
    return null;
  }

  @Nonnull
  @Override
  public Collection<SootField> resolveFields() throws ResolveException {
    // TODO: implement
    return null;
  }

  @Nonnull
  @Override
  public Set<Modifier> resolveModifiers() {
    // TODO: implement
    return null;
  }

  @Nonnull
  @Override
  public Set<ClassType> resolveInterfaces() {
    // TODO: implement
    return null;
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveSuperclass() {
    // TODO: implement
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Optional<ClassType> resolveOuterClass() {
    // TODO: implement
    return Optional.empty();
  }

  @Override
  @Nonnull
  public Position resolvePosition() {
    return NoPositionInformation.getInstance();
  }
}
