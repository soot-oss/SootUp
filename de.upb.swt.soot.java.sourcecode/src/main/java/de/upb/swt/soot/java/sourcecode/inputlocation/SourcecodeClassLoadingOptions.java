package de.upb.swt.soot.java.sourcecode.inputlocation;

import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public enum SourcecodeClassLoadingOptions implements ClassLoadingOptions {
  Default {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
      return Collections.emptyList();
    }
  }
}
