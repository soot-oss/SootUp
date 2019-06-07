package de.upb.soot.frontends;

import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.MethodSignature;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows for replacing specific parts of a method. By default, it delegates to the {@link
 * MethodSource} delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveBody(SootMethod)}, simply call
 * {@link #withBody(Body)} to obtain a new {@link OverridingMethodSource}. The new instance will
 * then use the supplied value instead of calling {@link #resolveBody(SootMethod)} on the delegate.
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
  public Body resolveBody(@Nonnull SootMethod m) throws ResolveException {
    return overriddenBody ? body : delegate.resolveBody(m);
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
}
