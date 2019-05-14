package de.upb.soot.frontends.asm;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

class AsmClassSource extends ClassSource {

  @Nonnull private final ClassNode classNode;

  public AsmClassSource(
      INamespace namespace,
      Path sourcePath,
      JavaClassType javaClassType,
      @Nonnull ClassNode classNode) {
    super(namespace, sourcePath, javaClassType);
    this.classNode = classNode;
  }

  private static Set<SootField> resolveFields(
      List<FieldNode> fieldNodes,
      IdentifierFactory signatureFactory,
      JavaClassType classSignature) {
    // FIXME: add support for annotation
    return fieldNodes.stream()
        .map(
            fieldNode -> {
              String fieldName = fieldNode.name;
              Type fieldType = AsmUtil.toJimpleType(fieldNode.desc);
              FieldSignature fieldSignature =
                  signatureFactory.getFieldSignature(fieldName, classSignature, fieldType);
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);

              return new SootField(fieldSignature, modifiers);
            })
        .collect(Collectors.toSet());
  }

  private static Stream<SootMethod> resolveMethods(
      List<MethodNode> methodNodes, IdentifierFactory signatureFactory, JavaClassType cs) {
    return methodNodes.stream()
        .map(
            methodSource -> {
              if (!(methodSource instanceof AsmMethodSource)) {
                throw new AsmFrontendException(
                    String.format("Failed to create Method Signature %s", methodSource));
              }
              AsmMethodSource asmClassClassSourceContent = (AsmMethodSource) methodSource;

              List<JavaClassType> exceptions = new ArrayList<>();
              Iterable<JavaClassType> exceptionsSignatures =
                  AsmUtil.asmIdToSignature(methodSource.exceptions);

              for (JavaClassType exceptionSig : exceptionsSignatures) {
                exceptions.add(exceptionSig);
              }
              String methodName = methodSource.name;
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
              List<Type> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc);
              Type retType = sigTypes.remove(sigTypes.size() - 1);

              MethodSignature methodSignature =
                  signatureFactory.getMethodSignature(methodName, cs, retType, sigTypes);

              return SootMethod.builder()
                  .withSource(asmClassClassSourceContent)
                  .withSignature(methodSignature)
                  .withModifiers(modifiers)
                  .withThrownExceptions(exceptions)
                  .build();
            });
  }

  @Nonnull
  public Collection<SootMethod> resolveMethods() throws ResolveException {
    IdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
    return resolveMethods(classNode.methods, identifierFactory, classSignature)
        .collect(Collectors.toSet());
  }

  @Nonnull
  public Collection<SootField> resolveFields() throws ResolveException {
    IdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
    return resolveFields(classNode.fields, identifierFactory, classSignature);
  }

  public Set<Modifier> resolveModifiers() {
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(classNode.access);
    return modifiers;
  }

  public Set<JavaClassType> resolveInterfaces() {
    return new HashSet<>(AsmUtil.asmIdToSignature(classNode.interfaces));
  }

  public Optional<JavaClassType> resolveSuperclass() {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.superName));
  }

  public Optional<JavaClassType> resolveOuterClass() {
    return Optional.ofNullable(AsmUtil.asmIDToSignature(classNode.outerClass));
  }

  public CAstSourcePositionMap.Position resolvePosition() {
    // FIXME: what is this??? the source code line number of the complete file?
    return null;
  }
}
