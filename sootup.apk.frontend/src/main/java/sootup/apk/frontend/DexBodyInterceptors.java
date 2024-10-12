package sootup.apk.frontend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.apk.frontend.interceptors.DexNullTransformer;
import sootup.apk.frontend.interceptors.DexNumberTranformer;
import sootup.core.transform.BodyInterceptor;

public enum DexBodyInterceptors {
  Default(new DexNumberTranformer(), new DexNullTransformer());

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  DexBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @Nonnull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
