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
import org.jspecify.annotations.NonNull;
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
  public static JavaModuleInfo createAutomaticModuleInfo(@NonNull ModuleSignature moduleName) {

    return new JavaModuleInfo() {
      @Override
      public ModuleSignature getModuleSignature() {
        return moduleName;
      }

      @Override
      public Collection<ModuleReference> requires() {
        // can read all other modules and the unnamed module (modules on the classpath)
        throw new UnsupportedOperationException(
            "All modules can be required from the automatic module. Handle it separately.");
      }

      @Override
      public Collection<PackageReference> exports() {
        // all Packages are exported
        throw new UnsupportedOperationException(
            "All Packages are exported in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<PackageReference> opens() {
        // all Packages are open
        throw new UnsupportedOperationException(
            "All Packages are open in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<InterfaceReference> provides() {
        throw new UnsupportedOperationException(
            "All Packages are open in the automatic module. Handle it separately.");
      }

      @Override
      public Collection<JavaClassType> uses() {
        throw new UnsupportedOperationException(
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

    @NonNull private final ModuleSignature moduleInfo;
    @NonNull private final EnumSet<ModuleModifier> modifiers;

    public ModuleReference(
        @NonNull ModuleSignature moduleInfo, @NonNull EnumSet<ModuleModifier> accessModifier) {
      this.moduleInfo = moduleInfo;
      this.modifiers = accessModifier;
    }

    @NonNull
    public EnumSet<ModuleModifier> getModifiers() {
      return modifiers;
    }

    @NonNull
    public ModuleSignature getModuleSignature() {
      return moduleInfo;
    }

    @Override
    public String toString() {
      return modifiers + " " + moduleInfo;
    }
  }

  public static class InterfaceReference {
    @NonNull private final JavaClassType interfaceType;
    @NonNull private final JavaClassType interfaceImplementation;

    public InterfaceReference(
        @NonNull JavaClassType interfaceType, @NonNull JavaClassType interfaceImplementation) {
      this.interfaceType = interfaceType;
      this.interfaceImplementation = interfaceImplementation;
    }

    @NonNull
    public JavaClassType getInterfaceType() {
      return interfaceType;
    }

    @NonNull
    public JavaClassType getInterfaceImplementation() {
      return interfaceImplementation;
    }

    @Override
    public String toString() {
      return "provides " + interfaceType + " with " + interfaceImplementation;
    }
  }

  public static class PackageReference {
    @NonNull private final ModulePackageName packageName;
    @NonNull private final EnumSet<ModuleModifier> modifers;
    @NonNull private final Set<ModuleSignature> targetModules;

    public PackageReference(
        @NonNull ModulePackageName packageName,
        @NonNull EnumSet<ModuleModifier> modifier,
        @NonNull Collection<ModuleSignature> targetModules) {
      this.packageName = packageName;
      this.modifers = modifier;
      this.targetModules =
          targetModules.isEmpty() ? Collections.emptySet() : new HashSet<>(targetModules);
    }

    /** does not return true in case of self reference (which is usually implicitly allowed). */
    public boolean appliesTo(@NonNull ModuleSignature moduleSignature) {

      if (targetModules.isEmpty()) {
        // no specific list of modules is given so this package is exported|opened|.. to all
        // packages that are interested in it.
        return true;
      }

      return targetModules.contains(moduleSignature);
    }

    @NonNull
    public ModulePackageName getPackageName() {
      return packageName;
    }

    @NonNull
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
