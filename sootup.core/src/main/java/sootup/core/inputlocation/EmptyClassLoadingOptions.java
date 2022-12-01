package sootup.core.inputlocation;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.transform.BodyInterceptor;

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
