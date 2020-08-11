package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class JavaModuleInfo extends AbstractClass<AbstractModuleClassSource> {

  @Nonnull private final ClassType classSignature;
  // FIXME: how to create automatic modules
  private boolean isAutomaticModule;
  private EnumSet<Modifier> modifiers;

  // FIXME: or module Signature?
  private String moduleName;

  public JavaModuleInfo(AbstractModuleClassSource classSource, boolean isAutomaticModule) {
    super(classSource);
    this.classSignature = classSource.getClassType();
    this.isAutomaticModule = isAutomaticModule;
    this.moduleName = getModuleClassSourceContent().getModuleName();
  }

  public static class ModuleReference {

    private JavaClassType moduleInfo;
    private EnumSet<Modifier> modifiers;
    private SootClassSource classSource;

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

    public boolean exportedTo(JavaModuleInfo moduleInfo) {
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

  private AbstractModuleClassSource getModuleClassSourceContent() throws ResolveException {
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
      requires = ImmutableUtils.emptyImmutableSet();

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
      exports = ImmutableUtils.emptyImmutableSet();

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
      opens = ImmutableUtils.emptyImmutableSet();

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
      provides = ImmutableUtils.emptyImmutableSet();

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
      uses = ImmutableUtils.emptyImmutableSet();

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
    return ImmutableUtils.emptyImmutableSet();
  }

  @Nonnull
  @Override
  public Set<Field> getFields() {
    return ImmutableUtils.emptyImmutableSet();
  }
}
