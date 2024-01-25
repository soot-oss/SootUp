import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.transform.BodyInterceptor;
import sootup.java.core.interceptors.*;
import sootup.java.core.interceptors.Dex.DexNullTransformer;
import sootup.java.core.interceptors.Dex.DexNumberTranformer;
import sootup.java.core.interceptors.Dex.DexTrapStackTransformer;

public enum DexBodyInterceptors {
  Default(
      new NopEliminator(),
      new CastAndReturnInliner(),
      new UnreachableCodeEliminator(),
      /*new LocalSplitter(),*/
      /*new Aggregator()*/
      /*new CopyPropagator(),*/
      /*new DeadAssignmentEliminator()*/
//      new UnusedLocalEliminator(),
//      new ConditionalBranchFolder(),
//      new EmptySwitchEliminator(),
//      new LocalNameStandardizer()
//      new DexTrapStackTransformer(),
      new DexNumberTranformer(),
      new DexNullTransformer());

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  DexBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @Nonnull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
