package sootup.java.core;

import javax.annotation.Nonnull;
import sootup.core.Language;

/**
 * @author Markus Schmidt
 *     <p>Interface to mark AnalysisInputLocations that are capable of retreiving
 *     JavaModuleInformations
 */
public interface MultiReleaseModuleInfoAnalysisInputLocation
    extends ModuleInfoAnalysisInputLocation {

  @Nonnull
  Language getLanguage();
}
