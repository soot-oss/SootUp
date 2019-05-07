package de.upb.soot.frontends.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

class AsmClassClassSourceContent extends AbstractAsmSourceContent {

  public AsmClassClassSourceContent(@Nonnull ClassSource classSource) {
    super(classSource);
  }

  private static Set<SootField> resolveFields(
      List<FieldNode> fieldNodes, SignatureFactory signatureFactory, JavaClassType classSignature) {
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
      List<MethodNode> methodNodes, SignatureFactory signatureFactory, JavaClassType cs) {
    return methodNodes.stream()
        .map(
            methodSource -> {
              if (!(methodSource instanceof AsmMethodSourceContent)) {
                throw new AsmFrontendException(
                    String.format("Failed to create Method Signature %s", methodSource));
              }
              AsmMethodSourceContent asmClassClassSourceContent =
                  (AsmMethodSourceContent) methodSource;

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

  @Override
  @Nonnull
  public Collection<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    SignatureFactory signatureFactory = DefaultSignatureFactory.getInstance();
    return resolveMethods(this.methods, signatureFactory, signature).collect(Collectors.toSet());
  }

  @Override
  @Nonnull
  public Collection<SootField> resolveFields(@Nonnull JavaClassType classSignature)
      throws ResolveException {
    SignatureFactory signatureFactory = DefaultSignatureFactory.getInstance();
    return resolveFields(fields, signatureFactory, classSignature);
  }

  @Override
  @Nonnull
  public MethodVisitor visitMethod(
      int access,
      @Nonnull String name,
      @Nonnull String desc,
      @Nonnull String signature,
      @Nonnull String[] exceptions) {

    AsmMethodSourceContent mn =
        new AsmMethodSourceContent(access, name, desc, signature, exceptions);
    methods.add(mn);
    return mn;
  }
}
