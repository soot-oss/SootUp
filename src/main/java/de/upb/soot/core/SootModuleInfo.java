package de.upb.soot.core;

import com.google.common.base.Suppliers;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ModuleClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
import de.upb.soot.util.Utils;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class SootModuleInfo extends AbstractClass<ModuleClassSource> {

  /** */
  private static final long serialVersionUID = -6856798288630958622L;

  @Nonnull private final JavaClassType classSignature;
  // FIXME: how to create automatic modules
  private boolean isAutomaticModule;
  private EnumSet<Modifier> modifiers;

  // FIXME: or module Signature?
  private String moduleName;

  public SootModuleInfo(ModuleClassSource classSource, boolean isAutomaticModule) {
    super(classSource);
    this.classSignature = classSource.getClassType();
    this.isAutomaticModule = isAutomaticModule;
    this.moduleName = getModuleClassSourceContent().getModuleName();
  }

  public static class ModuleReference {

    private JavaClassType moduleInfo;
    private EnumSet<Modifier> modifiers;
    private ClassSource classSource;

    public ModuleReference(JavaClassType moduleInfo, EnumSet<Modifier> accessModifier) {
      this.moduleInfo = moduleInfo;
      this.modifiers = accessModifier;
    }
  }

  public static class PackageReference {
    private String packageName;
    private EnumSet<Modifier> modifers;
    private Set<JavaClassType> targetModules;

    public PackageReference(
        String packageName, EnumSet<Modifier> modifier, Collection<JavaClassType> targetModules) {
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

  @Nonnull
  private final Supplier<Set<ModuleReference>> _lazyRequiredModules =
      Suppliers.memoize(this::lazyFieldInitializer);

  private final Supplier<Set<PackageReference>> _lazyExportedPackages =
      Suppliers.memoize(this::lazyExportsInitializer);
  private final Supplier<Set<PackageReference>> _lazyOpenedPackages =
      Suppliers.memoize(this::lazyOpenssInitializer);

  private final Supplier<Set<JavaClassType>> _lazyUsedServices =
      Suppliers.memoize(this::lazyUsesInitializer);

  private final Supplier<Set<JavaClassType>> _lazyProvidedServices =
      Suppliers.memoize(this::lazyProvidesInitializer);

  private ModuleClassSource getModuleClassSourceContent() throws ResolveException {
    if (this.classSource == null) {
      throw new ResolveException("Module classSource is null");
    }
    // FIXME: this is ugly
    return this.classSource;
  }

  @Nonnull
  private Set<ModuleReference> lazyFieldInitializer() {
    Set<ModuleReference> requires;
    try {
      requires = new HashSet<>(getModuleClassSourceContent().requires());
    } catch (ResolveException e) {
      requires = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return requires;
  }

  @Nonnull
  private Set<PackageReference> lazyExportsInitializer() {
    Set<PackageReference> exports;
    try {
      exports = new HashSet<>(getModuleClassSourceContent().exports());
    } catch (ResolveException e) {
      exports = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return exports;
  }

  @Nonnull
  private Set<PackageReference> lazyOpenssInitializer() {
    Set<PackageReference> opens;
    try {
      opens = new HashSet<>(getModuleClassSourceContent().opens());
    } catch (ResolveException e) {
      opens = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return opens;
  }

  @Nonnull
  private Set<JavaClassType> lazyProvidesInitializer() {
    Set<JavaClassType> provides;
    try {
      provides = new HashSet<>(getModuleClassSourceContent().provides());
    } catch (ResolveException e) {
      provides = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return provides;
  }

  @Nonnull
  private Set<JavaClassType> lazyUsesInitializer() {
    Set<JavaClassType> uses;
    try {
      uses = new HashSet<>(getModuleClassSourceContent().uses());
    } catch (ResolveException e) {
      uses = Utils.emptyImmutableSet();

      // TODO: [JMP] Exception handling
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return uses;
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }

  @Override
  public String getName() {
    return classSignature.getClassName();
  }

  @Override
  public Type getType() {
    return classSignature;
  }

  @Nonnull
  @Override
  public Set<Method> getMethods() {
    return Utils.emptyImmutableSet();
  }

  @Nonnull
  @Override
  public Set<Field> getFields() {
    return Utils.emptyImmutableSet();
  }
}
