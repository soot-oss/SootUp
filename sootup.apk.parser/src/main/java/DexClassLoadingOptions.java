import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.transform.BodyInterceptor;

public enum DexClassLoadingOptions implements ClassLoadingOptions {
  Default {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
      return DexBodyInterceptors.Default.bodyInterceptors();
    }
  }
}
