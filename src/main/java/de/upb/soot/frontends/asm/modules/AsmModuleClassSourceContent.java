package de.upb.soot.frontends.asm.modules;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.asm.AsmFrontendException;
import de.upb.soot.frontends.asm.AsmUtil;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.IView;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class AsmModuleClassSourceContent extends ClassNode implements IClassSourceContent {

  private final ClassSource classSource;

  public AsmModuleClassSourceContent(@Nonnull ClassSource classSource) {
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

  @Nonnull
  private SootModuleInfo.HierachyStep resolveDangling(
      @Nonnull IView view, @Nonnull JavaClassType cs) {
    // sootClass.setModifiers(AsmUtil.getModifiers(access & ~org.objectweb.asm.Opcodes.ACC_SUPER));
    return SootModuleInfo.builder().dangling(view, this.classSource, null, this.module.name);
  }

  @Nonnull
  private SootModuleInfo.Build resolveHierarchy(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    SootModuleInfo.HierachyStep hierachyStep;
    ArrayList<JavaClassType> providers = new ArrayList<>();
    ArrayList<SootModuleInfo.ModuleReference> requieres = new ArrayList<>();
    ArrayList<SootModuleInfo.PackageReference> exports = new ArrayList<>();
    ArrayList<SootModuleInfo.PackageReference> opens = new ArrayList<>();

    if (sootClass.resolvingLevel().isLowerThan(de.upb.soot.core.ResolvingLevel.DANGLING)) {
      // FIXME: do the setting stuff again...
      hierachyStep = resolveDangling(view, cs);
    } else {
      hierachyStep = SootModuleInfo.fromExisting(sootClass);
    }

    { // add exports
      for (ModuleExportNode exportNode : module.exports) {
        Iterable<JavaClassType> optionals = AsmUtil.asmIdToSignature(exportNode.modules);
        ArrayList<JavaClassType> modules = new ArrayList<>();
        for (JavaClassType sootClassOptional : optionals) {
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
        Iterable<JavaClassType> optionals = AsmUtil.asmIdToSignature(moduleOpenNode.modules);
        ArrayList<JavaClassType> modules = new ArrayList<>();
        for (JavaClassType sootClassOptional : optionals) {
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
        JavaClassType classSignature =
            view.getIdentifierFactory().getClassType(AsmUtil.toQualifiedName(moduleRequireNode.module));
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
        JavaClassType serviceSignature =
            view.getIdentifierFactory().getClassType(AsmUtil.toQualifiedName(moduleProvideNode.service));
        Iterable<JavaClassType> providersSignatures =
            AsmUtil.asmIdToSignature(moduleProvideNode.providers);
        for (JavaClassType sootClassSignature : providersSignatures) {
          providers.add(sootClassSignature);
        }

        providers.add(serviceSignature);
      }
    }

    return hierachyStep.hierachy(requieres, exports, opens, providers);
  }

  @Nonnull
  private SootModuleInfo.Build resolveSignature(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {
    SootModuleInfo.Build signatureStep;
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    if (sootClass.resolvingLevel().isLowerThan(de.upb.soot.core.ResolvingLevel.HIERARCHY)) {
      signatureStep = resolveHierarchy(view, cs);
    } else {
      signatureStep = SootModuleInfo.fromExisting(sootClass);
    }
    return signatureStep;
  }

  @Nonnull
  private SootModuleInfo.Build resolveBody(@Nonnull IView view, @Nonnull JavaClassType cs)
      throws AsmFrontendException {
    Optional<AbstractClass> aClass = view.getClass(cs);
    if (!aClass.isPresent()) {
      throw new AsmFrontendException(String.format("Cannot resolve class %s", cs));
    }
    SootModuleInfo sootClass = (SootModuleInfo) aClass.get();
    SootModuleInfo.Build bodyStep;
    if (sootClass.resolvingLevel().isLowerThan(de.upb.soot.core.ResolvingLevel.SIGNATURES)) {
      bodyStep = resolveSignature(view, cs);
    } else {
      bodyStep = SootModuleInfo.fromExisting(sootClass);
    }

    return bodyStep;
  }
}
