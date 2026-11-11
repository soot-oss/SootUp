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

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.DexBody;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.util.Modifiers;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootMethod;

public class DexMethodSource implements BodySource {

  // null until the code is converted, for a method whose body is resolved lazily
  @Nullable private final Set<Local> locals;
  @Nullable private final MutableControlFlowGraph mutableControlFlowGraph;
  private final Method method;
  private final MultiDexContainer.@Nullable DexEntry<? extends DexFile> dexEntry;
  @Nullable private final ClassType declaringClass;

  private final List<BodyInterceptor> bodyInterceptors;

  @NonNull private final View view;
  private final MethodSignature methodSignature;

  public DexMethodSource(
      Set<Local> locals,
      MethodSignature methodSignature,
      MutableControlFlowGraph mutableControlFlowGraph,
      Method method,
      List<BodyInterceptor> bodyInterceptors,
      @NonNull View view) {
    this.methodSignature = methodSignature;
    this.view = view;
    this.locals = locals;
    this.bodyInterceptors = bodyInterceptors;
    this.mutableControlFlowGraph = mutableControlFlowGraph;
    this.method = method;
    this.dexEntry = null;
    this.declaringClass = null;
  }

  /** A body source that converts the code of the method when the body is resolved. */
  public DexMethodSource(
      Method method,
      MultiDexContainer.DexEntry<? extends DexFile> dexEntry,
      ClassType declaringClass,
      MethodSignature methodSignature,
      List<BodyInterceptor> bodyInterceptors,
      @NonNull View view) {
    this.methodSignature = methodSignature;
    this.view = view;
    this.locals = null;
    this.bodyInterceptors = bodyInterceptors;
    this.mutableControlFlowGraph = null;
    this.method = method;
    this.dexEntry = dexEntry;
    this.declaringClass = declaringClass;
  }

  @NonNull
  @Override
  public Body resolveBody(@NonNull Iterable<MethodModifier> modifiers)
      throws ResolveException, IOException {
    Set<Local> locals = this.locals;
    MutableControlFlowGraph graph = mutableControlFlowGraph;
    if (graph == null) {
      DexBody dexBody = new DexBody(method, dexEntry, declaringClass, view);
      graph = dexBody.buildGraph(methodSignature);
      locals = dexBody.getLocals();
    }
    Set<MethodModifier> modifiersSet =
        StreamSupport.stream(modifiers.spliterator(), false).collect(Collectors.toSet());
    Body.BodyBuilder bodyBuilder =
        Body.builder(graph)
            .setModifiers(modifiersSet)
            .setMethodSignature(getSignature())
            .setPosition(NoPositionInformation.getInstance())
            .setLocals(locals);
    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      bodyInterceptor.interceptBody(bodyBuilder, view);
    }
    return bodyBuilder.build();
  }

  public JavaSootMethod makeSootMethod() {
    JavaSootMethod sootMethod;
    EnumSet<MethodModifier> methodModifiers = Modifiers.getMethodModifiers(method.getAccessFlags());
    try {
      // keep this source as delegate, so the parameter annotations stay available
      sootMethod =
          new JavaSootMethod(
              new OverridingBodySource(this).withBody(resolveBody(methodModifiers)),
              getSignature(),
              methodModifiers,
              DexUtil.extractThrownExceptions(method.getAnnotations(), view.getIdentifierFactory()),
              DexUtil.createAnnotationUsage(method.getAnnotations(), view.getIdentifierFactory()),
              NoPositionInformation.getInstance());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sootMethod;
  }

  @Override
  public Object resolveAnnotationsDefaultValue() {
    // only abstract methods of annotations have default values, and they do not use this source
    return null;
  }

  @Override
  public List<AnnotationUsage> getParameterAnnotations(int paramIndex) {
    return getParameterAnnotations(paramIndex, "Any");
  }

  /**
   * Returns the annotations of a parameter. Dex files store BUILD annotations as runtime invisible
   * and RUNTIME annotations as runtime visible ones.
   */
  @Override
  public List<AnnotationUsage> getParameterAnnotations(int paramIndex, String visibility) {
    List<? extends MethodParameter> parameters = method.getParameters();
    if (paramIndex < 0 || paramIndex >= parameters.size()) {
      return Collections.emptyList();
    }
    boolean visible = "Any".equals(visibility) || "RuntimeVisible".equals(visibility);
    boolean invisible = "Any".equals(visibility) || "RuntimeInvisible".equals(visibility);
    return DexUtil.createAnnotationUsage(
            parameters.get(paramIndex).getAnnotations(), view.getIdentifierFactory())
        .stream()
        .filter(usage -> usage.isRuntimeVisible() ? visible : invisible)
        .collect(Collectors.toList());
  }

  @NonNull
  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }
}
