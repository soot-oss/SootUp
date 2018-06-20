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
    // if it is not in target, it is located in a zip archive
    if (!sourcePath.startsWith("target")) {
      sigPath = sourcePath.getRoot().relativize(sourcePath);
    } else {
      sigPath = Paths.get("target/classes").relativize(sourcePath);
    }

    return Optional.of(new ClassSource(ns, ClassSignature.fromPath(sigPath, signatureFactory)) {
    });
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
