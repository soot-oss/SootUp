package sootup.java.bytecode.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Christian Brüggemann and others
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
import com.google.common.base.Preconditions;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;

/**
 * An implementation of the {@link AnalysisInputLocation} interface for the Java modulepath. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathAnalysisInputLocation implements ModuleInfoAnalysisInputLocation {

  @Nonnull private final ModuleFinder moduleFinder;
  @Nonnull private final SourceType sourcetype;
  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  /**
   * Creates a {@link JavaModulePathAnalysisInputLocation} which locates classes in the given module
   * path.
   *
   * @param modulePath The class path to search in The {@link ClassProvider} for generating {@link
   *     SootClassSource}es for the files found on the class path
   */
  public JavaModulePathAnalysisInputLocation(@Nonnull String modulePath) {
    this(modulePath, SourceType.Application);
  }

  public JavaModulePathAnalysisInputLocation(
      @Nonnull String modulePath, @Nonnull SourceType sourcetype) {
    this(modulePath, FileSystems.getDefault(), sourcetype);
  }

  public JavaModulePathAnalysisInputLocation(
      @Nonnull String modulePath, @Nonnull FileSystem fileSystem, @Nonnull SourceType sourcetype) {
    this(modulePath, fileSystem, sourcetype, new ArrayList<>());
  }

  /**
   * Creates a {@link JavaModulePathAnalysisInputLocation} which locates classes in the given module
   * path.
   *
   * @param modulePath The class path to search in The {@link ClassProvider} for generating {@link
   *     SootClassSource}es for the files found on the class path
   * @param fileSystem filesystem for the path
   */
  public JavaModulePathAnalysisInputLocation(
      @Nonnull String modulePath,
      @Nonnull FileSystem fileSystem,
      @Nonnull SourceType sourcetype,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.sourcetype = sourcetype;
    this.bodyInterceptors = bodyInterceptors;
    moduleFinder = new ModuleFinder(modulePath, fileSystem, sourcetype);
  }

  @Nonnull
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View view) {
    return moduleFinder.getModuleInfo(sig);
  }

  @Nonnull
  public Set<ModuleSignature> getModules(View view) {
    return moduleFinder.getModules();
  }

  @Override
  @Nonnull
  public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    Preconditions.checkArgument(
        identifierFactory instanceof JavaModuleIdentifierFactory,
        "Factory must be a JavaModuleSignatureFactory");

    Collection<ModuleSignature> allModules = moduleFinder.getAllModules();
    return allModules.stream()
        .flatMap(sig -> getClassSourcesInternal(sig, view))
        .map(src -> (JavaSootClassSource) src)
        .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public SourceType getSourceType() {
    return sourcetype;
  }

  @Override
  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  @Override
  @Nonnull
  public Collection<JavaSootClassSource> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    Preconditions.checkArgument(
        identifierFactory instanceof JavaModuleIdentifierFactory,
        "Factory must be a JavaModuleSignatureFactory");
    return getClassSourcesInternal(moduleSignature, view).collect(Collectors.toList());
  }

  protected Stream<JavaSootClassSource> getClassSourcesInternal(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View view) {

    AnalysisInputLocation inputLocation = moduleFinder.getModule(moduleSignature);
    if (inputLocation == null) {
      return Stream.empty();
    }

    return inputLocation.getClassSources(view).stream().map(src -> (JavaSootClassSource) src);
  }

  @Override
  @Nonnull
  public Optional<JavaSootClassSource> getClassSource(
      @Nonnull ClassType classType, @Nonnull View view) {
    JavaClassType klassType = (JavaClassType) classType;

    ModuleSignature modulename =
        ((ModulePackageName) klassType.getPackageName()).getModuleSignature();
    // get inputlocation from cache
    AnalysisInputLocation inputLocation = moduleFinder.getModule(modulename);

    if (inputLocation == null) {
      return Optional.empty();
    }

    return inputLocation.getClassSource(klassType, view).map(src -> (JavaSootClassSource) src);
  }

  @Override
  public int hashCode() {
    return moduleFinder.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JavaModulePathAnalysisInputLocation)) {
      return false;
    }
    return moduleFinder.equals(((JavaModulePathAnalysisInputLocation) o).moduleFinder);
  }
}
