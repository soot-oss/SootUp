package de.upb.swt.soot.core.frontend;

import com.google.common.graph.ImmutableGraph;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.function.Function;
import javax.annotation.Nonnull;

/** @author Hasitha Rajapakse */

/**
 * Allows for replacing specific parts of a method or, resolve methods where all information is
 * already existing.
 *
 * <p>When replacing specific parts of a method by default, it delegates to the {@link MethodSource}
 * delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveBody()}, simply call {@link
 * #withBody(Body)} to obtain a new {@link OverridingMethodSource}. The new instance will then use
 * the supplied value instead of calling {@link #resolveBody()} on the delegate.
 */
public class OverridingMethodSource implements MethodSource {

  private final MethodSource delegate;
  @Nonnull private final Body body;

  private final MethodSignature methodSignature;

  public OverridingMethodSource(@Nonnull MethodSource delegate) {
    this.delegate = delegate;
    body = null;
    this.methodSignature = null;
  }

  private OverridingMethodSource(
      @Nonnull MethodSource delegate, boolean overriddenBody, @Nonnull Body body) {
    this.delegate = delegate;
    this.body = body;
    this.methodSignature = null;
  }

  /** Method source where all information already available */
  public OverridingMethodSource(MethodSignature methodSignature, Body body) {
    this.delegate = null;
    this.body = body;
    this.methodSignature = methodSignature;
  }

  @Nonnull
  @Override
  public Body resolveBody() throws ResolveException {
    return body != null ? body : delegate.resolveBody();
  }

  @Nonnull
  @Override
  public MethodSignature getSignature() {
    return methodSignature != null ? methodSignature : delegate.getSignature();
  }

  @Nonnull
  public OverridingMethodSource withBody(@Nonnull Body body) {
    return new OverridingMethodSource(delegate, true, body);
  }

  /**
   * Creates a new {@link OverridingMethodSource} that replaces the statements of the method's body.
   * If the body is resolved as null, this method throws {@link IllegalStateException}.
   */
  @Nonnull
  public OverridingMethodSource withBodyStmts(
      @Nonnull Function<ImmutableGraph<Stmt>, ImmutableGraph<Stmt>> stmtModifier) {
    Body body = resolveBody();
    if (body == Body.getNoBody()) {
      throw new IllegalStateException(
          "Cannot replace statements in method " + delegate.getSignature() + ", body is null");
    }

    return withBody(body.withStmts(stmtModifier.apply(body.getStmtGraph())));
  }
}
