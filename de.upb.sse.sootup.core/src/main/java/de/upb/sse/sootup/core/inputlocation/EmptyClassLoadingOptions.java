package de.upb.sse.sootup.core.inputlocation;

import de.upb.sse.sootup.core.transform.BodyInterceptor;
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
