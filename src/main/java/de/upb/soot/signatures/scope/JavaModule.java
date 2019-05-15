package de.upb.soot.signatures.scope;

import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.types.JavaClassTypeScope;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class JavaModule implements JavaClassTypeScope {

  // FIXME: exports everything and requires everyhing
  // special Module to mean "all unnamed modules"
  public static final JavaModule UNNAMED_MODULE = new JavaModule();

  // special Module to mean "everyone"
  private static final JavaModule EVERYONE_MODULE = new JavaModule();

  @Nullable private final SootModuleInfo sootModuleInfo;

  ModuleSignature moduleSignature;
  private final Collection<String> modulePackages;
  private final boolean isAutomaticModule;

  private final JavaModuleGraph moduleGraph;

  private JavaModule() {
    this(null, Collections.emptyList(), false, null);
  }

  public JavaModule(
      @Nullable SootModuleInfo sootModuleInfo,
      Collection<String> packages,
      boolean isAutomaticModule,
      JavaModuleGraph moduleGraph) {
    this.sootModuleInfo = sootModuleInfo;
    this.modulePackages = packages;
    this.isAutomaticModule = isAutomaticModule;
    this.moduleGraph = moduleGraph;
  }

  @Override
  public JavaClassTypeScope getScope() {
    return this;
  }

  public Set<String> getPublicExportedPackages() {
    Set<String> publicExportedPackages = new HashSet<>();
    for (String packaze : modulePackages) {
      if (this.exportsPackage(packaze, EVERYONE_MODULE)) {
        publicExportedPackages.add(packaze);
      }
    }
    return publicExportedPackages;
  }

  public Set<String> getPublicOpenedPackages() {
    Set<String> publicOpenedPackages = new HashSet<>();
    for (String packaze : modulePackages) {
      if (this.opensPackage(packaze, EVERYONE_MODULE)) {
        publicOpenedPackages.add(packaze);
      }
    }
    return publicOpenedPackages;
  }

  public boolean opensPackage(String packaze, ModuleSignature toModule) {
    Optional<JavaModule> module = this.moduleGraph.findModule(toModule);
    return module.map(m -> opensPackage(packaze, m)).orElse(false);
  }

  public boolean exportsPackage(String packaze, ModuleSignature toModule) {
    Optional<JavaModule> module = this.moduleGraph.findModule(toModule);
    return module.map(m -> exportsPackage(packaze, m)).orElse(false);
  }

  public boolean exportsPackage(String packaze, JavaModule toModule) {

    //        if (packaze.equalsIgnoreCase(SootModuleInfo.MODULE_INFO)) {
    //            return true;
    //        }

    /// all packages are exported/open to self
    if (this == toModule) {
      return this.modulePackages.contains(packaze);
    }

    // a automatic module exports all its packages
    if (this.isAutomaticModule()) {
      return this.modulePackages.contains(packaze);
    }

    Set<SootModuleInfo.PackageReference> exportedPackages =
        this.sootModuleInfo.getExportedPackages();

    if (exportedPackages == null || exportedPackages.isEmpty()) {
      return false;
    }

    List<String> qualifiedExport = this.exportedPackages.get(packaze);
    if (qualifiedExport == null) {
      return false;
    }

    if (qualifiedExport.contains(EVERYONE_MODULE)) {
      return true;
    }
    if (toModule != EVERYONE_MODULE && qualifiedExport.contains(toModule)) {
      return true;
    }

    return false;
  }

  public boolean opensPackage(String packaze, JavaModule toModule) {

    //        if (packaze.equalsIgnoreCase(SootModuleInfo.MODULE_INFO)) {
    //            return true;
    //        }

    /// all packages are exported/open to self
    if (this == toModule) {
      return this.modulePackages.contains(packaze);
    }

    // all packages in open and automatic modules are open
    if (this.isAutomaticModule()) {
      return this.modulePackages.contains(packaze);
    }

    List<String> qualifiedOpens = this.openedPackages.get(packaze);
    if (qualifiedOpens == null) {
      return false; // if qualifiedExport is null, the package is not exported
    }

    if (qualifiedOpens.contains(EVERYONE_MODULE)) {
      return true;
    }
    if (toModule != EVERYONE_MODULE && qualifiedOpens.contains(toModule)) {
      return true;
    }

    return false;
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }
}
