package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.frontend.modules.AsmModuleClassSource;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class AsmJavaClassProvider implements ClassProvider {

  public AsmJavaClassProvider() {}

  @Override
  public AbstractClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, ClassType classType) {
    SootClassNode classNode = new SootClassNode();

    AsmUtil.initAsmClassSource(sourcePath, classNode);

    JavaClassType klassType = (JavaClassType) classType;
    if (klassType.isModuleInfo()) {
      return new AsmModuleClassSource(srcNamespace, sourcePath, klassType, classNode.module);

    } else {
      return new AsmClassSource(srcNamespace, sourcePath, klassType, classNode);
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
