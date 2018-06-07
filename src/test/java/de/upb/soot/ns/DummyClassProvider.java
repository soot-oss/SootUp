package de.upb.soot.ns;

import com.sun.nio.zipfs.ZipPath;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import de.upb.soot.ns.classprovider.ClassSource;
import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.SignatureFactory;

/**
 * @author Manuel Benz created on 07.06.18
 */
class DummyClassProvider implements IClassProvider {
  private final SignatureFactory signatureFactory;

  public DummyClassProvider(SignatureFactory signatureFactory) {
    this.signatureFactory = signatureFactory;
  }

  @Override
  public Optional<ClassSource> getClass(INamespace ns, Path sourcePath) {
    Path sigPath = null;
    if (sourcePath instanceof ZipPath) {
      sigPath = sourcePath.getRoot().relativize(sourcePath);
    } else {
      sigPath = Paths.get("target/classes").relativize(sourcePath);
    }

    return Optional.of(new ClassSource(ns, PathUtils.signatureFromPath(sigPath, signatureFactory)) {
    });
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
