package de.upb.swt.soot.core.frontend;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows for replacing specific parts of a method. By default, it delegates to the {@link
 * MethodSource} delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveBody()}, simply call {@link
 * #withBody(Body)} to obtain a new {@link OverridingMethodSource}. The new instance will then use
 * the supplied value instead of calling {@link #resolveBody()} on the delegate.
 */
public class OverridingMethodSource implements MethodSource {

  @Nonnull private final MethodSource delegate;

  // Since resolveBody may return null, we cannot use `null` here to indicate that `body`
  // is not overridden.
  private final boolean overriddenBody;
  @Nullable private final Body body;

  public OverridingMethodSource(@Nonnull MethodSource delegate) {
    this.delegate = delegate;
    overriddenBody = false;
    body = null;
  }

  private OverridingMethodSource(
      @Nonnull MethodSource delegate, boolean overriddenBody, @Nullable Body body) {
    this.delegate = delegate;
    this.overriddenBody = overriddenBody;
    this.body = body;
  }

  @Nullable
  @Override
  public Body resolveBody() throws ResolveException {
    return overriddenBody ? body : delegate.resolveBody();
  }

  @Nonnull
  @Override
  public MethodSignature getSignature() {
    return delegate.getSignature();
  }

  @Nonnull
  public OverridingMethodSource withBody(@Nullable Body body) {
    return new OverridingMethodSource(delegate, true, body);
  }

  /**
   * Creates a new {@link OverridingMethodSource} that replaces the statements of the method's body.
   * If the body is resolved as null, this method throws {@link IllegalStateException}.
   */
  @Nonnull
  public OverridingMethodSource withBodyStmts(@Nonnull Consumer<List<Stmt>> stmtModifier) {
    Body body = resolveBody();
    if (body == null) {
      throw new IllegalStateException(
          "Cannot replace statements in method " + delegate.getSignature() + ", body is null");
    }

    List<Stmt> newStmts = new ArrayList<>(body.getStmts());
    stmtModifier.accept(newStmts);
    return withBody(body.withStmts(newStmts));
  }
}
