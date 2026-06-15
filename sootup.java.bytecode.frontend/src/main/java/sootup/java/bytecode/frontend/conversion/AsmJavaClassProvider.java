package sootup.java.bytecode.frontend.conversion;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo, Markus Schmidt, Kadiray Karakaya and others
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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.PathbasedClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.types.ModuleJavaClassType;

/** A {@link PathbasedClassProvider} capable of handling Java bytecode */
public class AsmJavaClassProvider implements PathbasedClassProvider {

  @NonNull private final View view;
  private static final @NonNull Logger logger = LoggerFactory.getLogger(AsmJavaClassProvider.class);

  public AsmJavaClassProvider(@NonNull View view) {
    this.view = view;
  }

  public static SootClassSource createClassSource(
      @NonNull final AnalysisInputLocation analysisInputLocation,
      @NonNull final Path sourcePath,
      @NonNull final ClassType classType,
      @NonNull final ClassNode classNode) {
    if ((classNode.access & Opcodes.ACC_ANNOTATION) == Opcodes.ACC_ANNOTATION) {
      return new AsmAnnotationClassSource(analysisInputLocation, sourcePath, classType, classNode);
    }

    AsmClassSource asmClassSource =
        new AsmClassSource(analysisInputLocation, sourcePath, classType, classNode);

    /*
    // copy and load the complete class at once into memory so the newly created asmClassSource can
    // release the memory and structures from the asm library
    return new OverridingJavaClassSource(
        asmClassSource.getAnalysisInputLocation(),
        asmClassSource.getSourcePath(),
        asmClassSource.getClassType(),
        asmClassSource.resolveSuperclass().orElse(null),
        asmClassSource.resolveInterfaces(),
        asmClassSource.resolveOuterClass().orElse(null),
        asmClassSource.resolveFields(),
        asmClassSource.resolveMethods(),
        asmClassSource.resolvePosition(),
        asmClassSource.resolveModifiers(),
        asmClassSource.resolveAnnotations(),
        Collections.emptyList(), // TODO! implement
        Collections.emptyList());

     */
    return asmClassSource;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      @NonNull final AnalysisInputLocation analysisInputLocation,
      @NonNull final Path sourcePath,
      @NonNull final ClassType classType) {

    if (classType instanceof ModuleJavaClassType
        && classType.getClassName().equals(JavaModuleIdentifierFactory.MODULE_INFO_FILE)) {
      logger.warn("Can not create ClassSource from a module info descriptor! path:" + sourcePath);
      return Optional.empty();
    }
    final var classNode = new SootClassNode(analysisInputLocation);
    final var actualClassFQNOpt = classNode.readClassName(sourcePath);
    final var requestedClassFQN = classType.getFullyQualifiedName();

    if (actualClassFQNOpt.isEmpty()) {
      return Optional.empty();
    }

    if (!actualClassFQNOpt.get().equals(requestedClassFQN)) {
      logger.warn(
          "Class names do not match. Actual:{}, Requested:{}",
          actualClassFQNOpt.get(),
          requestedClassFQN);
      return Optional.empty();
    }

    return Optional.of(createClassSource(analysisInputLocation, sourcePath, classType, classNode));
  }

  @Override
  @NonNull
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }

  class SootClassNode extends ClassNode {

    private final AnalysisInputLocation analysisInputLocation;

    SootClassNode(AnalysisInputLocation analysisInputLocation) {
      super(AsmUtil.SUPPORTED_ASM_OPCODE);
      this.analysisInputLocation = analysisInputLocation;
    }

    /**
     * Initializes a class node.
     *
     * @param classSource The source.
     * @return the actual class signature found in the compilation unit
     */
    protected Optional<String> readClassName(@NonNull final Path classSource) {
      try (InputStream sourceFileInputStream = Files.newInputStream(classSource);
          BufferedInputStream bis = new BufferedInputStream(sourceFileInputStream)) {
        ClassReader classReader = new ClassReader(bis);
        classReader.accept(this, ClassReader.SKIP_FRAMES);
        return Optional.of(classReader.getClassName().replace('/', '.'));
      } catch (IOException exception) {
        logger.debug("Cannot create class source for {}", classSource, exception);
      } catch (IllegalArgumentException exception) {
        logger.debug("Cannot create class source for {}", classSource, exception);
      }
      return Optional.empty();
    }

    @Override
    @NonNull
    public MethodVisitor visitMethod(
        int access,
        @NonNull String name,
        @NonNull String desc,
        @NonNull String signature,
        @NonNull String[] exceptions) {

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
