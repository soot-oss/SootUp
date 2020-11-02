package de.upb.swt.soot.core.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Hasitha Rajapakse, Markus Schmidt, Christian Brüggemann
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  @Nullable private final MethodSource delegate;
  @Nullable private final Body body;

  private final MethodSignature methodSignature;

  public OverridingMethodSource(@Nonnull MethodSource delegate) {
    this.delegate = delegate;
    body = null;
    this.methodSignature = null;
  }

  private OverridingMethodSource(@Nonnull MethodSource delegate, @Nonnull Body body) {
    this.delegate = delegate;
    this.body = body;
    this.methodSignature = null;
  }

  /** Method source where all information already available */
  public OverridingMethodSource(@Nonnull MethodSignature methodSignature, @Nonnull Body body) {
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
    return new OverridingMethodSource(delegate, body);
  }
}
