import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.transform.BodyInterceptor;

public enum DexClassLoadingOptions {
  Default {
    @Nonnull
    public List<BodyInterceptor> getBodyInterceptors() {
      return DexBodyInterceptors.Default.bodyInterceptors();
    }
  }
}
