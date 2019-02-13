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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;

public class AsmModuleClassSourceContent extends ClassNode
    implements IClassSourceContent {

  private final ClassSource classSource;

  public AsmModuleClassSourceContent(@Nonnull ClassSource classSource) {
    super(Opcodes.ASM6);
    this.classSource = classSource;

    // FIXME: maybe delete class reading
    AsmUtil.initASMClassSource(classSource, this);
  }

  @Override
  public @Nonnull AbstractClass resolve(@Nonnull ResolvingLevel level, @Nonnull IView view) throws AsmFrontendException {
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

  private @Nonnull SootModuleInfo.HierachyStep resolveDangling(@Nonnull IView view, @Nonnull JavaClassSignature cs) {
    // sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    return SootModuleInfo.builder().dangling(view, this.classSource, null, this.module.name);
  }

  private @Nonnull SootModuleInfo.Build resolveHierarchy(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
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
        Iterable<JavaClassSignature> optionals =
            AsmUtil.asmIDToSignature(exportNode.modules, view);
        ArrayList<JavaClassSignature> modules = new ArrayList<>();
        for (JavaClassSignature sootClassOptional : optionals) {
          if (sootClassOptional.isModuleInfo()) {
            modules.add(sootClassOptional);
          }
        }
        // FIXME: create constructs here
        // sootModuleInfo.addExport(exportNode.packaze, exportNode.access, modules);
        SootModuleInfo.PackageReference reference =
            new SootModuleInfo.PackageReference(
                exportNode.packaze, AsmUtil.getModifiers(exportNode.access), modules);
        opens.add(reference);
      }
    }

    {
      /// add opens
      for (ModuleOpenNode moduleOpenNode : module.opens) {
        Iterable<JavaClassSignature> optionals =
            AsmUtil.asmIDToSignature(moduleOpenNode.modules, view);
        ArrayList<JavaClassSignature> modules = new ArrayList<>();
        for (JavaClassSignature sootClassOptional : optionals) {
          if (sootClassOptional.isModuleInfo()) {
            modules.add(sootClassOptional);
          }
        }

        SootModuleInfo.PackageReference reference =
            new SootModuleInfo.PackageReference(
                moduleOpenNode.packaze, AsmUtil.getModifiers(moduleOpenNode.access), modules);
        opens.add(reference);
      }
    }

    {
      // add requies
      for (ModuleRequireNode moduleRequireNode : module.requires) {
        JavaClassSignature classSignature =
            view.getSignatureFactory()
                .getClassSignature(AsmUtil.toQualifiedName(moduleRequireNode.module));
        if (classSignature.isModuleInfo()) {
          // sootModuleInfo.addRequire(sootClassOptional.get(), moduleRequireNode.access,
          // moduleRequireNode.version);
          SootModuleInfo.ModuleReference reference =
              new SootModuleInfo.ModuleReference(
                  classSignature, AsmUtil.getModifiers(moduleRequireNode.access));
          requieres.add(reference);
        }
      }
    }

    {
      // add provides
      for (ModuleProvideNode moduleProvideNode : module.provides) {
        JavaClassSignature serviceSignature =
            view.getSignatureFactory()
                .getClassSignature(AsmUtil.toQualifiedName(moduleProvideNode.service));
        Iterable<JavaClassSignature> providersSignatures =
            AsmUtil.asmIDToSignature(moduleProvideNode.providers, view);
        for (JavaClassSignature sootClassSignature : providersSignatures) {
          providers.add(sootClassSignature);
        }

        providers.add(serviceSignature);
      }
    }

    return hierachyStep.hierachy(requieres, exports, opens, providers);
  }

  private @Nonnull SootModuleInfo.Build resolveSignature(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
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

  private @Nonnull SootModuleInfo.Build resolveBody(@Nonnull IView view, @Nonnull JavaClassSignature cs)
      throws AsmFrontendException {
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
