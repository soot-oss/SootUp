package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.interceptors.DexArrayIndexConstantInliner;
import sootup.apk.frontend.interceptors.DexNullTransformer;
import sootup.apk.frontend.interceptors.DexNumberTranformer;
import sootup.apk.frontend.interceptors.DexSharedConstantSplitter;
import sootup.core.interceptor.BodyInterceptor;
import sootup.interceptors.LocalSplitter;
import sootup.interceptors.TypeAssigner;
import sootup.interceptors.UnusedLocalEliminator;

public enum DexBodyInterceptors {
  /**
   * Splits the locals first, as Soot's dexpler does: dex code reuses registers for values of
   * different types, and the number and null transformers decide per local whether its constants
   * are floats or nulls. A constant that several statements use is then copied per statement
   * ({@link DexSharedConstantSplitter}), so each use decides about its own copy.
   *
   * <p>{@link DexArrayIndexConstantInliner} runs after the null/number passes, which settle which
   * constants are ints, and before {@link UnusedLocalEliminator} drops the index locals it inlines
   * away. {@link TypeAssigner} runs last: dex registers are untyped, so dexlib2-derived locals start
   * out as {@code UnknownType}; it applies the same type resolution the bytecode frontend applies to
   * javac-derived bodies, so every local of a {@link #Default} body carries a concrete type.
   */
  Default(
      new LocalSplitter(),
      new DexSharedConstantSplitter(),
      new DexNumberTranformer(),
      new DexNullTransformer(),
      new DexArrayIndexConstantInliner(),
      new UnusedLocalEliminator(),
      new TypeAssigner()),

  /**
   * The dex transformers of {@link #Default} without its {@link LocalSplitter}, for callers that
   * put their own in front of them, e.g. one with another {@link LocalSplitter.ExceptionalFlow}.
   */
  Transformers(
      new DexSharedConstantSplitter(),
      new DexNumberTranformer(),
      new DexNullTransformer(),
      new DexArrayIndexConstantInliner());

  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  DexBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @NonNull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
