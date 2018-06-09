package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Manuel Benz created on 07.06.18
 */
class DummyClassProvider implements IClassProvider {

  public DummyClassProvider() {
  }

  @Override
  public Optional<ClassSource> getClass(INamespace ns, Path sourcePath, ClassSignature classSignature) {

    return Optional.of(new ClassSource(ns, classSignature) {
    });
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
