package de.upb.swt.soot.java.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann
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

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Java View since Java 9.
 *
 * @author Linghui Luo
 */
public class JavaModuleView extends JavaView {

  public JavaModuleView(
      @Nonnull Project project,
      Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project, classLoadingOptionsSpecifier);
  }
  /** Creates a new instance of the {@link JavaView} class. */
  public JavaModuleView(@Nonnull Project project) {
    this(project, analysisInputLocation -> null);
  }

  @Nonnull
  public synchronized Collection<JavaModuleInfo> getModuleInfos() {
    return getAbstractClassSources()
        .filter(clazz -> clazz instanceof JavaModuleInfo)
        .map(clazz -> (JavaModuleInfo) clazz)
        .collect(Collectors.toList());
  }

  @Nonnull
  public synchronized Optional<JavaModuleInfo> getModuleInfo(@Nonnull ClassType type) {
    return getAbstractClass(type)
        .map(
            clazz -> {
              if (clazz instanceof JavaModuleInfo) {
                return (JavaModuleInfo) clazz;
              } else {
                throw new ResolveException(type + " is not a module-info class!");
              }
            });
  }
}
