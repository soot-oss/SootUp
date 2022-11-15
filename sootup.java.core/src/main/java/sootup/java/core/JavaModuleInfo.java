package sootup.java.core;

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

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.frontend.ResolveException;
import sootup.java.core.signatures.ModulePackageName;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;

public abstract class JavaModuleInfo {

  public JavaModuleInfo() {}

  public abstract ModuleSignature getModuleSignature();

  public Set<ModuleModifier> getModifiers() {
    return Collections.emptySet();
  }

  // dependencies to other modules
  public abstract Collection<ModuleReference> requires();

  // exported packages
  public abstract Collection<PackageReference> exports();

  // permission for reflection
  public abstract Collection<PackageReference> opens();

  // interface providing
  public abstract Collection<InterfaceReference> provides();

  // interface usage
  public abstract Collection<JavaClassType> uses();

  public abstract boolean isAutomaticModule();

  public boolean isUnnamedModule() {
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getModuleSignature()).append(' ');
    if (isAutomaticModule()) {
      sb.append("auto ");
    }
    sb.append("requires ").append(requires());
    sb.append("exports ").append(exports());
    sb.append("opens ").append(opens());
    sb.append("uses ").append(uses());
    sb.append("provides ").append(provides());
    return sb.toString();
  }

  /** Represents the automatic module (e.g. a jar without a module-descriptor on the module path) */
  public static JavaModuleInfo createAutomaticModuleInfo(@Nonnull ModuleSignature moduleName) {

    return new JavaModuleInfo() {
      @Override
      public ModuleSignature getModuleSignature() {
        return moduleName;
      }

      @Override
      public Collection<ModuleReference> requires() {
        // can read all other modules and the unnamed module (modules on the classpath)
        throw new ResolveException(
            "All modules can be required from the automatic module. Handle it separately.");
      }

      @Override
      public Collection<PackageReference> exports() {
        // all Packages are exported
        throw new ResolveException(
            "All Packages are exported in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<PackageReference> opens() {
        // all Packages are open
        throw new ResolveException(
            "All Packages are open in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<InterfaceReference> provides() {
        throw new ResolveException(
            "All Packages are open in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<JavaClassType> uses() {
        throw new ResolveException(
            "All Packages are open in the automatic module. Handle it separately.");
      }

      @Override
      public boolean isAutomaticModule() {
        return true;
      }
    };
  }

  /** Represents all Packages from the Classpath */
  public static JavaModuleInfo getUnnamedModuleInfo() {
    return new JavaModuleInfo() {
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
      public Collection<InterfaceReference> provides() {
        return Collections.emptyList();
      }

      @Override
      public Collection<JavaClassType> uses() {
        return Collections.emptyList();
      }

      @Override
      public boolean isAutomaticModule() {
        return true;
      }

      @Override
      public boolean isUnnamedModule() {
        return true;
      }

      @Override
      public String toString() {
        return "<unnamed>" + super.toString();
      }
    };
  }

  public static class ModuleReference {

    @Nonnull private final ModuleSignature moduleInfo;
    @Nonnull private final EnumSet<ModuleModifier> modifiers;

    public ModuleReference(
        @Nonnull ModuleSignature moduleInfo, @Nonnull EnumSet<ModuleModifier> accessModifier) {
      this.moduleInfo = moduleInfo;
      this.modifiers = accessModifier;
    }

    @Nonnull
    public EnumSet<ModuleModifier> getModifiers() {
      return modifiers;
    }

    @Nonnull
    public ModuleSignature getModuleSignature() {
      return moduleInfo;
    }

    @Override
    public String toString() {
      return modifiers + " " + moduleInfo;
    }
  }

  public static class InterfaceReference {
    @Nonnull private final JavaClassType interfaceType;
    @Nonnull private final JavaClassType interfaceImplementation;

    public InterfaceReference(
        @Nonnull JavaClassType interfaceType, @Nonnull JavaClassType interfaceImplementation) {
      this.interfaceType = interfaceType;
      this.interfaceImplementation = interfaceImplementation;
    }

    @Nonnull
    public JavaClassType getInterfaceType() {
      return interfaceType;
    }

    @Nonnull
    public JavaClassType getInterfaceImplementation() {
      return interfaceImplementation;
    }

    @Override
    public String toString() {
      return "provides " + interfaceType + " with " + interfaceImplementation;
    }
  }

  public static class PackageReference {
    @Nonnull private final ModulePackageName packageName;
    @Nonnull private final EnumSet<ModuleModifier> modifers;
    @Nonnull private final Set<ModuleSignature> targetModules;

    public PackageReference(
        @Nonnull ModulePackageName packageName,
        @Nonnull EnumSet<ModuleModifier> modifier,
        @Nonnull Collection<ModuleSignature> targetModules) {
      this.packageName = packageName;
      this.modifers = modifier;
      this.targetModules =
          targetModules.isEmpty() ? Collections.emptySet() : new HashSet<>(targetModules);
    }

    /** does not return true in case of self reference (which is usually implicitly allowed). */
    public boolean appliesTo(@Nonnull ModuleSignature moduleSignature) {

      if (targetModules.isEmpty()) {
        // no specific list of modules is given so this package is exported|opened|.. to all
        // packages that are interested in it.
        return true;
      }

      return targetModules.contains(moduleSignature);
    }

    @Nonnull
    public ModulePackageName getPackageName() {
      return packageName;
    }

    @Nonnull
    public EnumSet<ModuleModifier> getModifiers() {
      return modifers;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(modifers).append(' ').append(packageName);
      if (!targetModules.isEmpty()) {
        sb.append(" to ").append(targetModules);
      }
      return sb.toString();
    }
  }
}
