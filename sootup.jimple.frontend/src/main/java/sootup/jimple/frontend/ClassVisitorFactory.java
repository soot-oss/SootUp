package sootup.jimple.frontend;

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

@FunctionalInterface
public interface ClassVisitorFactory {
  @NonNull ClassVisitor create(
      @NonNull Path path, @NonNull List<BodyInterceptor> bodyInterceptors, @NonNull View view);
}
