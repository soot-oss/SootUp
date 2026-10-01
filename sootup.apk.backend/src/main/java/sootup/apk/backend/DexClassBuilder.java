package sootup.apk.backend;

import java.util.*;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.value.ImmutableArrayEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableNullEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableTypeEncodedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootField;

public class DexClassBuilder {

  private static final Logger log = LoggerFactory.getLogger(DexClassBuilder.class);

  private final DexOutputLocation dexOutputLocation;
  private final DexMethodBuilder dexMethodBuilder;

  public DexClassBuilder(DexOutputLocation dexOutputLocation) {
    this.dexOutputLocation = dexOutputLocation;
    this.dexMethodBuilder = new DexMethodBuilder(getView());
  }

  public void createClass(SootClass c) {
    log.info("Creating class {}", c.getName());
    String sourceFile = c.getClassSource().getClassType().getClassName();
    ClassType classType = c.getType();
    String dexClassType = DexUtil.toDexType(classType);

    int accessFlags =
        c.getModifiers().stream()
            .mapToInt(ClassModifier::getBytecode)
            .reduce(0, (flagsBefore, newFlag) -> flagsBefore | newFlag);

    List<Field> fields =
        !c.getFields().isEmpty()
            ? c.getFields().stream().map(f -> createField(dexClassType, f)).toList()
            : null;

    String superclass =
        c.getSuperclass().isPresent()
            ? DexUtil.toDexClassName(c.getSuperclass().get().getFullyQualifiedName())
            : null;

    List<String> interfaces =
        !c.getInterfaces().isEmpty()
            ? c.getInterfaces().stream().map(DexUtil::toDexType).toList()
            : null;

    List<ImmutableMethod> dexMethods =
        !c.getMethods().isEmpty()
            ? c.getMethods().stream().map(dexMethodBuilder::createMethod).toList()
            : null;

    List<Annotation> annotations = createClassAnnotations(c);

    ClassDef classDef =
        new ImmutableClassDef(
            dexClassType,
            accessFlags,
            superclass,
            interfaces,
            sourceFile,
            annotations,
            fields,
            dexMethods);

    synchronized (dexOutputLocation) {
      dexOutputLocation.addClass(classDef);
    }
  }

  private Field createField(String classType, SootField f) {
    String fieldName = f.getName();
    String fieldType = DexUtil.toDexType(f.getType());

    int accessFlags =
        f.getModifiers().stream()
            .mapToInt(FieldModifier::getBytecode)
            .reduce(0, (flagsBefore, newFlag) -> flagsBefore | newFlag);

    EncodedValue initialValue = null;

    if (f instanceof JavaSootField javaSootField) {
      var a = javaSootField.getAnnotations();
      for (var annotation : a) {
        if (annotation.getAnnotation().getClassName().equals("initialValue")) {
          Object value = annotation.getValues().get("value");
          if (value instanceof EncodedValue) {
            initialValue = (EncodedValue) value;
          }
        }
      }
    }

    Set<Annotation> fieldAnnotations = createFieldAnnotations(f);

    return new ImmutableField(
        classType, fieldName, fieldType, accessFlags, initialValue, fieldAnnotations, null);
  }

