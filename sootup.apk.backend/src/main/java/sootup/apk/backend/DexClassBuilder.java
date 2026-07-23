package sootup.apk.backend;

import java.util.List;
import java.util.Set;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.types.ClassType;
import sootup.core.views.View;

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

    List<Annotation> annotations = null; // TODO

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

    // initialize a constant field
    // static fields of type Primitive, String, null
    ImmutableEncodedValue initialValue = null; // TODO

    Set<Annotation> fieldAnnotations = null; // TODO

    return new ImmutableField(
        classType, fieldName, fieldType, accessFlags, initialValue, fieldAnnotations, null);
  }

  protected View getView() {
    return dexOutputLocation.getView();
  }
}
