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

import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public abstract class JavaModuleInfo {

  // FIXME: [AD] how to create automatic modules
  private boolean isAutomaticModule;
  private EnumSet<Modifier> modifiers;
  private String moduleName;

  public JavaModuleInfo(boolean isAutomaticModule) {
    this.isAutomaticModule = isAutomaticModule;
  }

  public abstract String getModuleName();

  public abstract Collection<ModuleReference> requires();

  public abstract Collection<PackageReference> exports();

  public abstract Collection<PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<Modifier> resolveModifiers();

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
      // FIXME: [AD] check for automatic modules ?
      return targetModules.contains(moduleInfo);
    }
  }

  public boolean isAutomaticModule() {
    return isAutomaticModule;
  }
}
