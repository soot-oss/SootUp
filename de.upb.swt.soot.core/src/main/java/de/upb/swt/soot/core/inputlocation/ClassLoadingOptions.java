package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import javax.annotation.Nonnull;

public interface ClassLoadingOptions {

  @Nonnull
  List<BodyInterceptor> getBodyInterceptors();

  interface BodyInterceptors {
    @Nonnull
    List<BodyInterceptor> bodyInterceptors();
  }
}
