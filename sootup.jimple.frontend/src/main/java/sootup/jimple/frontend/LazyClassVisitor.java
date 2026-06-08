package sootup.jimple.frontend;

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class LazyClassVisitor extends ClassVisitor {

  public LazyClassVisitor(
      @NonNull Path path, @NonNull List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    super(path, bodyInterceptors, view);
  }

  @Override
  protected MethodVisitor createMethodVisitor() {
    return new LazyMethodVisitor(this);
  }

  @Override
  protected JavaSootMethod processMethod(JavaSootMethod m) {
    return m;
  }
}
