package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Bastian Haverkamp
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

import java.nio.file.Path;
import javax.annotation.Nonnull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;

public abstract class JavaAnnotationSootClassSource extends JavaSootClassSource {

  public JavaAnnotationSootClassSource(
      @Nonnull AnalysisInputLocation<? extends SootClass<?>> srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  @Override
  @Nonnull
  public JavaAnnotationSootClass buildClass(@Nonnull SourceType sourceType) {
    return new JavaAnnotationSootClass(this, sourceType);
  }
}
