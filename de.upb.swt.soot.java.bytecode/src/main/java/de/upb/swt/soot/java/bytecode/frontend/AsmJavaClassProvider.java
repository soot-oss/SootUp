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
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.types.AnnotationType;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.types.ModuleJavaClassType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

/** A {@link ClassProvider} capable of handling Java bytecode */
public class AsmJavaClassProvider implements ClassProvider<JavaSootClass> {

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  public AsmJavaClassProvider(@Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  public AbstractClassSource<JavaSootClass> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> srcNamespace,
      Path sourcePath,
      ClassType classType) {
    SootClassNode classNode = new SootClassNode();

    try {
      AsmUtil.initAsmClassSource(sourcePath, classNode);
    } catch (IOException exception) {
      throw new ResolveException(
          exception.getMessage(), sourcePath, NoPositionInformation.getInstance(), exception);
    }

    JavaClassType klassType = (JavaClassType) classType;
    if (klassType instanceof ModuleJavaClassType
        && klassType.getClassName().equals(JavaModuleIdentifierFactory.MODULE_INFO_FILE)) {
      throw new ResolveException(
          "Can not create ClassSource from a module info descriptor!", sourcePath);
    } else {
      if (klassType instanceof AnnotationType) {
        return new AsmAnnotationClassSource(srcNamespace, sourcePath, klassType, classNode);
      }

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
