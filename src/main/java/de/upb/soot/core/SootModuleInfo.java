package de.upb.soot.core;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.util.Utils;
import de.upb.soot.views.IView;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class SootModuleInfo extends AbstractClass {

  // implementation of StepBuilder Pattern to create SootClasses consistent
  // http://www.svlada.com/step-builder-pattern/

  /**
   * Creates a SootClass with a fluent interfaces and enforces at compile team a clean order to
   * ensure a consistent state of the soot class Therefore, a different Interface is returned after
   * each step.. (therby order is enforced)
   */
  public interface DanglingStep extends Build {
    HierachyStep dangling(IView view, ClassSource source, ClassType classType, String moduleName);

    HierachyStep isAutomaticModule(boolean isAutomatic);
  }

  public interface HierachyStep extends Build {
    Build hierachy(
        Collection<ModuleReference> requires,
        Collection<PackageReference> exports,
        Collection<PackageReference> opens,
        Collection<JavaClassSignature> services);
  }

  public interface Build {
    SootModuleInfo build();
  }

  public static class SootModuleInfoBuilder implements DanglingStep, HierachyStep, Build {
    private ResolvingLevel resolvingLevel;
    private ClassType classType;
    private EnumSet<Modifier> modifiers;
    private ClassSource classSource;
    private IView view;
    private Collection<ModuleReference> requires;
    private Collection<PackageReference> exports;
    private Collection<PackageReference> opens;
    private Collection<JavaClassSignature> services;
    private boolean isAutomaticModule = false;
    private String moduleName;

    public SootModuleInfoBuilder() {}

    @Override
    public HierachyStep dangling(IView view, ClassSource source, ClassType classType, String name) {
      this.view = view;
      this.classSource = source;
      this.classType = classType;
      this.resolvingLevel = ResolvingLevel.DANGLING;
      this.moduleName = name;
      return this;
    }

    @Override
    public HierachyStep isAutomaticModule(boolean isAutomatic) {
      this.isAutomaticModule = isAutomatic;
      return this;
    }

    @Override
    public Build hierachy(
        Collection<ModuleReference> requires,
        Collection<PackageReference> exports,
        Collection<PackageReference> opens,
        Collection<JavaClassSignature> services) {

      this.requires = requires;
      this.exports = exports;
      this.opens = opens;
      this.services = services;
      this.resolvingLevel = ResolvingLevel.HIERARCHY;
      return this;
    }

    @Override
    public SootModuleInfo build() {
      return new SootModuleInfo(this);
    }
  }

  public static DanglingStep builder() {
    return new SootModuleInfoBuilder();
  }

  // FIXME: check if everything is here...
  public static SootModuleInfoBuilder fromExisting(SootModuleInfo sootClass) {
    SootModuleInfoBuilder builder = new SootModuleInfoBuilder();
    // builder.resolvingLevel = sootClass.resolvingLevel;
    builder.modifiers = sootClass.modifiers;
    builder.classSource = sootClass.classSource;
    builder.requires = sootClass.requiredModules;
    builder.exports = sootClass.exportedPackages;
    builder.opens = sootClass.openedPackages;
    builder.services = sootClass.usedServices;
    builder.moduleName = sootClass.name;
    return builder;
  }

  // FIXME: add missing statementss
  private SootModuleInfo(SootModuleInfoBuilder builder) {
    super(builder.classSource);
    this.resolvingLevel = builder.resolvingLevel;
    this.moduleSignature = builder.classSource.getClassSignature();
    this.modifiers = builder.modifiers;
    this.isAutomaticModule = builder.isAutomaticModule;
    this.name = builder.moduleName;
    builder.view.addClass(this);
  }

  /** */
  private static final long serialVersionUID = -6856798288630958622L;

  public static class ModuleReference {

    private JavaClassSignature moduleInfo;
    private EnumSet<Modifier> modifiers;
    private ClassSource classSource;

    public ModuleReference(JavaClassSignature moduleInfo, EnumSet<Modifier> accessModifier) {
      this.moduleInfo = moduleInfo;
      this.modifiers = accessModifier;
    }
  }

  public static class PackageReference {
    private String packageName;
    private EnumSet<Modifier> modifers;
    private Set<JavaClassSignature> targetModules;

    public PackageReference(
        String packageName,
        EnumSet<Modifier> modifier,
        Collection<JavaClassSignature> targetModules) {
      this.packageName = packageName;
      this.modifers = modifier;
      this.targetModules = new HashSet<>(targetModules);
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

  private JavaClassSignature moduleSignature;
  private final ResolvingLevel resolvingLevel;

  private HashSet<ModuleReference> requiredModules = new HashSet<>();

  private HashSet<PackageReference> exportedPackages = new HashSet<>();

  private HashSet<PackageReference> openedPackages = new HashSet<>();

  private HashSet<JavaClassSignature> usedServices = new HashSet<>();

  // FIXME: how to create automatic modules
  private boolean isAutomaticModule;
  private EnumSet<Modifier> modifiers;

  // FIXME: or module Signature?
  private String name;

  /**
   * Create a new SootModuleInfo.
   *
   * @param cs the ClassSource that was used to create this module-info
   * @param moduleSignature the moduleSignature
   * @param access the module access modifier
   */
  public SootModuleInfo(
      IView view,
      ClassSource cs,
      JavaClassSignature moduleSignature,
      EnumSet<Modifier> access,
      String version,
      ResolvingLevel resolvingLevel) {
    super(cs);
    this.moduleSignature = moduleSignature;
    this.resolvingLevel = resolvingLevel;
    this.modifiers = null;
    view.addClass(this);
    // FIXME: add code
  }

  public ResolvingLevel resolvingLevel() {
    return resolvingLevel;
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }

  @Override
  public String getName() {
    return moduleSignature.getClassName();
  }

  @Override
  public ISignature getSignature() {
    return this.moduleSignature;
  }
  
  @Nonnull @Override public Set<IMethod> getMethods() {
    return Utils.emptyImmutableSet();
  }
  
  @Nonnull @Override public Set<IField> getFields() {
    return Utils.emptyImmutableSet();
  }
}
