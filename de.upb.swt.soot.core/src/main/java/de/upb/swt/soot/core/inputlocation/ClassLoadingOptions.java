package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Options that are passed through to the respective frontend. These define how the frontend should
 * behave while loading classes.
 *
 * <p>Besides being able to create your own by implementing this interface, each frontend has
 * built-in sets of options such as {@code java.bytecode.inputlocation.BytecodeClassLoadingOptions}
 * and {@code java.sourcecode.inputlocation.SourcecodeClassLoadingOptions}
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
