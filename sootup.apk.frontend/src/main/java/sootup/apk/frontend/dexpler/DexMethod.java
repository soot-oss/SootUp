package sootup.apk.frontend.dexpler;

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

import java.util.List;
import java.util.stream.Collectors;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.Util.DexUtil;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class DexMethod {

  protected final MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
  protected final ClassType declaringclassType;

  public DexMethod(
      final MultiDexContainer.DexEntry<? extends DexFile> dexFile, final ClassType declaringClass) {
    this.dexEntry = dexFile;
    this.declaringclassType = declaringClass;
  }

  /** Creates the method; its body is converted on the first {@code getBody()}. */
  public JavaSootMethod makeSootMethod(
      final Method method, List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    List<Type> parameters =
        method.getParameters().stream()
            .map(methodParameter -> DexUtil.toSootType(methodParameter.getType(), 0))
            .collect(Collectors.toList());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                declaringclassType,
                method.getName(),
                DexUtil.toSootType(method.getReturnType(), 0),
                parameters);
    return new DexMethodSource(methodSignature, method, dexEntry, bodyInterceptors, view)
        .makeSootMethod();
  }
}
