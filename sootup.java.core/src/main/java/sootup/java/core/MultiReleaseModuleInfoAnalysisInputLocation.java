package sootup.java.core;

import sootup.core.Language;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.views.View;
import sootup.java.core.signatures.ModuleSignature;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author Markus Schmidt
 *     <p>Interface to mark AnalysisInputLocations that are capable of retreiving
 *     JavaModuleInformations
 */
public interface MultiReleaseModuleInfoAnalysisInputLocation extends ModuleInfoAnalysisInputLocation {

  @Nonnull
  Language getLanguage();
}
