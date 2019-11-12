package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import javax.annotation.Nonnull;

// Regarding the html-links in the Javadoc:
// We cannot use javadoc links directly if we would like to avoid javadoc errors:
// The referenced module is unknown from within here. Therefore we use html links
// which are relative to the documentation root

/**
 * Options that are passed through to the respective frontend. These define how the frontend should
 * behave while loading classes.
 *
 * <p>Besides being able to create your own by implementing this interface, each frontend has
 * built-in sets of options such as <a href="../../../../../../de/upb/swt/soot/java/bytecode/inputlocation/BytecodeClassLoadingOptions.html">java.bytecode.inputlocation.BytecodeClassLoadingOptions</a>
 * and <a href="../../../../../../de/upb/swt/soot/java/sourcecode/inputlocation/SourcecodeClassLoadingOptions.html">java.sourcecode.inputlocation.SourcecodeClassLoadingOptions</a>
 *
 * @author Christian Brüggemann
 */
public interface ClassLoadingOptions {

  /**
   * The interceptors are executed in order on each loaded method body, allowing it to be inspected
   * and manipulated.
   */
  @Nonnull
  List<BodyInterceptor> getBodyInterceptors();
}
