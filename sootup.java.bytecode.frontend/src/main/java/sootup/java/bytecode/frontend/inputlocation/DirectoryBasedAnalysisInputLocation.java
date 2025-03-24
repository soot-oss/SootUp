package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Markus Schmidt and others
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.conversion.AsmJavaClassProvider;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.types.JavaClassType;

class DirectoryBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

  protected DirectoryBasedAnalysisInputLocation(
      @NonNull Path path,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    this(path, srcType, bodyInterceptors, Collections.emptyList());
  }

  protected DirectoryBasedAnalysisInputLocation(
      @NonNull Path path,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull Collection<Path> ignoredPaths) {
    super(path, srcType, bodyInterceptors, ignoredPaths);
  }

  @Override
  @NonNull
  public Stream<JavaSootClassSource> getClassSources(@NonNull View view) {
    // FIXME: 1) store the classprovider reference as a field; 2) and above too; and 3) move view
    // which is only used in SootNode to be just there?
    return walkDirectory(path, view.getIdentifierFactory(), new AsmJavaClassProvider(view));
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {
    return getClassSourceInternal((JavaClassType) type, path, new AsmJavaClassProvider(view));
  }
}
