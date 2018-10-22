package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.tree.FieldNode;

public class AsmClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements de.upb.soot.namespaces.classprovider.ISourceContent {

  @Override
  public void resolve(de.upb.soot.core.ResolvingLevel level, de.upb.soot.views.IView view) {
    JavaClassSignature cs = view.getSignatureFacotry().getClassSignature(this.signature);
    switch (level) {
      case DANGLING:
        resolveDangling(view, cs);
        break;

      case HIERARCHY:
        resolveHierarchy(view, cs);
        break;

      case SIGNATURES:
        resolveSignature(view, cs);
        break;

      case BODIES:
        resolveBody(view, cs);
        break;
    }

    // everything is almost resolved at this tate
    // System.out.println(this.access);
    // System.out.println(this.methods);
    // create the soot class....
    // FIXME: or a soot module ... what to do with a module
    // what to do with a module

  }

  private void resolveDangling(IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    // sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    // what is whit the reftype?
    // sootClass.setRefType(null);

    // FIXME: innerclass?

    // FIXME: outerclass?

    // FIXME: annotations?
  }

  private void resolveHierarchy(IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      resolveDangling(view, cs);
    }
    {
      // add super class

      String superClassName = AsmUtil.toQualifiedName(superName);
      de.upb.soot.signatures.JavaClassSignature superSignature = view.getSignatureFacotry().getClassSignature(superClassName);
      Optional<AbstractClass> superClass = view.getClass(superSignature);
      if (superClass.isPresent()) {
        // sootClass.setSuperclass(superClass.get());
      }
    }
    {
      // add the interfaces

      for (String interfaceName : this.interfaces) {
        String fqInterfaceName = AsmUtil.toQualifiedName(interfaceName);
        de.upb.soot.signatures.JavaClassSignature interfaceSig = view.getSignatureFacotry().getClassSignature(fqInterfaceName);
        Optional<AbstractClass> interfaceClass = view.getClass(interfaceSig);
        if (interfaceClass.isPresent()) {
          // sootClass.setSuperclass(interfaceClass.get());
        }
      }
    }

  }

  private void resolveSignature(de.upb.soot.views.IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      resolveHierarchy(view, cs);
    }

    {
      // add the fields
      for (FieldNode fieldNode : this.fields) {
        String fieldName = fieldNode.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);
        Type fieldType = AsmUtil.toJimpleDesc(fieldNode.desc, view).get(0);
        SootField sootField = new SootField(view, null, null, modifiers);
      }

    }

    { // add methods
      for (org.objectweb.asm.tree.MethodNode methodSource : this.methods) {
        String methodName = methodSource.name;

        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(methodSource.access);
        List<Type> sigTypes = AsmUtil.toJimpleDesc(methodSource.desc, view);
        Type retType = sigTypes.remove(sigTypes.size() - 1);
        List<SootClass> exceptions = new ArrayList<>();
        for (String exceptionName : methodSource.exceptions) {
          String excepetionFQName = AsmUtil.toQualifiedName(exceptionName);
          de.upb.soot.signatures.JavaClassSignature exceptionSig
              = view.getSignatureFacotry().getClassSignature(excepetionFQName);
          Optional<AbstractClass> excepetionClass = view.getClass(exceptionSig);
          if (excepetionClass.isPresent()) {
            exceptions.add((SootClass) excepetionClass.get());
          }
        }

        de.upb.soot.core.SootMethod sootMethod
            = new de.upb.soot.core.SootMethod(view, null, null, null, modifiers, null, null);
        // sootClass.addMethod(sootMethod);
      }
    }

  }

  private void resolveBody(de.upb.soot.views.IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      resolveSignature(view, cs);
    }
  }

  @Override
  public org.objectweb.asm.ModuleVisitor visitModule(String name, int access, String version) {
    // FIXME: do something here??
    return super.visitModule(name, access, version);
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
