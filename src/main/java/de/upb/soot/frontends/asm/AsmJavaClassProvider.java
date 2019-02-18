package de.upb.soot.frontends.asm;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.asm.modules.AsmModuleClassSourceContent;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AsmJavaClassProvider implements IClassProvider {

  public AsmJavaClassProvider() {
  }

  @Override
  public ClassSource createClassSource(@Nonnull INamespace srcNamespace, @Nullable Path sourcePath,
      @Nullable JavaClassSignature classSignature) {
    return new ClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  public @Nonnull FileType getHandledFileType() {
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
  public @Nonnull IClassSourceContent getContent(@Nonnull ClassSource classSource) {

    IClassSourceContent classNode;
    if (classSource.getClassSignature().isModuleInfo()) {
      classNode = new AsmModuleClassSourceContent(classSource);
    } else {
      classNode = new AsmClassClassSourceContent(classSource);
    }

    return classNode;
  }
}
