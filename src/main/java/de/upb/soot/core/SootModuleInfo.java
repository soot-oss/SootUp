package de.upb.soot.core;

import de.upb.soot.namespaces.classprovider.ClassSource;

import java.util.Optional;

public class SootModuleInfo extends SootClass {

  private String name;

  public SootModuleInfo(ClassSource cs, String name, int access, String version) {
    super(cs);
  }

  public String getName() {
    return name;
  }

  public void addRequire(SootModuleInfo module, int access, String version) {
  }

  public void addExport(String packaze, int access, Iterable<SootModuleInfo> modules) {
  }

  public void addOpen(String packaze, int access, Iterable<SootModuleInfo> modules) {
  }

  public void addUse(SootClass service) {
  }

  public void addProvide(String service, Iterable<SootClass> providers) {
  }
}
