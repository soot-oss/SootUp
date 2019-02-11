package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.views.IView;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AsmClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements de.upb.soot.namespaces.classprovider.ISourceContent {

  private AbstractClassSource classSource;

  public AsmClassSourceContent(AbstractClassSource classSource) {
    super(Opcodes.ASM6);
    this.classSource = classSource;
    //FIXME: maybe delete class reading
    AsmUtil.initASMClassSource(classSource, this);

  }

  @Override
  public AbstractClass resolve(ResolvingLevel level, IView view) {
    JavaClassSignature cs = view.getSignatureFactory().getClassSignature(this.signature);
    SootClass.SootClassBuilder builder = null;
    // FIXME: currently ugly because, the orignal class is always re-resolved but never copied...
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

    // FIXME: should return the build sootclass?
    // return builder.build();

    // everything is almost resolved at this tate
    // System.out.println(this.access);
    // System.out.println(this.methods);
    // create the soot class....
    // what to do with a module

    return builder.build();
  }

  private SootClass.HierachyStep resolveDangling(IView view, JavaClassSignature cs) {

    return SootClass.builder().dangling(view, this.classSource, null);

  }

  private SootClass.SignatureStep resolveHierarchy(IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    Set<JavaClassSignature> interfaces = new HashSet<>();
    JavaClassSignature mySuperCl = null;
    SootClass.HierachyStep danglingStep;

    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      // FIXME: do the setting stuff again...
      danglingStep = resolveDangling(view, cs);
    } else {
      danglingStep = SootClass.fromExisting(sootClass);
    }
    {
      // add super class

      mySuperCl = AsmUtil.resolveAsmNameToClassSignature(superName, view).orElse(null);
    }
    {
      // add the interfaces
      Iterable<Optional<JavaClassSignature>> optionals = AsmUtil.asmIDToSignature(this.interfaces, view);
      for (Optional<JavaClassSignature> interfaceClass : optionals) {

        interfaceClass.ifPresent(interfaces::add);
      }
    }
    return danglingStep.hierachy(mySuperCl, interfaces, null, null);
  }

  private SootClass.BodyStep resolveSignature(de.upb.soot.views.IView view, JavaClassSignature cs) {
    SootClass.SignatureStep signatureStep;
    Set<IMethod> methods = new HashSet<>();
    Set<SootField> fields = new HashSet<>();
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    } else {
      signatureStep = SootClass.fromExisting(sootClass);
    }

    {
      // add the fields
      for (FieldNode fieldNode : this.fields) {
        String fieldName = fieldNode.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);
        Type fieldType = AsmUtil.toJimpleDesc(fieldNode.desc, view).get(0);
        // FIXME: fieldname??
        SootField sootField = new SootField(view, null, null, null, modifiers);
        fields.add(sootField);
      }

    }

    { // add methods
      for (org.objectweb.asm.tree.MethodNode methodSource : this.methods) {
        String methodName = methodSource.name;

        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
        List<Type> sigTypes = AsmUtil.toJimpleDesc(methodSource.desc, view);
        Type retType = sigTypes.remove(sigTypes.size() - 1);
        List<JavaClassSignature> exceptions = new ArrayList<>();
        Iterable<Optional<JavaClassSignature>> optionals = AsmUtil.asmIDToSignature(methodSource.exceptions, view);

        for (Optional<JavaClassSignature> excepetionClass : optionals) {
          excepetionClass.ifPresent(exceptions::add);
        }

        de.upb.soot.core.SootMethod sootMethod
            = new de.upb.soot.core.SootMethod(view, null, null, null, null, modifiers, null, null);
        // sootClass.addMethod(sootMethod);
        methods.add(sootMethod);
      }
    }
    return signatureStep.signature(fields, methods);
  }

  private SootClass.Build resolveBody(de.upb.soot.views.IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    SootClass.BodyStep bodyStep;
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      bodyStep = resolveSignature(view, cs);
    } else {
      bodyStep = SootClass.fromExisting(sootClass);
    }
    // FIXME:
    return bodyStep.bodies("dummy");
  }

  @Override
  public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String desc, String signature,
      String[] exceptions) {

    de.upb.soot.namespaces.classprovider.asm.AsmMethodSource mn
        = new AsmMethodSource(null, access, name, desc, signature, exceptions);
    methods.add(mn);
    return mn;
  }
}
