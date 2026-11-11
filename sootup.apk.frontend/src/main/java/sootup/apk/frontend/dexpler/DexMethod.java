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
import java.util.Map;
import java.util.stream.Collectors;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.Util.DexUtil;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.Modifiers;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class DexMethod {

  protected final MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
  protected final ClassType declaringclassType;
  // the default values of the methods, if the class is an annotation
  private final Map<String, ? extends EncodedValue> annotationDefaults;

  public DexMethod(
      final MultiDexContainer.DexEntry<? extends DexFile> dexFile, final ClassType declaringClass) {
    this(dexFile, declaringClass, Collections.emptyMap());
  }

  public DexMethod(
      final MultiDexContainer.DexEntry<? extends DexFile> dexFile,
      final ClassType declaringClass,
      @NonNull final Map<String, ? extends EncodedValue> annotationDefaults) {
    this.dexEntry = dexFile;
    this.declaringclassType = declaringClass;
    this.annotationDefaults = annotationDefaults;
  }

  public JavaSootMethod makeSootMethod(
      final Method method, List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    List<Type> parameters =
        method.getParameters().stream()
            .map(
                methodParameter ->
                    DexUtil.toSootType(methodParameter.getType(), 0, view.getIdentifierFactory()))
            .collect(Collectors.toList());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                declaringclassType,
                method.getName(),
                DexUtil.toSootType(method.getReturnType(), 0, view.getIdentifierFactory()),
                parameters);
    int modifierFlags = method.getAccessFlags();
    if (Modifier.isAbstract(modifierFlags) || Modifier.isNative(modifierFlags)) {
      DexMethodSource dexMethodSource =
          new DexMethodSource(
              Collections.emptySet(),
              methodSignature,
              new MutableBlockControlFlowGraph(),
              method,
              bodyInterceptors,
              view,
              annotationDefaults.get(method.getName()));
      return dexMethodSource.makeSootMethod();
    }
    // the code is converted when the body is requested for the first time
    return new JavaSootMethod(
        new DexMethodSource(
            method, dexEntry, declaringclassType, methodSignature, bodyInterceptors, view),
        methodSignature,
        Modifiers.getMethodModifiers(modifierFlags),
        DexUtil.extractThrownExceptions(method.getAnnotations(), view.getIdentifierFactory()),
        DexUtil.createAnnotationUsage(method.getAnnotations(), view.getIdentifierFactory()),
        NoPositionInformation.getInstance());
  }
}
