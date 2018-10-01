package de.upb.soot.core;

import de.upb.soot.namespaces.classprovider.ClassSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SootModuleInfo extends SootClass {

  private String name;

  private Map<SootModuleInfo, Integer> requiredModules = new HashMap<SootModuleInfo, Integer>();

  // TODO: change String to SootClassReference
  private Map<String, List<String>> exportedPackages = new HashMap<String, List<String>>();

  // TODO: change String to SootClassReference
  private Map<String, List<String>> openedPackages = new HashMap<String, List<String>>();
  private boolean isAutomaticModule;

  public SootModuleInfo(ClassSource cs, String name, int access, String version) {
    super(cs);
    this.name = name;
    //FIXME: add code
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

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }
}
