package de.upb.soot.frontends.asm.modules;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.ModuleClassSource;
import de.upb.soot.frontends.asm.AsmUtil;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class AsmModuleClassSource extends ModuleClassSource {

  private final ModuleNode module;

  public AsmModuleClassSource(
      INamespace srcNamespace,
      Path sourcePath,
      JavaClassType classSignature,
      @Nonnull ModuleNode moduleNode) {
    super(srcNamespace, sourcePath, classSignature);
    this.module = moduleNode;
  }

  @Override
  public String getModuleName() {
    return this.module.name;
  }

  @Override
  public Collection<SootModuleInfo.ModuleReference> requires() {
    ArrayList<SootModuleInfo.ModuleReference> requieres = new ArrayList<>();

    // add requies
    for (ModuleRequireNode moduleRequireNode : module.requires) {
      JavaClassType classSignature = AsmUtil.asmIDToSignature(moduleRequireNode.module);
      if (classSignature.isModuleInfo()) {
        // sootModuleInfo.addRequire(sootClassOptional.get(), moduleRequireNode.access,
        // moduleRequireNode.version);
        SootModuleInfo.ModuleReference reference =
            new SootModuleInfo.ModuleReference(
                classSignature, AsmUtil.getModifiers(moduleRequireNode.access));
        requieres.add(reference);
      }
    }
    return null;
  }

  @Override
  public Collection<SootModuleInfo.PackageReference> exports() {
    ArrayList<SootModuleInfo.PackageReference> exports = new ArrayList<>();
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
      exports.add(reference);
    }
    return exports;
  }

  @Override
  public Collection<SootModuleInfo.PackageReference> opens() {
    ArrayList<SootModuleInfo.PackageReference> opens = new ArrayList<>();
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

    return opens;
  }

  // FIXME: does not look right here

  @Override
  public Collection<JavaClassType> provides() {
    ArrayList<JavaClassType> providers = new ArrayList<>();
    // add provides
    for (ModuleProvideNode moduleProvideNode : module.provides) {
      JavaClassType serviceSignature = AsmUtil.asmIDToSignature(moduleProvideNode.service);
      Iterable<JavaClassType> providersSignatures =
          AsmUtil.asmIdToSignature(moduleProvideNode.providers);
      for (JavaClassType sootClassSignature : providersSignatures) {
        providers.add(sootClassSignature);
      }

      providers.add(serviceSignature);
    }

    return providers;
  }

  @Override
  public Collection<JavaClassType> uses() {
    ArrayList<JavaClassType> uses = new ArrayList<>();
    // add provides
    for (String usedService : module.uses) {
      JavaClassType serviceSignature = AsmUtil.asmIDToSignature(usedService);
      uses.add(serviceSignature);
    }

    return uses;
  }

  @Override
  public Set<Modifier> resolveModifiers() {
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(module.access);
    return modifiers;
  }

  @Override
  public CAstSourcePositionMap.Position resolvePosition() {
    return null;
  }
}
