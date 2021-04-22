package de.upb.swt.soot.java.core;

import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * @author Markus Schmidt
 *     <p>Interface to mark AnalysisInputLocations that are capable of retreiving
 *     JavaModuleInformations
 */
public interface ModuleInfoAnalysisInputLocation {
  @Nonnull
  Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig);
}
