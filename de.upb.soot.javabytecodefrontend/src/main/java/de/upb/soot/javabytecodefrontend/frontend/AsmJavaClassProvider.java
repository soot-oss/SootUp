package de.upb.soot.javabytecodefrontend.frontend;

import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.frontend.ClassProvider;
import de.upb.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.soot.core.inputlocation.FileType;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.javabytecodefrontend.frontend.modules.AsmModuleClassSource;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class AsmJavaClassProvider implements ClassProvider {

  public AsmJavaClassProvider() {}

  @Override
  public AbstractClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, JavaClassType classSignature) {
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

      AsmMethodSource mn = new AsmMethodSource(access, name, desc, signature, exceptions);
      methods.add(mn);
      return mn;
    }
  }
}
