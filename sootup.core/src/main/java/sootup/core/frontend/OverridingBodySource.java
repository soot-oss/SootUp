package sootup.core.frontend;
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
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;

/** @author Hasitha Rajapakse */

/**
 * Allows for replacing specific parts of a method or, resolve methods where all information is
 * already existing.
 *
 * <p>When replacing specific parts of a method by default, it delegates to the {@link BodySource}
 * delegate provided in the constructor.
 *
 * <p>To alter the results of invocations to e.g. {@link #resolveBody(Iterable)}, simply call {@link
 * #withBody(Body)} to obtain a new {@link OverridingBodySource}. The new instance will then use the
 * supplied value instead of calling {@link #resolveBody(Iterable)} on the delegate.
 */
public class OverridingBodySource implements BodySource {

  @Nullable private final BodySource delegate;
  @Nullable private final Body body;

  private final MethodSignature methodSignature;

  public OverridingBodySource(@Nonnull BodySource delegate) {
    this.delegate = delegate;
    body = null;
    this.methodSignature = null;
  }

  private OverridingBodySource(@Nonnull BodySource delegate, @Nonnull Body body) {
    this.delegate = delegate;
    this.body = body;
    this.methodSignature = null;
  }

  /** Method source where all information already available */
  public OverridingBodySource(@Nonnull MethodSignature methodSignature, @Nonnull Body body) {
    this.delegate = null;
    this.body = body;
    this.methodSignature = methodSignature;
  }

  @Nonnull
  @Override
  public Body resolveBody(@Nonnull Iterable<MethodModifier> modifiers) throws IOException {
    return body != null ? body : delegate.resolveBody(modifiers);
  }

  @Override
  public Object resolveAnnotationsDefaultValue() {
    return delegate.resolveAnnotationsDefaultValue();
  }

  @Nonnull
  @Override
  public MethodSignature getSignature() {
    return methodSignature != null ? methodSignature : delegate.getSignature();
  }

  @Nonnull
  public OverridingBodySource withBody(@Nonnull Body body) {
    return new OverridingBodySource(delegate, body);
  }
}
