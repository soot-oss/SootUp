package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions.BodyInterceptors;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public enum BytecodeBodyInterceptors implements BodyInterceptors {
  Default(new CastAndReturnInliner());

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  BytecodeBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @Override
  @Nonnull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
