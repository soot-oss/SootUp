package de.upb.swt.soot.java.core;

import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import java.util.Optional;
import javax.annotation.Nonnull;

public interface JavaModuleAnalysisInputLocation {
  @Nonnull
  Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig);
}
