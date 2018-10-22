package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

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

    // FIXME: module --- ?
    dealWithModule(view, sootClass);

  }

  private void dealWithModule(IView view, SootClass sootClass) {
    if (module != null) {
      if (sootClass instanceof SootModuleInfo) {
        SootModuleInfo sootModuleInfo = (SootModuleInfo) sootClass;
        {// add exports

          for (ModuleExportNode exportNode : module.exports) {
            Iterable<Optional<SootClass>> optionals = resolveAsmNamesToSootClasses(exportNode.modules, view);
            ArrayList<SootModuleInfo> modules = new ArrayList<>();
            for (Optional<SootClass> sootClassOptional : optionals) {
              if (sootClassOptional.isPresent() && sootClassOptional.get() instanceof SootModuleInfo) {
                modules.add((SootModuleInfo) sootClassOptional.get());
              }
            }

            sootModuleInfo.addExport(exportNode.packaze, exportNode.access, modules);
          }
        }

        {
          /// add opens
          for (ModuleOpenNode moduleOpenNode : module.opens) {
            Iterable<Optional<SootClass>> optionals = resolveAsmNamesToSootClasses(moduleOpenNode.modules, view);
            ArrayList<SootModuleInfo> modules = new ArrayList<>();
            for (Optional<SootClass> sootClassOptional : optionals) {
              if (sootClassOptional.isPresent() && sootClassOptional.get() instanceof SootModuleInfo) {
                modules.add((SootModuleInfo) sootClassOptional.get());
              }
            }

            sootModuleInfo.addOpen(moduleOpenNode.packaze, moduleOpenNode.access, modules);
          }

        }

        {
          // add requies
          for (ModuleRequireNode moduleRequireNode : module.requires) {
            Optional<SootClass> sootClassOptional = resolveAsmNameToSootClass(moduleRequireNode.module, view);
            if (sootClassOptional.isPresent() && sootClassOptional.get() instanceof SootModuleInfo) {
              sootModuleInfo.addRequire((SootModuleInfo) sootClassOptional.get(), moduleRequireNode.access,
                  moduleRequireNode.version);

            }
          }

        }

        {
          // add provides
          for (ModuleProvideNode moduleProvideNode : module.provides) {
            Optional<SootClass> serviceOptional = resolveAsmNameToSootClass(moduleProvideNode.service, view);
            Iterable<Optional<SootClass>> providersOptionals
                = resolveAsmNamesToSootClasses(moduleProvideNode.providers, view);
            ArrayList<SootClass> providers = new ArrayList<>();
            for (Optional<SootClass> sootClassOptional : providersOptionals) {
              if (sootClassOptional.isPresent()) {
                providers.add(sootClassOptional.get());
              }
            }

            if (serviceOptional.isPresent()) {
              // FIXME: must service be resolved

              sootModuleInfo.addProvide(moduleProvideNode.service, providers);

            }
          }
        }

      }
    }
  }

  private void resolveHierarchy(IView view, SootClass sootClass) {
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      resolveDangling(view, sootClass);
    }
    {
      // add super class

      Optional<SootClass> superClass = resolveAsmNameToSootClass(superName, view);
      if (superClass.isPresent()) {
        sootClass.setSuperclass(superClass.get());
      }
    }
    {
      // add the interfaces
      Iterable<Optional<SootClass>> optionals = resolveAsmNamesToSootClasses(this.interfaces, view);
      for (Optional<SootClass> interfaceClass : optionals) {

        if (interfaceClass.isPresent()) {
          sootClass.addInterface(interfaceClass.get());
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
        Iterable<Optional<SootClass>> optionals = resolveAsmNamesToSootClasses(methodSource.exceptions, view);

        for (Optional<SootClass> excepetionClass : optionals) {

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

  private Iterable<Optional<SootClass>> resolveAsmNamesToSootClasses(Iterable<String> modules, IView view) {
    if (modules == null) {
      return java.util.Collections.emptyList();
    }
    return StreamSupport.stream(modules.spliterator(), false).map(p -> resolveAsmNameToSootClass(p, view))
        .collect(java.util.stream.Collectors.toList());
  }

  private Optional<SootClass> resolveAsmNameToSootClass(String asmClassName, IView view) {
    String excepetionFQName = AsmUtil.toQualifiedName(asmClassName);
    de.upb.soot.signatures.ClassSignature classSignature = view.getSignatureFacotry().getClassSignature(excepetionFQName);
    Optional<SootClass> sootClass = view.getSootClass(classSignature);
    return sootClass;
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
