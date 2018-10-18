package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.objectweb.asm.tree.FieldNode;

public class AsmClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements de.upb.soot.namespaces.classprovider.ISourceContent {
  @Override
  public void resolve(de.upb.soot.core.ResolvingLevel level, de.upb.soot.views.IView view,
      de.upb.soot.core.SootClass sootClass) {

    switch (level) {
      case DANGLING:
        resolveDangling(view, sootClass);
        break;

      case HIERARCHY:
        resolveHierarchy(view, sootClass);
        break;

      case SIGNATURES:
        resolveSignature(view, sootClass);
        break;

      case BODIES:
        resolveBody(view, sootClass);
        break;
    }

    // everything is almost resolved at this tate
    // System.out.println(this.access);
    // System.out.println(this.methods);
    // create the soot class....
    // FIXME: or a soot module ... what to do with a module
    // what to do with a module

  }

  private void resolveDangling(IView view, SootClass sootClass) {
    sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    // what is whit the reftype?
    sootClass.setRefType(null);

    // FIXME: innerclass?

    // FIXME: outerclass?

    // FIXME: annotations?
  }

  private void resolveHierarchy(IView view, SootClass sootClass) {
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      resolveDangling(view, sootClass);
    }
    {
      // add super class

      String superClassName = AsmUtil.toQualifiedName(superName);
      de.upb.soot.signatures.ClassSignature superSignature = view.getSignatureFacotry().getClassSignature(superClassName);
      Optional<SootClass> superClass = view.getSootClass(superSignature);
      if (superClass.isPresent()) {
        sootClass.setSuperclass(superClass.get());
      }
    }
    {
      // add the interfaces

      for (String interfaceName : this.interfaces) {
        String fqInterfaceName = AsmUtil.toQualifiedName(interfaceName);
        de.upb.soot.signatures.ClassSignature interfaceSig = view.getSignatureFacotry().getClassSignature(fqInterfaceName);
        Optional<SootClass> interfaceClass = view.getSootClass(interfaceSig);
        if (interfaceClass.isPresent()) {
          sootClass.setSuperclass(interfaceClass.get());
        }
      }
    }

  }

  private void resolveSignature(de.upb.soot.views.IView view, de.upb.soot.core.SootClass sootClass) {
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      resolveHierarchy(view, sootClass);
    }

    {
      // add the fields
      for (FieldNode fieldNode : this.fields) {
        String fieldName = fieldNode.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);
        Type fieldType = AsmUtil.toJimpleDesc(fieldNode.desc, view).get(0);
        SootField sootField = new SootField(view, name, fieldType, modifiers);
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
          de.upb.soot.signatures.ClassSignature exceptionSig
              = view.getSignatureFacotry().getClassSignature(excepetionFQName);
          Optional<SootClass> excepetionClass = view.getSootClass(exceptionSig);
          if (excepetionClass.isPresent()) {
            exceptions.add(excepetionClass.get());
          }
        }

        de.upb.soot.core.SootMethod sootMethod
            = new de.upb.soot.core.SootMethod(view, methodName, sigTypes, retType, modifiers, exceptions);
        sootMethod.setSource((AsmMethodSource) methodSource);
        sootClass.addMethod(sootMethod);
      }
    }

  }

  private void resolveBody(de.upb.soot.views.IView view, de.upb.soot.core.SootClass sootClass) {
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      resolveSignature(view, sootClass);
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
