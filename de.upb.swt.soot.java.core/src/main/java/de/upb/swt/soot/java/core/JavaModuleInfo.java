package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Andreas Dann, Christian Brüggemann and others
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

import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.signatures.ModuleSignature;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import javax.annotation.Nonnull;

public abstract class JavaModuleInfo {

  private final boolean isAutomaticModule;

  public JavaModuleInfo(boolean isAutomaticModule) {
    this.isAutomaticModule = isAutomaticModule;
  }

  public abstract ModuleSignature getModuleSignature();

  public abstract Collection<ModuleReference> requires();

  public abstract Collection<PackageReference> exports();

  public abstract Collection<PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<ModuleModifier> resolveModifiers();

  /** Represents the automatic module (e.g. a jar without a module-descriptor on the module path) */
  // TODO: adapt properties?
  public static JavaModuleInfo createAutomaticModuleInfo(@Nonnull ModuleSignature moduleName) {

    return new JavaModuleInfo(true) {
      @Override
      public ModuleSignature getModuleSignature() {
        return moduleName;
      }

      @Override
      public Collection<ModuleReference> requires() {
        // TODO: can read all other modules and the unnamed module (modules on the classpath)
        return Collections.emptyList();
      }

      @Override
      public Collection<PackageReference> exports() {
        // TODO: all Packages are exported
        return Collections.emptyList();
      }

      @Override
      public Collection<PackageReference> opens() {
        // TODO: all Packages are open
        return Collections.emptyList();
      }

      @Override
      public Collection<JavaClassType> provides() {
        return Collections.emptyList();
      }

      @Override
      public Collection<JavaClassType> uses() {
        return Collections.emptyList();
      }

      @Override
      public Set<ModuleModifier> resolveModifiers() {
        return Collections.emptySet();
      }
    };
  }

  /** Represents all Packages from the Classpath */
  public static JavaModuleInfo getUnnamedModuleInfo() {
    return new JavaModuleInfo(true) {
      @Override
      public ModuleSignature getModuleSignature() {
        return JavaModuleIdentifierFactory.getModuleSignature("");
      }

      @Override
      public Collection<ModuleReference> requires() {
        return Collections.emptyList();
      }

      @Override
      public Collection<PackageReference> exports() {
        return Collections.emptyList();
      }

      @Override
      public Collection<PackageReference> opens() {
        return Collections.emptyList();
      }

      @Override
      public Collection<JavaClassType> provides() {
        return Collections.emptyList();
      }

      @Override
      public Collection<JavaClassType> uses() {
        return Collections.emptyList();
      }

      @Override
      public Set<ModuleModifier> resolveModifiers() {
        return Collections.emptySet();
      }
    };
  }

  public static class ModuleReference {

    @Nonnull private final JavaClassType moduleInfo;
    @Nonnull private final EnumSet<ModuleModifier> modifiers;

    public ModuleReference(
        @Nonnull JavaClassType moduleInfo, @Nonnull EnumSet<ModuleModifier> accessModifier) {
      this.moduleInfo = moduleInfo;
      this.modifiers = accessModifier;
    }

    @Nonnull
    public EnumSet<ModuleModifier> getModifiers() {
      return modifiers;
    }

    @Nonnull
    public JavaClassType getModuleInfo() {
      return moduleInfo;
    }
  }

  public static class PackageReference {
    @Nonnull private PackageName packageName;
    @Nonnull private EnumSet<ModuleModifier> modifers;
    @Nonnull private Set<JavaClassType> targetModules;

    public PackageReference(
        @Nonnull String packageName,
        @Nonnull EnumSet<ModuleModifier> modifier,
        @Nonnull Collection<JavaClassType> targetModules) {
      // TODO: get via Identifierfactory
      this.packageName = new PackageName(packageName);
      this.modifers = modifier;
      this.targetModules = new HashSet<>(targetModules);
    }

    public boolean isPublic() {
      return this.targetModules.isEmpty();
    }

    public boolean exportedTo(@Nonnull JavaModuleInfo moduleInfo) {
      if (isPublic()) {
        return true;
      }
      // TODO: [AD] check for automatic modules ?
      return targetModules.contains(moduleInfo);
    }

    @Nonnull
    public PackageName getPackageName() {
      return packageName;
    }

    @Nonnull
    public EnumSet<ModuleModifier> getModifiers() {
      return modifers;
    }
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }
}
