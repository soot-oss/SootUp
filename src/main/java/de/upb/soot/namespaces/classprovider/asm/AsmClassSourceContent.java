package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class AsmClassSourceContent extends org.objectweb.asm.tree.ClassNode
    implements de.upb.soot.namespaces.classprovider.ISourceContent {

  @Override
  public void resolve(de.upb.soot.core.ResolvingLevel level, de.upb.soot.views.IView view) {
    JavaClassSignature cs = view.getSignatureFacotry().getClassSignature(this.signature);
    SootClass.SootClassBuilder builder = null;
    //FIXME: currently ugly because, the orignal class is always re-resolved but never copied...
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
    //FIXME: should return the build sootclass
    //return builder.build();

    // everything is almost resolved at this tate
    // System.out.println(this.access);
    // System.out.println(this.methods);
    // create the soot class....
    // FIXME: or a soot module ... what to do with a module
    // what to do with a module

  }

  private SootClass.HierachyStep resolveDangling(IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    // sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    // what is whit the reftype?
    // sootClass.setRefType(null);

    // FIXME: innerclass?

    // FIXME: outerclass?

    // FIXME: annotations?

    // FIXME: module --- ?
    SootModuleInfo sootModuleInfo = null;
    dealWithModule(view, sootModuleInfo);
    //FIXME: what to use here
    return SootClass.builder().dangling(view, null,null);

  }

  private void dealWithModule(IView view, SootModuleInfo sootClass) {
    if (module != null) {
      if (sootClass instanceof SootModuleInfo) {
        SootModuleInfo sootModuleInfo = (SootModuleInfo) sootClass;
        {// add exports

          for (ModuleExportNode exportNode : module.exports) {
            Iterable<Optional<JavaClassSignature>> optionals = resolveAsmNamesToSootClasses(exportNode.modules, view);
            ArrayList<JavaClassSignature> modules = new ArrayList<>();
            for (Optional<JavaClassSignature> sootClassOptional : optionals) {
              if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
                modules.add(sootClassOptional.get());
              }
            }

            sootModuleInfo.addExport(exportNode.packaze, exportNode.access, modules);
          }
        }

        {
          /// add opens
          for (ModuleOpenNode moduleOpenNode : module.opens) {
            Iterable<Optional<JavaClassSignature>> optionals = resolveAsmNamesToSootClasses(moduleOpenNode.modules, view);
            ArrayList<JavaClassSignature> modules = new ArrayList<>();
            for (Optional<JavaClassSignature> sootClassOptional : optionals) {
              if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
                modules.add(sootClassOptional.get());
              }
            }

            sootModuleInfo.addOpen(moduleOpenNode.packaze, moduleOpenNode.access, modules);
          }

        }

        {
          // add requies
          for (ModuleRequireNode moduleRequireNode : module.requires) {
            Optional<JavaClassSignature> sootClassOptional = resolveAsmNameToClassSignature(moduleRequireNode.module, view);
            if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
              sootModuleInfo.addRequire(sootClassOptional.get(), moduleRequireNode.access, moduleRequireNode.version);

            }
          }

        }

        {
          // add provides
          for (ModuleProvideNode moduleProvideNode : module.provides) {
            Optional<JavaClassSignature> serviceOptional = resolveAsmNameToClassSignature(moduleProvideNode.service, view);
            Iterable<Optional<JavaClassSignature>> providersOptionals
                = resolveAsmNamesToSootClasses(moduleProvideNode.providers, view);
            ArrayList<JavaClassSignature> providers = new ArrayList<>();
            for (Optional<JavaClassSignature> sootClassOptional : providersOptionals) {
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

  private SootClass.SignatureStep resolveHierarchy(IView view, JavaClassSignature cs) {
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    Set<JavaClassSignature> interfaces = new HashSet<>();
    Optional<JavaClassSignature> mySuperCl = null;
    SootClass.HierachyStep danglingStep = null;

    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      // FIXME: do the setting stuff again...
      danglingStep = resolveDangling(view, cs);
    }
    else{
      danglingStep = SootClass.fromExisting(sootClass);
    }
    {
      // add super class

      Optional<JavaClassSignature> superClass = resolveAsmNameToClassSignature(superName, view);
      if (superClass.isPresent()) {
        mySuperCl = superClass;
      }
    }
    {
      // add the interfaces
      Iterable<Optional<JavaClassSignature>> optionals = resolveAsmNamesToSootClasses(this.interfaces, view);
      for (Optional<JavaClassSignature> interfaceClass : optionals) {

        if (interfaceClass.isPresent()) {
          interfaces.add(interfaceClass.get());
        }
      }
    }
    return danglingStep.hierachy(mySuperCl, interfaces, null, null);
  }

  private SootClass.BodyStep resolveSignature(de.upb.soot.views.IView view, JavaClassSignature cs) {
    SootClass.SignatureStep signatureStep = null;
    Set<SootMethod> methods = new HashSet<>();
    Set<SootField> fields = new HashSet<>();
    SootClass sootClass = (SootClass) view.getClass(cs).get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    }
    else{
      signatureStep = SootClass.fromExisting(sootClass);
    }

    {
      // add the fields
      for (FieldNode fieldNode : this.fields) {
        String fieldName = fieldNode.name;
        EnumSet<Modifier> modifiers = AsmUtil.getModifiers(fieldNode.access);
        Type fieldType = AsmUtil.toJimpleDesc(fieldNode.desc, view).get(0);
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
        Iterable<Optional<JavaClassSignature>> optionals = resolveAsmNamesToSootClasses(methodSource.exceptions, view);

        for (Optional<JavaClassSignature> excepetionClass : optionals) {

          if (excepetionClass.isPresent()) {
            exceptions.add(excepetionClass.get());
          }
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
    SootClass.BodyStep bodyStep = null;
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      bodyStep = resolveSignature(view, cs);
    }
    else{
      bodyStep = SootClass.fromExisting(sootClass);
    }
    // FIXME:
    return bodyStep.bodies("dummy");
  }

  private Iterable<Optional<JavaClassSignature>> resolveAsmNamesToSootClasses(Iterable<String> modules, IView view) {
    if (modules == null) {
      return java.util.Collections.emptyList();
    }
    return StreamSupport.stream(modules.spliterator(), false).map(p -> resolveAsmNameToClassSignature(p, view))
        .collect(java.util.stream.Collectors.toList());
  }

  // FIXME: double check optional here
  private Optional<JavaClassSignature> resolveAsmNameToClassSignature(String asmClassName, IView view) {
    String excepetionFQName = AsmUtil.toQualifiedName(asmClassName);
    de.upb.soot.signatures.JavaClassSignature classSignature
        = view.getSignatureFacotry().getClassSignature(excepetionFQName);
    return Optional.ofNullable(classSignature);
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
