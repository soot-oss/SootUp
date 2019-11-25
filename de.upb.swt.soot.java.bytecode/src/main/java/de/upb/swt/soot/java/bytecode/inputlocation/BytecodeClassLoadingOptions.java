package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Built-in sets of {@link ClassLoadingOptions} for the bytecode frontend.
 *
 * @author Christian Brüggemann
 */
public enum BytecodeClassLoadingOptions implements ClassLoadingOptions {
  Default {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
      return BytecodeBodyInterceptors.Default.bodyInterceptors();
    }
  }
}
