package de.upb.sse.sootup.java.core;

import de.upb.sse.sootup.core.frontend.AbstractClassSource;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.views.View;
import de.upb.sse.sootup.java.core.signatures.ModuleSignature;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;

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