  private List<Annotation> createClassAnnotations(SootClass c) {
    List<Annotation> annotations = new ArrayList<>();
    if (c instanceof JavaSootClass javaSootClass) {
      var a = javaSootClass.getAnnotations();
      for (var annotation : a) {
        List<AnnotationElement> annotationElements = new ArrayList<>();
        for (var entry : annotation.getValues().entrySet()) {
          AnnotationElement annotationElement =
              new ImmutableAnnotationElement(
                  entry.getKey(), DexUtil.buildEncodedValueForAnnotation(entry.getValue()));
          annotationElements.add(annotationElement);
        }
        ImmutableAnnotation ann =
            new ImmutableAnnotation(
                annotation.getVisibility() == AnnotationUsage.AnnotationUsageVisibility.RUNTIME
                    ? AnnotationVisibility.RUNTIME
                    : AnnotationVisibility.BUILD,
                DexUtil.toDexClassName(annotation.getAnnotation().getFullyQualifiedName()),
                annotationElements);
        annotations.add(ann);
      }
    }

    // TODO How to distinguish enclosingClass and enclosingMethod in SootUp?
    if (c.getOuterClass().isPresent()) {
      ImmutableAnnotationElement enclosingElement =
          new ImmutableAnnotationElement(
              "value",
              new ImmutableTypeEncodedValue(
                  DexUtil.toDexClassName(c.getOuterClass().get().getFullyQualifiedName())));
      annotations.add(
          new ImmutableAnnotation(
              AnnotationVisibility.SYSTEM,
              "Ldalvik/annotation/EnclosingClass;",
              Collections.singleton(enclosingElement)));
    } else if (c.getName().contains("$")
        && dexOutputLocation
            .getView()
            .getClasses()
            .anyMatch(
                cl -> cl.getName().equals(c.getName().substring(0, c.getName().indexOf("$"))))) {
      ImmutableAnnotationElement enclosingElement =
          new ImmutableAnnotationElement(
              "value",
              new ImmutableTypeEncodedValue(
                  DexUtil.toDexClassName(c.getName().substring(0, c.getName().indexOf("$")))));
      annotations.add(
          new ImmutableAnnotation(
              AnnotationVisibility.SYSTEM,
              "Ldalvik/annotation/EnclosingClass;",
              Collections.singleton(enclosingElement)));
    }

    if (c.isInnerClass()) {
      ImmutableEncodedValue immutableEncodedValue;
      if (c.getName().contains("$")
          && c.getName().substring(c.getName().lastIndexOf('$') + 1).matches("\\d+")) {
        immutableEncodedValue = ImmutableNullEncodedValue.INSTANCE;
      } else {
        immutableEncodedValue =
            new ImmutableTypeEncodedValue(
                DexUtil.toDexClassName(c.getName().substring(c.getName().lastIndexOf("$"))));
      }

      ImmutableAnnotationElement enclosingElement =
          new ImmutableAnnotationElement("value", immutableEncodedValue);
      annotations.add(
          new ImmutableAnnotation(
              AnnotationVisibility.SYSTEM,
              "Ldalvik/annotation/InnerClass;",
              Collections.singleton(enclosingElement)));
    }

    if ((!c.getName().contains("$")
            || (c.getName().contains("$")
                && !c.getName().substring(c.getName().lastIndexOf('$') + 1).matches("\\d+")))
        && dexOutputLocation
            .getView()
            .getClasses()
            .anyMatch(cl -> cl.getName().contains(c.getName()))) {
      List<String> classNames =
          dexOutputLocation
              .getView()
              .getClasses()
              .map(SootClass::getName)
              .filter(
                  name ->
                      name.contains(c.getName())
                          && !(name.contains("$")
                              && name.substring(name.lastIndexOf('$') + 1).matches("\\d+")))
              .toList();
      List<ImmutableTypeEncodedValue> classes = new ArrayList<>();
      for (String memberClass : classNames) {
        classes.add(new ImmutableTypeEncodedValue(DexUtil.toDexClassName(memberClass)));
      }
      ImmutableArrayEncodedValue classesValue = new ImmutableArrayEncodedValue(classes);
      ImmutableAnnotationElement element = new ImmutableAnnotationElement("value", classesValue);
      ImmutableAnnotation memberAnnotation =
          new ImmutableAnnotation(
              AnnotationVisibility.SYSTEM,
              "Ldalvik/annotation/MemberClasses;",
              Collections.singletonList(element));
      annotations.add(memberAnnotation);
    }

    return annotations;
  }

  private Set<Annotation> createFieldAnnotations(SootField f) {
    Set<Annotation> annotations = new HashSet<>();
    if (f instanceof JavaSootField javaSootField) {
      var a = javaSootField.getAnnotations();
      for (var annotation : a) {
        if (annotation.getVisibility() == AnnotationUsage.AnnotationUsageVisibility.NONE) {
          continue;
        }
        List<AnnotationElement> annotationElements = new ArrayList<>();
        for (var entry : annotation.getValues().entrySet()) {
          AnnotationElement annotationElement =
              new ImmutableAnnotationElement(
                  entry.getKey(), DexUtil.buildEncodedValueForAnnotation(entry.getValue()));
          annotationElements.add(annotationElement);
        }
        ImmutableAnnotation ann =
            new ImmutableAnnotation(
                annotation.getVisibility() == AnnotationUsage.AnnotationUsageVisibility.RUNTIME
                    ? AnnotationVisibility.RUNTIME
                    : AnnotationVisibility.BUILD,
                DexUtil.toDexClassName(annotation.getAnnotation().getFullyQualifiedName()),
                annotationElements);
        annotations.add(ann);
      }
    }
    return annotations;
  }

  protected View getView() {
    return dexOutputLocation.getView();
  }
}
