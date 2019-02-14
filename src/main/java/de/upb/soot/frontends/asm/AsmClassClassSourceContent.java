package de.upb.soot.frontends.asm;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.TypeSignature;
import de.upb.soot.views.IView;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class AsmClassClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements IClassSourceContent {

  private final ClassSource classSource;

  public AsmClassClassSourceContent(@Nonnull ClassSource classSource) {
    super(AsmUtil.SUPPORTED_ASM_OPCODE);
    this.classSource = classSource;
    // FIXME: maybe delete class reading
    AsmUtil.initASMClassSource(classSource, this);
  }

  @Override
  public @Nonnull AbstractClass resolve(@Nonnull ResolvingLevel level, @Nonnull IView view) throws AsmFrontendException {
    JavaClassSignature cs = view.getSignatureFactory().getClassSignature(this.signature);
    SootClass.SootClassBuilder builder = null;
    // FIXME: currently ugly because, the original class is always re-resolved but never copied...
    switch (level) {
      case DANGLING:
        builder = (SootClass.SootClassBuilder) resolveDangling(view, cs);
        break;

      case HIERARCHY:
        builder = (SootClass.SootClassBuilder) resolveHierarchy(view, cs);
        break;

      case SIGNATURES:
        builder = (SootClass.SootClassBuilder) resolveSignature(view, cs);
        break;

      case BODIES:
        builder = (SootClass.SootClassBuilder) resolveBody(view, cs);
        break;
    }

    return builder.build();
  }

  // FIXME: Parameter `cs` is unused
  private @Nonnull SootClass.HierachyStep resolveDangling(@Nonnull IView view, @Nonnull JavaClassSignature cs) {

    return SootClass.builder().dangling(view, this.classSource, null);
  }

  private @Nonnull SootClass.SignatureStep resolveHierarchy(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
    SootClass sootClass = (SootClass) view.getClass(cs).orElseThrow(() -> new AsmFrontendException(String.format("Cannot resolve class %s", cs)));
    Set<JavaClassSignature> interfaces = new HashSet<>();
    Optional<JavaClassSignature> mySuperCl = Optional.empty();
    SootClass.HierachyStep danglingStep;

    if (sootClass.resolvingLevel().isLoweverLevel(ResolvingLevel.DANGLING)) {
      // FIXME: do the setting stuff again...
      danglingStep = resolveDangling(view, cs);
    } else {
      danglingStep = SootClass.fromExisting(sootClass);
    }
    {
      // add super class
      JavaClassSignature superClass =
          view.getSignatureFactory().getClassSignature(AsmUtil.toQualifiedName(superName));
      mySuperCl = Optional.of(superClass);
    }
    {
      // add the interfaces
      Iterable<JavaClassSignature> interfaceSignatures =
          AsmUtil.asmIDToSignature(this.interfaces, view);
      for (JavaClassSignature interfaceSig : interfaceSignatures) {

        interfaces.add(interfaceSig);
      }
    }
    return danglingStep.hierachy(mySuperCl, interfaces, null, Optional.empty());
  }

  private @Nonnull SootClass.BodyStep resolveSignature(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
    SootClass.SignatureStep signatureStep;
    Set<IMethod> methods = new HashSet<>();
    Set<SootField> fields = new HashSet<>();
    SootClass sootClass = (SootClass) view.getClass(cs).orElseThrow(() -> new AsmFrontendException(String.format("Cannot resolve class %s", cs)));
    if (sootClass.resolvingLevel().isLoweverLevel(ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    } else {
      signatureStep = SootClass.fromExisting(sootClass);
    }

    {
      // FIXME: add support for annotation
      // add the fields
      for (FieldNode fieldNode : this.fields) {
        String fieldName = fieldNode.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);
        TypeSignature fieldType = AsmUtil.toJimpleType(view, fieldNode.desc);
        FieldSignature fieldSignature =
            view.getSignatureFactory()
                .getFieldSignature(fieldName, sootClass.getSignature(), fieldType);
        SootField sootField =
            new SootField(view, sootClass.getSignature(), fieldSignature, fieldType, modifiers);
        fields.add(sootField);
      }
    }

    { // add methods
      for (org.objectweb.asm.tree.MethodNode methodSource : this.methods) {

        if (!(methodSource instanceof AsmMethodSourceContent)) {
          throw new AsmFrontendException(
              String.format("Failed to create Method Signature %s", methodSource));
        }
        AsmMethodSourceContent asmClassClassSourceContent = (AsmMethodSourceContent) methodSource;

        List<JavaClassSignature> exceptions = new ArrayList<>();
        Iterable<JavaClassSignature> exceptionsSignatures =
            AsmUtil.asmIDToSignature(methodSource.exceptions, view);

        for (JavaClassSignature exceptionSig : exceptionsSignatures) {
          exceptions.add(exceptionSig);
        }
        String methodName = methodSource.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
        List<TypeSignature> sigTypes = AsmUtil.toJimpleSignatureDesc(methodSource.desc, view);
        TypeSignature retType = sigTypes.remove(sigTypes.size() - 1);

        MethodSignature methodSignature =
            view.getSignatureFactory()
                .getMethodSignature(methodName, sootClass.getSignature(), retType, sigTypes);

        SootMethod sootMethod =
            new SootMethod(
                view,
                sootClass.getSignature(),
                asmClassClassSourceContent,
                methodSignature,
                modifiers,
                exceptions,
                null);
        methods.add(sootMethod);
      }
    }
    return signatureStep.signature(fields, methods);
  }

  private @Nonnull SootClass.Build resolveBody(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
    SootClass sootClass = (SootClass) view.getClass(cs).orElseThrow(() -> new AsmFrontendException(String.format("Cannot resolve class %s", cs)));
    SootClass.BodyStep bodyStep;
    if (sootClass.resolvingLevel().isLoweverLevel(ResolvingLevel.SIGNATURES)) {
      bodyStep = resolveSignature(view, cs);
    } else {
      bodyStep = SootClass.fromExisting(sootClass);
    }
    // TODO: resolve the method bodies
    return bodyStep.bodies("dummy");
  }

  @Override
  public @Nonnull MethodVisitor visitMethod(
      int access, @Nonnull String name, @Nonnull String desc, @Nonnull String signature, @Nonnull String[] exceptions) {

    AsmMethodSourceContent mn =
        new AsmMethodSourceContent(access, name, desc, signature, exceptions);
    methods.add(mn);
    return mn;
  }
}
