package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;

public class AsmJavaClassProvider implements IClassProvider {


  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
       return new ClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  /**
   * Provide the ASM representation of the class file.
   * @param classSource The source to be read.
   * @return A representation of the class file.
   */
  @Override
  public Object getContent(ClassSource classSource) {
    return null;
  }
}
