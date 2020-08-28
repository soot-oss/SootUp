package de.upb.swt.soot.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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
