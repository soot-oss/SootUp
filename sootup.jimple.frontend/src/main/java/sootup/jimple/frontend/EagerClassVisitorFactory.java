package sootup.jimple.frontend;

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class EagerClassVisitorFactory implements ClassVisitorFactory {
  @Override
  public ClassVisitor create(
      @NonNull Path path, @NonNull List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    return new EagerClassVisitor(path, bodyInterceptors, view);
  }
}
