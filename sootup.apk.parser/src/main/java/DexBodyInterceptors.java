import interceptors.DexNullTransformer;
import interceptors.DexNumberTranformer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
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
