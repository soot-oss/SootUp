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

import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.main.DexBody;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.Modifiers;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

/** Converts the dex code of a method lazily, so a failing method does not affect its class. */
public class DexMethodSource implements BodySource {

  private final MethodSignature methodSignature;
  private final Method method;
  private final MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
  private final List<BodyInterceptor> bodyInterceptors;
  @NonNull private final View view;

  public DexMethodSource(
      MethodSignature methodSignature,
      Method method,
      MultiDexContainer.DexEntry<? extends DexFile> dexEntry,
      List<BodyInterceptor> bodyInterceptors,
      @NonNull View view) {
    this.methodSignature = methodSignature;
    this.method = method;
    this.dexEntry = dexEntry;
    this.bodyInterceptors = bodyInterceptors;
    this.view = view;
  }

  @NonNull
  @Override
  public Body resolveBody(@NonNull Iterable<MethodModifier> modifiers) throws ResolveException {
    Set<MethodModifier> modifiersSet =
        StreamSupport.stream(modifiers.spliterator(), false).collect(Collectors.toSet());
    try {
      DexBody dexBody = new DexBody(method, dexEntry, methodSignature.getDeclClassType());
      Body.BodyBuilder bodyBuilder =
          Body.builder(dexBody.buildControlFlowGraph())
              .setModifiers(modifiersSet)
              .setMethodSignature(methodSignature)
              .setPosition(NoPositionInformation.getInstance())
              .setLocals(dexBody.getLocals());
      for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
        bodyInterceptor.interceptBody(bodyBuilder, view);
      }
      return bodyBuilder.build();
    } catch (RuntimeException e) {
      throw new ResolveException(
          "Could not convert the dex code of " + methodSignature,
          Paths.get(dexEntry.getEntryName()),
          e);
    }
  }

  public JavaSootMethod makeSootMethod() {
    EnumSet<MethodModifier> methodModifiers = Modifiers.getMethodModifiers(method.getAccessFlags());
    return new JavaSootMethod(
        this,
        methodSignature,
        methodModifiers,
        Collections.emptyList(),
        Collections.emptySet(),
        NoPositionInformation.getInstance());
  }

  // annotation default values (dalvik.annotation.AnnotationDefault) are not read yet
  @Override
  public Object resolveAnnotationsDefaultValue() {
    return null;
  }

  @NonNull
  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }
}
