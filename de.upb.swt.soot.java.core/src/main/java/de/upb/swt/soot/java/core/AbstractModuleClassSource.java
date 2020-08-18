package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo and others
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

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Abstract class for module source.
 *
 * @author Linghui Luo
 */
public abstract class AbstractModuleClassSource extends AbstractClassSource {

  public AbstractModuleClassSource(
      AnalysisInputLocation srcNamespace, ClassType classSignature, Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  public JavaModuleInfo buildClass(@Nonnull SourceType sourceType) {
    return new JavaModuleInfo(this, false);
  }

  public abstract String getModuleName();

  public abstract Collection<JavaModuleInfo.ModuleReference> requires();

  public abstract Collection<JavaModuleInfo.PackageReference> exports();

  public abstract Collection<JavaModuleInfo.PackageReference> opens();

  public abstract Collection<JavaClassType> provides();

  public abstract Collection<JavaClassType> uses();

  public abstract Set<Modifier> resolveModifiers();

  public abstract Position resolvePosition();
}
