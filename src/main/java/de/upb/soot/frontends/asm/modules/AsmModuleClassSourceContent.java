package de.upb.soot.frontends.asm.modules;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.asm.AsmFrontendException;
import de.upb.soot.frontends.asm.AsmUtil;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Optional;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class AsmModuleClassSourceContent extends org.objectweb.asm.tree.ClassNode implements IClassSourceContent {

  private final ClassSource classSource;

  public AsmModuleClassSourceContent(ClassSource classSource) {
    super(Opcodes.ASM6);
    this.classSource = classSource;

    // FIXME: maybe delete class reading
    AsmUtil.initASMClassSource(classSource, this);
  }

  @Override
  public AbstractClass resolve(ResolvingLevel level, IView view) throws AsmFrontendException {
    JavaClassSignature cs = view.getSignatureFactory().getClassSignature(this.signature);
    SootModuleInfo.SootModuleInfoBuilder builder = null;
    if (module == null) {
      throw new IllegalArgumentException("This is not a module-info file");
    }

    // FIXME: currently ugly because, the orignal class is always re-resolved but never copied...
    switch (level) {
      case DANGLING:
        builder = (SootModuleInfo.SootModuleInfoBuilder) resolveDangling(view, cs);
        break;

      case HIERARCHY:
        builder = (SootModuleInfo.SootModuleInfoBuilder) resolveHierarchy(view, cs);
        break;

      // lower steps, don't make sense for modules ?

      case SIGNATURES:
        builder = (SootModuleInfo.SootModuleInfoBuilder) resolveSignature(view, cs);
        break;

      case BODIES:
        builder = (SootModuleInfo.SootModuleInfoBuilder) resolveBody(view, cs);
        break;
    }

    return builder.build();
  }

  private SootModuleInfo.HierachyStep resolveDangling(IView view, JavaClassSignature cs) {
    // sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    return SootModuleInfo.builder().dangling(view, this.classSource, null, this.module.name);
  }

  private SootModuleInfo.Build resolveHierarchy(IView view, JavaClassSignature cs) throws AsmFrontendException {
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    SootModuleInfo.HierachyStep hierachyStep;
    ArrayList<JavaClassSignature> providers = new ArrayList<>();
    ArrayList<SootModuleInfo.ModuleReference> requieres = new ArrayList<>();
    ArrayList<SootModuleInfo.PackageReference> exports = new ArrayList<>();
    ArrayList<SootModuleInfo.PackageReference> opens = new ArrayList<>();

    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      // FIXME: do the setting stuff again...
      hierachyStep = resolveDangling(view, cs);
    } else {
      hierachyStep = SootModuleInfo.fromExisting(sootClass);
    }

    { // add exports
      for (ModuleExportNode exportNode : module.exports) {
        Iterable<Optional<JavaClassSignature>> optionals = AsmUtil.asmIDToSignature(exportNode.modules, view);
        ArrayList<JavaClassSignature> modules = new ArrayList<>();
        for (Optional<JavaClassSignature> sootClassOptional : optionals) {
          if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
            modules.add(sootClassOptional.get());
          }
        }
        // FIXME: create constructs here
        // sootModuleInfo.addExport(exportNode.packaze, exportNode.access, modules);
        SootModuleInfo.PackageReference reference
            = new SootModuleInfo.PackageReference(exportNode.packaze, AsmUtil.getModifiers(exportNode.access), modules);
        opens.add(reference);
      }
    }

    {
      /// add opens
      for (ModuleOpenNode moduleOpenNode : module.opens) {
        Iterable<Optional<JavaClassSignature>> optionals = AsmUtil.asmIDToSignature(moduleOpenNode.modules, view);
        ArrayList<JavaClassSignature> modules = new ArrayList<>();
        for (Optional<JavaClassSignature> sootClassOptional : optionals) {
          if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
            modules.add(sootClassOptional.get());
          }
        }

        SootModuleInfo.PackageReference reference = new SootModuleInfo.PackageReference(moduleOpenNode.packaze,
            AsmUtil.getModifiers(moduleOpenNode.access), modules);
        opens.add(reference);
      }
    }

    {
      // add requies
      for (ModuleRequireNode moduleRequireNode : module.requires) {
        Optional<JavaClassSignature> sootClassOptional
            = AsmUtil.resolveAsmNameToClassSignature(moduleRequireNode.module, view);
        if (sootClassOptional.isPresent() && sootClassOptional.get().isModuleInfo()) {
          // sootModuleInfo.addRequire(sootClassOptional.get(), moduleRequireNode.access,
          // moduleRequireNode.version);
          SootModuleInfo.ModuleReference reference
              = new SootModuleInfo.ModuleReference(sootClassOptional.get(), AsmUtil.getModifiers(moduleRequireNode.access));
          requieres.add(reference);
        }
      }
    }

    {
      // add provides
      for (ModuleProvideNode moduleProvideNode : module.provides) {
        Optional<JavaClassSignature> serviceOptional
            = AsmUtil.resolveAsmNameToClassSignature(moduleProvideNode.service, view);
        Iterable<Optional<JavaClassSignature>> providersOptionals
            = AsmUtil.asmIDToSignature(moduleProvideNode.providers, view);
        for (Optional<JavaClassSignature> sootClassOptional : providersOptionals) {
          sootClassOptional.ifPresent(providers::add);
        }

        serviceOptional.ifPresent(providers::add);
      }
    }

    return hierachyStep.hierachy(requieres, exports, opens, providers);
  }

  private SootModuleInfo.Build resolveSignature(IView view, JavaClassSignature cs) throws AsmFrontendException {
    SootModuleInfo.Build signatureStep;
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    } else {
      signatureStep = SootModuleInfo.fromExisting(sootClass);
    }
    return signatureStep;
  }

  private SootModuleInfo.Build resolveBody(IView view, JavaClassSignature cs) throws AsmFrontendException {
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    SootModuleInfo.Build bodyStep;
    if (sootClass.resolvingLevel().isLoweverLevel(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      bodyStep = resolveSignature(view, cs);
    } else {
      bodyStep = SootModuleInfo.fromExisting(sootClass);
    }

    return bodyStep;
  }
}
