package de.upb.soot.frontends.asm;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.JavaClassSource;
import de.upb.soot.frontends.asm.modules.AsmModuleClassSourceContent;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.JavaClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class AsmJavaClassProvider implements IClassProvider {

  public AsmJavaClassProvider() {}

  @Override
  public ClassSource createClassSource(
      INamespace srcNamespace, Path sourcePath, JavaClassType classSignature) {
    return new JavaClassSource(srcNamespace, sourcePath, classSignature);
  }

  @Override
  @Nonnull
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  /**
   * Provide the ASM representation of the class file.
   *
   * @param classSource The source to be read.
   * @return A representation of the class file.
   */
  @Override
  @Nonnull
  public IClassSourceContent getContent(@Nonnull ClassSource classSource) {

    IClassSourceContent classNode;
    if (classSource.getClassType().isModuleInfo()) {
      classNode = new AsmModuleClassSourceContent(classSource);
    } else {
      classNode = new AsmClassClassSourceContent(classSource);
    }

    return classNode;
  }
}
