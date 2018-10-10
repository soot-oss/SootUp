package de.upb.soot.core;

import com.google.common.collect.Sets;

import de.upb.soot.namespaces.classprovider.ClassSource;

import java.util.HashSet;

public class SootModuleInfo extends SootClass {

  private class ModuleReference {

    private SootModuleInfo moduleInfo;
    private int accessModifier;

    public ModuleReference(SootModuleInfo moduleInfo, int accessModifier) {
      this.moduleInfo = moduleInfo;
      this.accessModifier = accessModifier;
    }
  }

  private class PackageReference {
    private String packageName;
    private int modifer;
    private HashSet<SootModuleInfo> targetModules;

    public PackageReference(String packageName, int modifier, HashSet<SootModuleInfo> targetModules) {
      this.packageName = packageName;
      this.modifer = modifier;
      this.targetModules = targetModules;
    }
    // e.g. hash by packagename?

    public boolean isPublic() {
      return this.targetModules.isEmpty();
    }

    public boolean exportedTo(SootModuleInfo moduleInfo) {
      if (isPublic()) {
        return true;
      }
      // FIXME: check for automatic modules ?
      return targetModules.contains(moduleInfo);
    }
  }

  private String name;

  private HashSet<ModuleReference> requiredModules = new HashSet<>();

  private HashSet<PackageReference> exportedPackages = new HashSet<>();

  private HashSet<PackageReference> openedPackages = new HashSet<>();

  private HashSet<SootClass> usedServices = new HashSet<>();

  // FIXME: how to create automatic modules
  private boolean isAutomaticModule;
  private int accessModifier;

  /**
   * Create a new SootModuleInfo.
   * 
   * @param cs
   *          the ClassSource that was used to create this module-info
   * @param name
   *          the module name
   * @param access
   *          the module access modifier
   * @param version
   *          the module's version
   */
  public SootModuleInfo(ClassSource cs, String name, int access, String version) {
    super(cs);
    this.name = name;
    this.accessModifier = access;
    // FIXME: add code
  }

  public String getName() {
    return name;
  }

  public void addRequire(SootModuleInfo module, int access, String version) {
    ModuleReference required = new ModuleReference(module, access);
    this.requiredModules.add(required);
  }

  public void addExport(String packaze, int access, Iterable<SootModuleInfo> modules) {
    PackageReference packageReference = new PackageReference(packaze, access, Sets.newHashSet(modules));
    this.exportedPackages.add(packageReference);
  }

  public void addOpen(String packaze, int access, Iterable<SootModuleInfo> modules) {
    PackageReference packageReference = new PackageReference(packaze, access, Sets.newHashSet(modules));
    this.openedPackages.add(packageReference);
  }

  public void addUse(SootClass service) {
    this.usedServices.add(service);
  }

  // FIXME: add here
  public void addProvide(String service, Iterable<SootClass> providers) {
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }

}
