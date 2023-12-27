import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.List;

public class DexClassLoadingOptions implements ClassLoadingOptions {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
        return null;
    }
}
