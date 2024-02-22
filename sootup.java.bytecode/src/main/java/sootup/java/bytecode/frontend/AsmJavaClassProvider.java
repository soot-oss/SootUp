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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.types.AnnotationType;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.types.ModuleJavaClassType;

/** A {@link ClassProvider} capable of handling Java bytecode */
public class AsmJavaClassProvider implements ClassProvider {

  @Nonnull private final View view;
  private static final @Nonnull Logger logger = LoggerFactory.getLogger(AsmJavaClassProvider.class);

  public AsmJavaClassProvider(@Nonnull View view) {
    this.view = view;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      @Nonnull AnalysisInputLocation analysisInputLocation,
      @Nonnull Path sourcePath,
      @Nonnull ClassType classType) {

    if (!Files.exists(sourcePath)) {
      return Optional.empty();
    }

    SootClassNode classNode = new SootClassNode(analysisInputLocation);

    final String actualClassSignature;
    try {
      actualClassSignature = AsmUtil.initAsmClassSource(sourcePath, classNode);
    } catch (IOException exception) {
      logger.warn("ioe", exception);
      return Optional.empty();
    } catch (IllegalArgumentException exception) {
      logger.warn("iae", exception);
      return Optional.empty();
    }

    String requestedName = classType.getPackageName().getName();
    String requestedFQClassName =
        classType.getPackageName().getName()
            + (requestedName.isEmpty() ? "" : ".")
            + classType.getClassName();
    String actualFQClassName = actualClassSignature.replace('/', '.');
    if (!actualFQClassName.equals(requestedFQClassName)) {
      logger.warn(
          "The given Classtype '"
              + classType
              + "' did not match the found ClassType in the compilation unit '"
              + actualClassSignature
              + "'. Possibly the AnalysisInputLocation points to a subfolder already including the PackageName directory while the ClassType you wanted to retrieve is missing a PackageName.");
      return Optional.empty();
    }

    JavaClassType klassType = (JavaClassType) classType;

    if (klassType instanceof ModuleJavaClassType
        && klassType.getClassName().equals(JavaModuleIdentifierFactory.MODULE_INFO_FILE)) {
      logger.warn("Can not create ClassSource from a module info descriptor! path:" + sourcePath);
      return Optional.empty();
    } else {
      if (klassType instanceof AnnotationType) {
        return Optional.of(
            new AsmAnnotationClassSource(analysisInputLocation, sourcePath, klassType, classNode));
      }

      return Optional.of(
          new AsmClassSource(analysisInputLocation, sourcePath, klassType, classNode));
    }
  }

  @Override
  @Nonnull
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  class SootClassNode extends ClassNode {

    private final AnalysisInputLocation analysisInputLocation;

    SootClassNode(AnalysisInputLocation analysisInputLocation) {
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
              analysisInputLocation.getBodyInterceptors());
      methods.add(mn);
      return mn;
    }
  }
}
