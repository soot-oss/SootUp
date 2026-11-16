package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.Body;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/** The bodies of dex methods are converted when they are requested, not with their class. */
public class DexLazyBodyTest {

  /** A body interceptor that does nothing; spied on to count the converted bodies. */
  public static class NoOpInterceptor implements BodyInterceptor {
    @Override
    public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {}
  }

  private final NoOpInterceptor interceptor = Mockito.spy(new NoOpInterceptor());

  private JavaSootClass cryptoClass() {
    Path apkPath = Paths.get("src/test/resources/Crypto.apk");
    List<BodyInterceptor> interceptors =
        new ArrayList<>(DexBodyInterceptors.Default.bodyInterceptors());
    interceptors.add(interceptor);
    JavaView view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apkPath, new AndroidVersionInfo(apkPath, ""), interceptors));
    return view.getClass(view.getIdentifierFactory().getClassType("com.example.CryptoWeakness"))
        .get();
  }

  @Test
  public void methodsAreListedWithoutConvertingTheirBodies() {
    Set<JavaSootMethod> methods = cryptoClass().getMethods();
    assertTrue(methods.stream().filter(JavaSootMethod::isConcrete).count() > 1);

    Mockito.verify(interceptor, Mockito.never())
        .interceptBody(Mockito.any(Body.BodyBuilder.class), Mockito.any(View.class));
  }

  @Test
  public void bodyIsConvertedOnceWhenRequested() {
    JavaSootMethod method =
        cryptoClass().getMethods().stream()
            .filter(m -> m.getName().equals("generateKeyWithWeakRandom"))
            .findFirst()
            .get();

    Body body = method.getBody();
    assertFalse(body.getStmts().isEmpty());
    assertSame(body, method.getBody());
    Mockito.verify(interceptor, Mockito.times(1))
        .interceptBody(Mockito.any(Body.BodyBuilder.class), Mockito.any(View.class));
  }
}
