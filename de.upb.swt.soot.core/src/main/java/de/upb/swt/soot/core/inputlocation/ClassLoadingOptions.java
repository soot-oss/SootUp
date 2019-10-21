package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClassLoadingOptions {
  @Nullable private final List<BodyInterceptor> customBodyInterceptors;

  public ClassLoadingOptions(@Nullable List<BodyInterceptor> customBodyInterceptors) {
    this.customBodyInterceptors = customBodyInterceptors;
  }

  public ClassLoadingOptions(@Nonnull BodyInterceptors bodyInterceptors) {
    this.customBodyInterceptors = bodyInterceptors.bodyInterceptors();
  }

  @Nullable
  public List<BodyInterceptor> getCustomBodyInterceptors() {
    return customBodyInterceptors;
  }

  public interface BodyInterceptors {
    @Nonnull
    List<BodyInterceptor> bodyInterceptors();
  }
}
