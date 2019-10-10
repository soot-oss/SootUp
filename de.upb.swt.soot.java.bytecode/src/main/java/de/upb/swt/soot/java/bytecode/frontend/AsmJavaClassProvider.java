package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.java.bytecode.frontend.modules.AsmModuleClassSource;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

public class AsmJavaClassProvider implements ClassProvider {

  @Nullable private final List<BodyInterceptor> customBodyInterceptors;

  public AsmJavaClassProvider() {
    this(null);
  }

  public AsmJavaClassProvider(@Nullable List<BodyInterceptor> customBodyInterceptors) {
    this.customBodyInterceptors = customBodyInterceptors;
  }

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

  class SootClassNode extends ClassNode {

    SootClassNode() {
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

      AsmMethodSource mn;
      mn = new AsmMethodSource(access, name, desc, signature, exceptions, customBodyInterceptors);
      methods.add(mn);
      return mn;
    }
  }
}
