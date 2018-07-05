package de.upb.soot.namespaces;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.namespaces.classprovider.asm.AsmClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;

/**
 * @author Manuel Benz created on 07.06.18
 */
class DummyClassProvider implements IClassProvider {

  public DummyClassProvider() {
  }

  @Override
  public SootClass getSootClass(ClassSource classSource) {
    return null;
  }

  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    return new AsmClassSource(srcNamespace, sourcePath, classSignature) {
    };
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
