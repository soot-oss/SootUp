package sootup.apk.parser.dexpler;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.Method;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.util.Modifiers;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class DexMethodSource implements BodySource {

  private final Set<Local> locals;
  private final MutableStmtGraph mutableStmtGraph;
  private final Method method;

  private final List<BodyInterceptor> bodyInterceptors;

  @Nonnull private final View view;
  private final MethodSignature methodSignature;

  public DexMethodSource(
      Set<Local> locals,
      MethodSignature methodSignature,
      MutableStmtGraph mutableStmtGraph,
      Method method,
      List<BodyInterceptor> bodyInterceptors,
      @Nonnull View view) {
    this.methodSignature = methodSignature;
    this.view = view;
    this.locals = locals;
    this.bodyInterceptors = bodyInterceptors;
    this.mutableStmtGraph = mutableStmtGraph;
    this.method = method;
  }

  @Nonnull
  @Override
  public Body resolveBody(@Nonnull Iterable<MethodModifier> modifiers)
      throws ResolveException, IOException {
    Set<MethodModifier> modifiersSet =
        StreamSupport.stream(modifiers.spliterator(), false).collect(Collectors.toSet());
    Body.BodyBuilder bodyBuilder =
        Body.builder(mutableStmtGraph)
            .setModifiers(modifiersSet)
            .setMethodSignature(getSignature())
            .setPosition(NoPositionInformation.getInstance())
            .setLocals(locals);
    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      bodyInterceptor.interceptBody(bodyBuilder, view);
    }
    return bodyBuilder.build();
  }

  public JavaSootMethod makeSootMethod() {
    JavaSootMethod sootMethod;
    EnumSet<MethodModifier> methodModifiers = Modifiers.getMethodModifiers(method.getAccessFlags());
    try {
      sootMethod =
          new JavaSootMethod(
              new OverridingBodySource(getSignature(), resolveBody(methodModifiers)),
              getSignature(),
              methodModifiers,
              Collections.emptyList(),
              Collections.emptySet(),
              NoPositionInformation.getInstance());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sootMethod;
  }

  @Override
  public Object resolveAnnotationsDefaultValue() {
    throw new UnsupportedOperationException("TODO");
  }

  @Nonnull
  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }
}
