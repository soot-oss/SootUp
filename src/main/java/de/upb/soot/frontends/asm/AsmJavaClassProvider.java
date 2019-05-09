package de.upb.soot.frontends.asm;

import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.asm.modules.AsmModuleClassSource;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class AsmJavaClassProvider implements IClassProvider {

  public AsmJavaClassProvider() {}

  @Override
  public AbstractClassSource createClassSource(
      INamespace srcNamespace, Path sourcePath, JavaClassType classSignature) {
    SootClassNode classNode = new SootClassNode();

    AsmUtil.initAsmClassSource(sourcePath, classNode);

    if (classSignature.isModuleInfo()) {
      return new AsmModuleClassSource(srcNamespace, sourcePath, classSignature, classNode.module);

    } else {
      return new AsmClassSource(srcNamespace, sourcePath, classSignature, classNode);
    }
  }

  @Override
  @Nonnull
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  static class SootClassNode extends ClassNode {

    public SootClassNode() {
      super(AsmUtil.SUPPORTED_ASM_OPCODE);
    }

    @Override
    @Nonnull
    public MethodVisitor visitMethod(
        int access,
        @Nonnull String name,
        @Nonnull String desc,
        @Nonnull String signature,
        @Nonnull String[] exceptions) {

      AsmMethodSourceContent mn =
          new AsmMethodSourceContent(access, name, desc, signature, exceptions);
      methods.add(mn);
      return mn;
    }
  }
}
