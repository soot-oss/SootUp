package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.conversion.AsmJavaClassProvider;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.types.JavaClassType;

public class ClassFileBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

  @NonNull private final String omittedPackageName;

  public ClassFileBasedAnalysisInputLocation(
      @NonNull Path classFilePath,
      @NonNull String omittedPackageName,
      @NonNull SourceType srcType) {
    this(
        classFilePath,
        omittedPackageName,
        srcType,
        BytecodeBodyInterceptors.Default.getBodyInterceptors());
  }

  public ClassFileBasedAnalysisInputLocation(
      @NonNull Path classFilePath,
      @NonNull String omittedPackageName,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    super(classFilePath, srcType, bodyInterceptors);
    this.omittedPackageName = omittedPackageName;

    if (!Files.isRegularFile(classFilePath)) {
      throw new IllegalArgumentException("Needs to point to a regular file!");
    }

    if (Files.isDirectory(classFilePath)) {
      throw new IllegalArgumentException("Needs to point to a regular file - not to a directory.");
    }
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {

    if (!type.getPackageName().getName().startsWith(omittedPackageName)) {
      return Optional.empty();
    }

    return getSingleClass((JavaClassType) type, path, new AsmJavaClassProvider(view));
  }

  @NonNull
  @Override
  public Stream<JavaSootClassSource> getClassSources(@NonNull View view) {
    AsmJavaClassProvider classProvider = new AsmJavaClassProvider(view);
    IdentifierFactory factory = view.getIdentifierFactory();
    Path dirPath = this.path.getParent();

    final String fullyQualifiedName = fromPath(dirPath, path);

    Optional<JavaSootClassSource> javaSootClassSource =
        classProvider
            .createClassSource(this, path, factory.getClassType(fullyQualifiedName))
            .map(src -> (JavaSootClassSource) src);

    return Stream.of(javaSootClassSource.get());
  }

  @NonNull
  protected String fromPath(@NonNull Path baseDirPath, Path packageNamePathAndClass) {
    String str =
        FilenameUtils.removeExtension(
            packageNamePathAndClass
                .subpath(baseDirPath.getNameCount(), packageNamePathAndClass.getNameCount())
                .toString()
                .replace(packageNamePathAndClass.getFileSystem().getSeparator(), "."));

    return omittedPackageName.isEmpty() ? str : omittedPackageName + "." + str;
  }
}
