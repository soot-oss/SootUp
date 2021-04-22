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
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.JavaModuleAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaModuleIdentifierFactory;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.signatures.ModulePackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;

/**
 * An implementation of the {@link AnalysisInputLocation} interface for the Java modulepath. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathAnalysisInputLocation
    implements BytecodeAnalysisInputLocation, JavaModuleAnalysisInputLocation {

  @Nonnull private final ModuleFinder moduleFinder;

  /**
   * Creates a {@link JavaModulePathAnalysisInputLocation} which locates classes in the given module
   * path.
   *
   * @param modulePath The class path to search in The {@link ClassProvider} for generating {@link
   *     SootClassSource}es for the files found on the class path
   */
  public JavaModulePathAnalysisInputLocation(@Nonnull String modulePath) {
    moduleFinder = new ModuleFinder(modulePath);
  }

  @Nonnull
  public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig) {
    return moduleFinder.getModuleInfo(sig);
  }

  @Override
  @Nonnull
  public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull ClassLoadingOptions classLoadingOptions) {
    Preconditions.checkArgument(
        identifierFactory instanceof JavaModuleIdentifierFactory,
        "Factory must be a ModuleSignatureFactory");

    // new AsmJavaClassProvider(classLoadingOptions.getBodyInterceptors())
    Set<AbstractClassSource<JavaSootClass>> found = new HashSet<>();
    for (ModuleSignature module : moduleFinder.discoverAllModules()) {
      AnalysisInputLocation<JavaSootClass> inputLocation = moduleFinder.discoverModule(module);
      IdentifierFactory identifierFactoryWrapper = identifierFactory;
      if (inputLocation == null) {
        continue;
      }
      if (!(inputLocation instanceof JrtFileSystemAnalysisInputLocation)) {
        /*
         * we need a wrapper to create correct types for the found classes, all other ignore modules by default, or have
         * no clue about modules.
         */
        identifierFactoryWrapper = new IdentifierFactoryWrapper(identifierFactoryWrapper, module);
      }
      found.addAll(inputLocation.getClassSources(identifierFactoryWrapper));
    }

    return found;
  }

  @Override
  @Nonnull
  public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
      @Nonnull ClassType classType, @Nonnull ClassLoadingOptions classLoadingOptions) {
    JavaClassType klassType = (JavaClassType) classType;

    ModuleSignature modulename =
        ((ModulePackageName) klassType.getPackageName()).getModuleSignature();
    // lookup the ns for the class provider from the cache
    AnalysisInputLocation<JavaSootClass> inputLocation = moduleFinder.discoverModule(modulename);

    if (inputLocation == null) {
      return Optional.empty();
    }
    return inputLocation.getClassSource(klassType);
  }

  private static class IdentifierFactoryWrapper extends JavaModuleIdentifierFactory {

    private final IdentifierFactory factory;
    private final ModuleSignature moduleSignature;

    private IdentifierFactoryWrapper(IdentifierFactory factory, ModuleSignature moduleSignature) {
      this.factory = factory;
      this.moduleSignature = moduleSignature;
    }

    @Override
    @Nonnull
    public JavaClassType fromPath(@Nonnull Path file) {
      if (factory instanceof JavaModuleIdentifierFactory) {
        JavaModuleIdentifierFactory moduleSignatureFactory = (JavaModuleIdentifierFactory) factory;
        String fullyQualifiedName =
            FilenameUtils.removeExtension(file.toString()).replace('/', '.');
        String packageName = "";
        int index = fullyQualifiedName.lastIndexOf(".");
        String className = fullyQualifiedName;
        if (index > 0) {
          className = fullyQualifiedName.substring(index);
          packageName = fullyQualifiedName.substring(0, index);
        }
        return moduleSignatureFactory.getClassType(
            className, packageName, moduleSignature.getModuleName());
      }
      return (JavaClassType) factory.fromPath(file);
    }
  }
}
