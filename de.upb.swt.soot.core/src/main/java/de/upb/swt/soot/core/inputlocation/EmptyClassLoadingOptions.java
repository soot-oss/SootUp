package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** @author Markus Schmidt */
public enum EmptyClassLoadingOptions implements ClassLoadingOptions {
  Default {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
      return Collections.emptyList();
    }
  }
}
