package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import sootup.core.frontend.SootClassSource;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.signatures.ModuleSignature;

/**
 * This AnalysisInputLocation models MultiRelease Jars or Directories if path points to a directory
 * that is not packed into a jar see https://openjdk.org/jeps/238#Modular_multi-release_JAR_files
 */
public class ModuleMultiReleaseJarAnalysisInputLocation extends MultiReleaseJarAnalysisInputLocation
    implements ModuleInfoAnalysisInputLocation {
  public ModuleMultiReleaseJarAnalysisInputLocation(
      @Nonnull Path path, @Nonnull SourceType srcType, int version) {
    super(path, srcType, version);

    /*



       // only versions >= 9 support java modules
       if (availableVersions[i] > 8) {
         moduleInfoMap.put(availableVersions[i], new HashMap<>());
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(versionRoot)) {
           for (Path entry : stream) {

             Path mi = path.resolve(moduleInfoFilename);

             if (Files.exists(mi)) {
               JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
               ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
               JavaModulePathAnalysisInputLocation inputLocation =
                   new JavaModulePathAnalysisInputLocation(
                       versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

               inputLocations.get(availableVersions[i]).add(inputLocation);
               moduleInfoMap.get(availableVersions[i]).put(moduleSignature, moduleInfo);
             }

             if (Files.isDirectory(entry)) {
               mi = versionRoot.resolve(moduleInfoFilename);

               if (Files.exists(mi)) {
                 JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
                 ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
                 JavaModulePathAnalysisInputLocation inputLocation =
                     new JavaModulePathAnalysisInputLocation(
                         versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

                 inputLocations.get(availableVersions[i]).add(inputLocation);
                 moduleInfoMap.get(availableVersions[i]).put(moduleSignature, moduleInfo);
               }
               // else TODO [bh] can we have automatic modules here?
             }
           }
         } catch (IOException e) {
           e.printStackTrace();
         }
       }


    */

    throw new UnsupportedOperationException("not fully implemented, yet!");
  }

  @Override
  protected ModuleInfoAnalysisInputLocation createAnalysisInputLocation(
      @Nonnull Path path, SourceType sourceType, List<BodyInterceptor> bodyInterceptors) {
    try {
      return new JavaModulePathAnalysisInputLocation(
          path, fileSystemCache.get(this.path), sourceType, bodyInterceptors);
    } catch (ExecutionException e) {
      throw new IllegalArgumentException("Could not open filesystemcache.", e);
    }
  }

  @Override
  public Collection<? extends SootClassSource> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View view) {
    // TODO: check if we need to combine modules as well or if only versioned .class files are
    return ((JavaModulePathAnalysisInputLocation) inputLocations.get(DEFAULT_VERSION))
        .getModulesClassSources(moduleSignature, view);
  }

  @Nonnull
  @Override
  public Optional<JavaModuleInfo> getModuleInfo(@Nonnull ModuleSignature sig, @Nonnull View view) {
    // TODO: check if we need to combine modules as well or if only versioned .class files are
    // allowed
    return ((JavaModulePathAnalysisInputLocation) inputLocations.get(DEFAULT_VERSION))
        .getModuleInfo(sig, view);
  }

  @Nonnull
  @Override
  public Set<ModuleSignature> getModules(@Nonnull View view) {
    // TODO: check if we need to combine modules as well or if only versioned .class files are
    // allowed
    return ((JavaModulePathAnalysisInputLocation) inputLocations.get(DEFAULT_VERSION))
        .getModules(view);
  }
}
