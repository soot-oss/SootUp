package dexpler;

import Util.DexUtil;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;
import sootup.java.core.*;

public class DexClassSource extends JavaSootClassSource {

  DexLibWrapper wrapper;

  DexLibWrapper.ClassInformation classInformation;

  List<BodyInterceptor> bodyInterceptors;

  @Nonnull private final View view;

  public DexClassSource(
      @Nonnull View view,
      @Nonnull AnalysisInputLocation analysisInputLocation,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(analysisInputLocation, classSignature, sourcePath);
    // Initialize only for the first time.
    this.view = view;
    this.bodyInterceptors = analysisInputLocation.getBodyInterceptors();
    if (this.wrapper == null) {
      this.wrapper = DexResolver.getInstance().initializeDexFile(new File(sourcePath.toString()));
    }
    this.classInformation = wrapper.getClassInformation(classSignature);
  }

  @Nonnull
  @Override
  public Collection<? extends JavaSootMethod> resolveMethods() throws ResolveException {
    Iterable<? extends Method> methodIterable = classInformation.classDefinition.getMethods();
    DexMethod dexMethod = createDexMethodFactory(classInformation.dexEntry, classSignature);
    // Convert the Iterable to a Stream
    Stream<? extends Method> methodStream =
        StreamSupport.stream(methodIterable.spliterator(), false);
    Iterable<? extends Method> virtualMethodIterable =
        classInformation.classDefinition.getVirtualMethods();
    Stream<? extends Method> virtualMethodStream =
        StreamSupport.stream(virtualMethodIterable.spliterator(), false);
    return Stream.concat(methodStream, virtualMethodStream)
        .map(method -> loadMethod(method, dexMethod))
        .collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Collection<? extends SootField> resolveFields() throws ResolveException {
    return resolveFields(
        classInformation.classDefinition.getFields(),
        JavaIdentifierFactory.getInstance(),
        classSignature);
  }

  @Nonnull
  @Override
  public Set<ClassModifier> resolveModifiers() {
    return DexUtil.getClassModifiers(classInformation.classDefinition.getAccessFlags());
  }

  @Nonnull
  @Override
  public Set<? extends ClassType> resolveInterfaces() {
    List<String> interfaces = classInformation.classDefinition.getInterfaces();
    if (interfaces.isEmpty()) {
      return new HashSet<>();
    }
    return interfaces.stream().map(DexUtil::stringToJimpleType).collect(Collectors.toSet());
  }

  @Nonnull
  @Override
  public Optional<? extends ClassType> resolveSuperclass() {
    if (classInformation != null) {
      String superclass = classInformation.classDefinition.getSuperclass();
      if (superclass.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(DexUtil.stringToJimpleType(superclass));
      }
    }
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Optional<? extends ClassType> resolveOuterClass() {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Position resolvePosition() {
    return NoPositionInformation.getInstance();
  }

  @Override
  protected Iterable<AnnotationUsage> resolveAnnotations() {
    return convertAnnotation(classInformation.classDefinition.getAnnotations());
  }

  private DexMethod createDexMethodFactory(
      MultiDexContainer.DexEntry<? extends DexFile> dexEntry, final ClassType declaringClass) {
    return new DexMethod(dexEntry, declaringClass);
  }

  private JavaSootMethod loadMethod(Method method, DexMethod dexMethod) {
    return dexMethod.makeSootMethod(method, bodyInterceptors, view);
  }

  protected static List<AnnotationUsage> convertAnnotation(Set<? extends Annotation> annotations) {
    if (annotations.isEmpty()) {
      return Collections.emptyList();
    }
    return null;
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
              EnumSet<FieldModifier> modifiers = DexUtil.getFieldModifiers(field.getAccessFlags());

              return new JavaSootField(
                  fieldSignature,
                  modifiers,
                  Collections.emptySet(), // TODO Fix this annotations [PM]
                  NoPositionInformation.getInstance());
            })
        .collect(Collectors.toSet());
  }
}
