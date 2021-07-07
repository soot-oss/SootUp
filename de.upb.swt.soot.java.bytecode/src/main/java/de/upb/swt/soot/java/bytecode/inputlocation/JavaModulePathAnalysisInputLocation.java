package de.upb.swt.soot.java.bytecode.inputlocation;
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.ModuleInfoAnalysisInputLocation;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

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

  /**
   * Creates a {@link JavaModulePathAnalysisInputLocation} which locates classes in the given module
   * path.
   *
   * @param modulePath The class path to search in The {@link ClassProvider} for generating {@link
   *     SootClassSource}es for the files found on the class path
   */
  public JavaModulePathAnalysisInputLocation(@Nonnull String modulePath) {
    this(modulePath, FileSystems.getDefault());
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
      @Nonnull String modulePath, @Nonnull FileSystem fileSystem) {
    moduleFinder = new ModuleFinder(modulePath, fileSystem);
  }

  @Nonnull
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View<?> view) {
    return moduleFinder.getModuleInfo(sig);
  }

  @Nonnull
  public Set<ModuleSignature> getModules(View<?> view) {
    return moduleFinder.getModules();
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory, @Nonnull View<?> view) {
    Preconditions.checkArgument(
        identifierFactory instanceof JavaModuleIdentifierFactory,
        "Factory must be a JavaModuleSignatureFactory");

    Collection<ModuleSignature> allModules = moduleFinder.getAllModules();
    return allModules.stream()
        .flatMap(
            sig ->
                getClassSourcesInternal(sig, (JavaModuleIdentifierFactory) identifierFactory, view))
        .collect(Collectors.toList());
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull View<?> view) {
    Preconditions.checkArgument(
        identifierFactory instanceof JavaModuleIdentifierFactory,
        "Factory must be a JavaModuleSignatureFactory");
    return getClassSourcesInternal(
            moduleSignature, (JavaModuleIdentifierFactory) identifierFactory, view)
        .collect(Collectors.toList());
  }

  protected Stream<? extends AbstractClassSource<JavaSootClass>> getClassSourcesInternal(
      @Nonnull ModuleSignature moduleSignature,
      @Nonnull JavaModuleIdentifierFactory identifierFactory,
      @Nonnull View<?> view) {

    AnalysisInputLocation<JavaSootClass> inputLocation = moduleFinder.getModule(moduleSignature);
    if (inputLocation == null) {
      return Stream.empty();
    }

    if (!(inputLocation instanceof JavaModulePathAnalysisInputLocation)) {
      /*
       * we need a wrapper to create correct types for the found classes, all other ignore modules by default, or have
       * no clue about modules.
       */
      identifierFactory = JavaModuleIdentifierFactory.getInstance(moduleSignature);
    }

    return inputLocation.getClassSources(identifierFactory, view).stream();
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull View<?> view) {
    JavaClassType klassType = (JavaClassType) classType;

    ModuleSignature modulename =
        ((ModulePackageName) klassType.getPackageName()).getModuleSignature();
    // get inputlocation from cache
    AnalysisInputLocation<JavaSootClass> inputLocation = moduleFinder.getModule(modulename);

    if (inputLocation == null) {
      return Optional.empty();
    }

    return inputLocation.getClassSource(klassType, view);
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
