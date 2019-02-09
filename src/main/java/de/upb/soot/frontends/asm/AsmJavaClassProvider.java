package de.upb.soot.frontends.asm;

import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.asm.modules.AsmModuleClassSourceContent;

public class AsmJavaClassProvider implements IClassProvider {

  public AsmJavaClassProvider() {
  }

  @Override
  public ClassSource createClassSource(
      de.upb.soot.namespaces.INamespace srcNamespace, java.nio.file.Path sourcePath,
      de.upb.soot.signatures.JavaClassSignature classSignature) {
    return new ClassSource(srcNamespace, sourcePath, classSignature);
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
  public IClassSourceContent getContent(ClassSource classSource) {

    IClassSourceContent classNode;
    // FIXME: maybe check here if module info file ... and create other ClassSource
    if (classSource.getClassSignature().isModuleInfo()) {
      classNode = new AsmModuleClassSourceContent(classSource);
    } else {
      classNode = new AsmClassClassSourceContent(classSource);
    }

    return classNode;
  }

}
