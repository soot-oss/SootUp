package sootup.jimple.frontend;

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class EagerClassVisitor extends ClassVisitor {

  public EagerClassVisitor(
      @NonNull Path path, @NonNull List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    super(path, bodyInterceptors, view);
  }

  @Override
  protected MethodVisitor createMethodVisitor() {
    return new EagerMethodVisitor(this);
  }

  @Override
  protected JavaSootMethod processMethod(JavaSootMethod m) {
    if (!m.isConcrete()) {
      return m;
    }
    Body.BodyBuilder bodyBuilder = Body.builder(m.getBody(), m.getModifiers());
    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      try {
        bodyInterceptor.interceptBody(bodyBuilder, view);
        bodyBuilder.getControlFlowGraph().validateStmtConnectionsInGraph();
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to apply " + bodyInterceptor + " to " + m.getSignature(), e);
      }
    }
    Body modifiedBody = bodyBuilder.build();
    return new JavaSootMethod(
        new OverridingBodySource(m.getBodySource()).withBody(modifiedBody),
        m.getSignature(),
        m.getModifiers(),
        m.getExceptionSignatures(),
        m.getPosition());
  }
}
