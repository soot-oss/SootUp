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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
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

  public JavaSootMethod makeSootMethod(
      final Method method, List<BodyInterceptor> bodyInterceptors, @Nonnull View view) {
    int modifierFlags = method.getAccessFlags();
    if (Modifier.isAbstract(modifierFlags) || Modifier.isNative(modifierFlags)) {
      String className = declaringclassType.getClassName();
      if (DexUtil.isByteCodeClassName(className)) {
        className = DexUtil.dottedClassName(className);
      }
      MethodSignature methodSignature =
          new MethodSignature(
              declaringclassType,
              className,
              Collections.emptyList(),
              DexUtil.toSootType(method.getReturnType(), 0));
      DexMethodSource dexMethodSource =
          new DexMethodSource(
              Collections.emptySet(),
              methodSignature,
              new MutableBlockStmtGraph(),
              method,
              bodyInterceptors,
              view);
      return dexMethodSource.makeSootMethod();
    } else {
      DexBody dexBody = new DexBody(method, dexEntry, declaringclassType);
      return dexBody.makeSootMethod(method, declaringclassType, bodyInterceptors, view);
    }
  }
}
