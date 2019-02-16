package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.ISourceContent;
import de.upb.soot.namespaces.classprovider.asm.modules.AsmModuleSourceContent;

public class AsmJavaClassProvider implements de.upb.soot.namespaces.classprovider.IClassProvider {

  public AsmJavaClassProvider() {
  }

  @Override
  public de.upb.soot.namespaces.classprovider.AbstractClassSource createClassSource(
      de.upb.soot.namespaces.INamespace srcNamespace, java.nio.file.Path sourcePath,
      de.upb.soot.signatures.JavaClassSignature classSignature) {
    return new de.upb.soot.namespaces.classprovider.asm.AsmClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  /**
   * Provide the ASM representation of the class file.
   *
   * @param classSource
   *          The source to be read.
   * @return A representation of the class file.
   */
  @Override
  public de.upb.soot.namespaces.classprovider.ISourceContent getContent(AbstractClassSource classSource) {

    ISourceContent classNode;
    // FIXME: maybe check here if module info file ... and create other ClassSource
    if (classSource.getClassSignature().isModuleInfo()) {
      classNode = new AsmModuleSourceContent(classSource);
    } else {
      classNode = new AsmClassSourceContent(classSource);
    }

    return classNode;
  }

}
