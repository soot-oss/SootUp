package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.frontend.modules.AsmModuleClassSource;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.ClassNode;

/** A {@link ClassProvider} capable of handling Java bytecode */
public class AsmJavaClassProvider implements ClassProvider {

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  public AsmJavaClassProvider(@Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;
  }

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

  class SootClassNode extends ClassNode {

    SootClassNode() {
      super(AsmUtil.SUPPORTED_ASM_OPCODE);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      // TODO: [ms] entrypoint for Annotations
      // https://asm.ow2.io/javadoc/org/objectweb/asm/ClassVisitor.html
      return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String descriptor, boolean visible) {
      // TODO: [ms] entrypoint for Annotation : "Visits an annotation on a type in the class
      // signature."
      return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    @Nonnull
    public MethodVisitor visitMethod(
        int access,
        @Nonnull String name,
        @Nonnull String desc,
        @Nonnull String signature,
        @Nonnull String[] exceptions) {

      AsmMethodSource mn =
          new AsmMethodSource(access, name, desc, signature, exceptions, bodyInterceptors);
      methods.add(mn);
      return mn;
    }
  }
}
