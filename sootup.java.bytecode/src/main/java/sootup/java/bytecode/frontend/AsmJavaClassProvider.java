package sootup.java.bytecode.frontend;
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
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.AnnotationType;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.types.ModuleJavaClassType;

/** A {@link ClassProvider} capable of handling Java bytecode */
public class AsmJavaClassProvider implements ClassProvider<JavaSootClass> {

  @Nonnull private final View<?> view;

  public AsmJavaClassProvider(@Nonnull View<?> view) {
    this.view = view;
  }

  @Override
  public AbstractClassSource<JavaSootClass> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> analysisInputLocation,
      Path sourcePath,
      ClassType classType) {
    SootClassNode classNode = new SootClassNode(analysisInputLocation);

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
        return new AsmAnnotationClassSource(
            analysisInputLocation, sourcePath, klassType, classNode);
      }

      return new AsmClassSource(analysisInputLocation, sourcePath, klassType, classNode);
    }
  }

  @Override
  @Nonnull
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  class SootClassNode extends ClassNode {

    private final AnalysisInputLocation<? extends SootClass<?>> analysisInputLocation;

    SootClassNode(AnalysisInputLocation<? extends SootClass<?>> analysisInputLocation) {
      super(AsmUtil.SUPPORTED_ASM_OPCODE);
      this.analysisInputLocation = analysisInputLocation;
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
          new AsmMethodSource(
              access,
              name,
              desc,
              signature,
              exceptions,
              view,
              view.getBodyInterceptors(analysisInputLocation));
      methods.add(mn);
      return mn;
    }
  }
}
