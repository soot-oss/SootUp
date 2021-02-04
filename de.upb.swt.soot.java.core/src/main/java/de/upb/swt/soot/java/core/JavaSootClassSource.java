package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public abstract class JavaSootClassSource extends SootClassSource {

  public JavaSootClassSource(
      @Nonnull AnalysisInputLocation<JavaSootClass> srcNamespace,
      @Nonnull ClassType classSignature,
      @Nonnull Path sourcePath) {
    super(srcNamespace, classSignature, sourcePath);
  }

  @Override
  @Nonnull
  public JavaSootClass buildClass(@Nonnull SourceType sourceType) {
    return new JavaSootClass(this, sourceType);
  }

  protected JavaSootClassSource(SootClassSource delegate) {
    super(delegate);
  }

  public abstract Iterable<AnnotationExpr> resolveAnnotations();

  public abstract Iterable<AnnotationExpr> resolveMethodAnnotations();

  public abstract Iterable<AnnotationExpr> resolveFieldAnnotations();
}
