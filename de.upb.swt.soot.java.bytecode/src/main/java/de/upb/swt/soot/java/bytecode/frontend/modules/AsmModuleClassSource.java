package de.upb.swt.soot.java.bytecode.frontend.modules;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.java.bytecode.frontend.AsmUtil;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.ModuleOpenNode;
import org.objectweb.asm.tree.ModuleProvideNode;
import org.objectweb.asm.tree.ModuleRequireNode;

public class AsmModuleClassSource extends AbstractClassSource {

  private final ModuleNode module;

  public AsmModuleClassSource(
      AnalysisInputLocation srcNamespace,
      Path sourcePath,
      JavaClassType classSignature,
      @Nonnull ModuleNode moduleNode) {
    super(srcNamespace, classSignature, sourcePath);
    this.module = moduleNode;
  }

  public AbstractClass buildClass(@Nonnull SourceType sourceType) {
    return new JavaModuleInfo(this, false);
  }

  public String getModuleName() {
    return this.module.name;
  }

  public Collection<JavaModuleInfo.ModuleReference> requires() {
    ArrayList<JavaModuleInfo.ModuleReference> requieres = new ArrayList<>();

    // add requies
    for (ModuleRequireNode moduleRequireNode : module.requires) {
      JavaClassType classSignature = AsmUtil.asmIDToSignature(moduleRequireNode.module);
      if (classSignature.isModuleInfo()) {
        // sootModuleInfo.addRequire(sootClassOptional.get(), moduleRequireNode.access,
        // moduleRequireNode.version);
        JavaModuleInfo.ModuleReference reference =
            new JavaModuleInfo.ModuleReference(
                classSignature, AsmUtil.getModifiers(moduleRequireNode.access));
        requieres.add(reference);
      }
    }
    return null;
  }

  public Collection<JavaModuleInfo.PackageReference> exports() {
    ArrayList<JavaModuleInfo.PackageReference> exports = new ArrayList<>();
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
      JavaModuleInfo.PackageReference reference =
          new JavaModuleInfo.PackageReference(
              exportNode.packaze, AsmUtil.getModifiers(exportNode.access), modules);
      exports.add(reference);
    }
    return exports;
  }

  public Collection<JavaModuleInfo.PackageReference> opens() {
    ArrayList<JavaModuleInfo.PackageReference> opens = new ArrayList<>();
    /// add opens
    for (ModuleOpenNode moduleOpenNode : module.opens) {
      Iterable<JavaClassType> optionals = AsmUtil.asmIdToSignature(moduleOpenNode.modules);
      ArrayList<JavaClassType> modules = new ArrayList<>();
      for (JavaClassType sootClassOptional : optionals) {
        if (sootClassOptional.isModuleInfo()) {
          modules.add(sootClassOptional);
        }
      }

      JavaModuleInfo.PackageReference reference =
          new JavaModuleInfo.PackageReference(
              moduleOpenNode.packaze, AsmUtil.getModifiers(moduleOpenNode.access), modules);
      opens.add(reference);
    }

    return opens;
  }

  // FIXME: does not look right here

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

  public Collection<JavaClassType> uses() {
    ArrayList<JavaClassType> uses = new ArrayList<>();
    // add provides
    for (String usedService : module.uses) {
      JavaClassType serviceSignature = AsmUtil.asmIDToSignature(usedService);
      uses.add(serviceSignature);
    }

    return uses;
  }

  public Set<Modifier> resolveModifiers() {
    EnumSet<Modifier> modifiers = AsmUtil.getModifiers(module.access);
    return modifiers;
  }

  public Position resolvePosition() {
    return NoPositionInformation.getInstance();
  }
}
