package sootup.java.core;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.views.View;
import sootup.java.core.signatures.ModuleSignature;

/**
 * @author Markus Schmidt
 *     <p>Interface to mark AnalysisInputLocations that are capable of retreiving
 *     JavaModuleInformations
 */
public interface ModuleInfoAnalysisInputLocation extends AnalysisInputLocation<JavaSootClass> {

  Collection<? extends AbstractClassSource<JavaSootClass>> getModulesClassSources(
      @Nonnull ModuleSignature moduleSignature, @Nonnull View<?> view);

  @Nonnull
  Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View<?> view);

  @Nonnull
  Set<ModuleSignature> getModules(View<?> view);
}
