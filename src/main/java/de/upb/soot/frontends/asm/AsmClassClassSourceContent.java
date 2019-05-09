package de.upb.soot.frontends.asm;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ClassType;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.IdentifierFactory;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.DefaultIdentifierFactory;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.views.IView;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

class AsmClassClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements IClassSourceContent {

  private final ClassSource classSource;

  public AsmClassClassSourceContent(@Nonnull ClassSource classSource) {
    super(AsmUtil.SUPPORTED_ASM_OPCODE);

    this.classSource = classSource;

    // FIXME: maybe delete class reading
    AsmUtil.initAsmClassSource(classSource, this);
  }

  @Override
  @Nonnull
  public AbstractClass resolveClass(@Nonnull ResolvingLevel level, @Nonnull IView view)
      throws AsmFrontendException {

    JavaClassType cs = view.getIdentifierFactory().getClassType(this.signature);
    SootClass.SootClassSurrogateBuilder builder;

    // FIXME: currently ugly because, the original class is always re-resolved but never copied...
    switch (level) {
      case DANGLING:
        builder = (SootClass.SootClassSurrogateBuilder) resolveDangling(view, cs);
        break;

      case HIERARCHY:
        builder = (SootClass.SootClassSurrogateBuilder) resolveHierarchy(view, cs);
        break;

      case SIGNATURES:
        builder = (SootClass.SootClassSurrogateBuilder) resolveSignature(view, cs);
        break;

      case BODIES:
        builder = (SootClass.SootClassSurrogateBuilder) resolveBody(view, cs);
        break;

      default:
        throw new AsmFrontendException("Unsupported resolving level \"" + level + "\".");
    }

    return builder.build();
  }

  // FIXME: Parameter `cs` is unused
  @Nonnull
  private SootClass.HierachyStep resolveDangling(@Nonnull IView view, @Nonnull JavaClassType cs) {

    return SootClass.surrogateBuilder().dangling(this.classSource, ClassType.Library);
  }

  @Nonnull
  private SootClass.SignatureStep resolveHierarchy(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {

    SootClass sootClass = (SootClass) view.getClass(cs).orElse(null);

    SootClass.HierachyStep danglingStep;

    if (sootClass == null
        || sootClass
            .resolvingLevel()
            .isLowerThan(
                ResolvingLevel
                    .DANGLING)) { // FIXME: [JMP] This expression is always `false`, because
      // `DANGLING` is the lowest level.
      // FIXME: do the setting stuff again...
      danglingStep = resolveDangling(view, cs);
    } else {
      danglingStep = SootClass.fromExisting(sootClass);
    }

    // Add super class
    JavaClassType mySuperClass =
        DefaultIdentifierFactory.getInstance().getClassType(AsmUtil.toQualifiedName(superName));

    // Add interfaces
    Set<JavaClassType> interfaces = new HashSet<>(AsmUtil.asmIdToSignature(this.interfaces));

    return danglingStep.hierachy(mySuperClass, interfaces, EnumSet.noneOf(Modifier.class), null);
  }

  private static Set<SootField> resolveFields(
      List<FieldNode> fieldNodes,
      IdentifierFactory identifierFactory,
      JavaClassType classSignature) {
    // FIXME: add support for annotation
    return fieldNodes.stream()
        .map(
            fieldNode -> {
              String fieldName = fieldNode.name;
              Type fieldType = AsmUtil.toJimpleType(fieldNode.desc);
              FieldSignature fieldSignature =
                  identifierFactory.getFieldSignature(fieldName, classSignature, fieldType);
              EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);

              return new SootField(fieldSignature, modifiers);
            })
        .collect(Collectors.toSet());
  }

  private static Stream<SootMethod> resolveMethods(
      List<MethodNode> methodNodes, IdentifierFactory identifierFactory, JavaClassType cs) {
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
                  identifierFactory.getMethodSignature(methodName, cs, retType, sigTypes);

              return SootMethod.builder()
                  .withSource(asmClassClassSourceContent)
                  .withSignature(methodSignature)
                  .withModifiers(modifiers)
                  .withThrownExceptions(exceptions)
                  .build();
            });
  }

  @Nonnull
  private SootClass.BodyStep resolveSignature(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {
    SootClass sootClass =
        (SootClass)
            view.getClass(cs)
                .orElseThrow(
                    () -> new AsmFrontendException(String.format("Cannot resolve class %s", cs)));

    SootClass.SignatureStep signatureStep;
    if (sootClass.resolvingLevel().isLowerThan(ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    } else {
      signatureStep = SootClass.fromExisting(sootClass);
    }

    Set<SootField> fields =
        resolveFields(this.fields, view.getIdentifierFactory(), sootClass.getType());
    Set<IMethod> methods =
        resolveMethods(this.methods, view.getIdentifierFactory(), cs).collect(Collectors.toSet());

    return signatureStep.signature(fields, methods);
  }

  @Override
  @Nonnull
  public Iterable<SootMethod> resolveMethods(@Nonnull JavaClassType signature)
      throws ResolveException {
    IdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
    return resolveMethods(methods, identifierFactory, signature).collect(Collectors.toSet());
  }

  @Override
  @Nonnull
  public Iterable<SootField> resolveFields(@Nonnull JavaClassType classSignature)
      throws ResolveException {
    IdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
    return resolveFields(fields, identifierFactory, classSignature);
  }

  @Nonnull
  private SootClass.Build resolveBody(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {

    SootClass sootClass =
        (SootClass)
            view.getClass(cs)
                .orElseThrow(
                    () -> new AsmFrontendException(String.format("Cannot resolve class %s", cs)));

    SootClass.BodyStep bodyStep =
        sootClass.resolvingLevel().isLowerThan(ResolvingLevel.SIGNATURES)
            ? resolveSignature(view, cs)
            : SootClass.fromExisting(sootClass);

    // TODO: resolve the method bodies
    return bodyStep.bodies("dummy");
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
