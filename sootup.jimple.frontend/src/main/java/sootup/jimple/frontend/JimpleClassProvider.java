package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.CharStreams;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * @author Markus Schmidt
 */
public class JimpleClassProvider implements ClassProvider {

  @NonNull private final List<BodyInterceptor> bodyInterceptors;
  @NonNull private final View view;

  private static final @NonNull Logger logger = LoggerFactory.getLogger(JimpleClassProvider.class);

  public JimpleClassProvider(List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    this.bodyInterceptors = bodyInterceptors;
    this.view = view;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      AnalysisInputLocation inputlocation, Path sourcePath, ClassType classSignature) {

    try {
      final JimpleConverter jimpleConverter = new JimpleConverter();
      return Optional.of(
          jimpleConverter.run(
              CharStreams.fromPath(sourcePath), inputlocation, sourcePath, bodyInterceptors, view));
    } catch (IOException | ResolveException e) {
      logger.warn(
          "The jimple file of "
              + classSignature
              + " in path: "
              + sourcePath
              + " could not be converted.",
          e);
      return Optional.empty();
    }
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JIMPLE;
  }
}
