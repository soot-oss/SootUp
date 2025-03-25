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

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jf.dexlib2.dexbacked.raw.EncodedValue;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.Util.DexUtil;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.Modifiers;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.language.JavaJimple;

public class DexClassSource extends JavaSootClassSource {

  DexLibWrapper wrapper;

  DexLibWrapper.ClassInformation classInformation;

  List<BodyInterceptor> bodyInterceptors;

  @NonNull private final View view;

  @NonNull
  public View getView() {
    return view;
  }

  public DexClassSource(
      @NonNull View view,
      @NonNull AnalysisInputLocation analysisInputLocation,
      @NonNull ClassType classSignature,
      @NonNull Path sourcePath) {
    super(analysisInputLocation, classSignature, sourcePath);
    // Initialize only for the first time.
    this.view = view;
    this.bodyInterceptors = analysisInputLocation.getBodyInterceptors();
    if (this.wrapper == null) {
      this.wrapper = DexResolver.getInstance().initializeDexFile(new File(sourcePath.toString()));
    }
    this.classInformation = wrapper.getClassInformation(classSignature);
  }

  @NonNull
  @Override
  public Collection<? extends JavaSootMethod> resolveMethods() throws ResolveException {
    if (classInformation != null) {
      DexMethod dexMethod = createDexMethodFactory(classInformation.dexEntry, classSignature);
      return StreamSupport.stream(
              classInformation.classDefinition.getMethods().spliterator(), false)
          .map(method -> loadMethod(method, dexMethod))
          .collect(Collectors.toSet());
    } else {
      throw new IllegalStateException("Class Information Should not be null");
    }
  }

  @NonNull
  @Override
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    return resolveFields(
        classInformation.classDefinition.getFields(), view.getIdentifierFactory(), classSignature);
  }

  @NonNull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return Modifiers.getClassModifiers(classInformation.classDefinition.getAccessFlags());
  }

  @NonNull
  @Override
  public Set<? extends ClassType> resolveInterfaces() {
    List<String> interfaces = classInformation.classDefinition.getInterfaces();
    if (interfaces.isEmpty()) {
      return new HashSet<>();
    }
    return interfaces.stream()
        .map(interface1 -> DexUtil.stringToJimpleType(view, interface1))
        .collect(Collectors.toSet());
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveSuperclass() {
    if (classInformation != null) {
      String superclass = classInformation.classDefinition.getSuperclass();
      if (superclass.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(DexUtil.stringToJimpleType(view, superclass));
      }
    } else {
      throw new IllegalStateException("Class Information Should not be null");
    }
  }

  @NonNull
  @Override
  public Optional<? extends ClassType> resolveOuterClass() {
    return Optional.empty();
  }

  @NonNull
  @Override
  public Position resolvePosition() {
    return NoPositionInformation.getInstance();
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    if (classInformation != null) {
      return convertAnnotation(classInformation.classDefinition.getAnnotations());
    }
    return Collections.emptyList();
  }

  private DexMethod createDexMethodFactory(
      MultiDexContainer.DexEntry<? extends DexFile> dexEntry, final ClassType declaringClass) {
    return new DexMethod(dexEntry, declaringClass);
  }

  private JavaSootMethod loadMethod(Method method, DexMethod dexMethod) {
    return dexMethod.makeSootMethod(method, bodyInterceptors, view);
  }

  protected List<AnnotationUsage> convertAnnotation(Set<? extends Annotation> annotations) {
    if (annotations.isEmpty()) {
      return Collections.emptyList();
    }
    ArrayList<AnnotationUsage> annotationUsage = new ArrayList<>();
    /* annotation.getVisibility() returns an integer refer org.jf.dexlib2.AnnotationVisibility.java
     * 0 -> BUILD
     * 1 -> RUNTIME
     * 2 -> SYSTEM
     * */
    Map<String, Object> paramMap = new HashMap<>();
    for (Annotation annotation : annotations) {
      for (AnnotationElement element : annotation.getElements()) {
        String name = element.getName();
        paramMap.put(name, convertAnnotationValue(element.getValue().getValueType()));
      }
      ClassType at =
          getView()
              .getIdentifierFactory()
              .getClassType(DexUtil.toQualifiedName(annotation.getType()));
      annotationUsage.add(new AnnotationUsage(at, paramMap));
    }
    return annotationUsage;
  }

  private static Object convertAnnotationValue(Object annotationValue) {
    if (annotationValue instanceof EncodedValue) {
      ClassConstant classConstant =
          JavaJimple.getInstance().newClassConstant(annotationValue.toString());
      return ConstantUtil.fromObject(classConstant);
    }
    return ConstantUtil.fromObject(annotationValue);
  }

  private static Set<JavaSootField> resolveFields(
      Iterable<? extends Field> fields,
      IdentifierFactory signatureFactory,
      ClassType classSignature) {
    return StreamSupport.stream(fields.spliterator(), false)
        .map(
            field -> {
              String fieldName = field.getName();
              Type fieldType = DexUtil.toSootType(field.getType(), 0);
              FieldSignature fieldSignature =
                  signatureFactory.getFieldSignature(fieldName, classSignature, fieldType);
              EnumSet<FieldModifier> modifiers =
                  Modifiers.getFieldModifiers(field.getAccessFlags());

              return new JavaSootField(
                  fieldSignature,
                  modifiers,
                  Collections.emptySet(), // TODO Fix this annotations [PM]
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
  }
}
