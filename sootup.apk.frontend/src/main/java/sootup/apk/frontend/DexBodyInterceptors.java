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
import sootup.apk.frontend.interceptors.DexNullTransformer;
import sootup.apk.frontend.interceptors.DexNumberTranformer;
import sootup.core.interceptor.BodyInterceptor;
import sootup.interceptors.TypeAssigner;

public enum DexBodyInterceptors {
  // TypeAssigner runs last: unlike javac-derived bytecode (where every local already carries a
  // concrete declared type), dex bytecode is weakly typed at the bytecode level — a register can
  // be used as an int, a boolean, or an object reference at different points, disambiguated only
  // by how it's used — so dexlib2-derived Jimple locals start out with
  // sootup.core.types.UnknownType
  // until something resolves them. DexNumberTranformer/DexNullTransformer narrow numeric and
  // null-vs-zero ambiguity first; TypeAssigner then does the same most-specific-common-supertype
  // resolution JavaClassPathAnalysisInputLocation already applies to javac-derived bodies (see
  // SootUpManager.createView in the FAIR project for the equivalent javac-side wiring), so a
  // consumer that needs every local to carry a concrete, non-Unknown type (e.g. FAIR's
  // TypeExtension.toFairType(), which has no case for UnknownType) doesn't need every downstream
  // caller to remember to run its own type-resolution pass over dex bodies.
  Default(new DexNumberTranformer(), new DexNullTransformer(), new TypeAssigner());

  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  DexBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @NonNull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
