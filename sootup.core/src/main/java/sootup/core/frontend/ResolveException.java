package sootup.core.frontend;

import java.nio.file.Path;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Position;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Andreas Dann and others
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
public class ResolveException extends RuntimeException {

  @NonNull private final String inputUri;
  @NonNull private final Position range;

  // FIXME: [ms] fix usages to give a file uri
  @Deprecated
  public ResolveException(@NonNull String message) {
    this(message, "./file-does-not-exist", NoPositionInformation.getInstance());
  }

  public ResolveException(@NonNull String message, @NonNull Path sourcePath) {
    this(message, sourcePath, NoPositionInformation.getInstance());
  }

  public ResolveException(
      @NonNull String message, @NonNull Path sourcePath, @NonNull Position position) {
    this(message, "file:/" + sourcePath.toAbsolutePath(), position);
  }

  private ResolveException(
      @NonNull String message, @NonNull String inputUri, @NonNull Position range) {
    super(message + " " + inputUri + " " + range);
    this.range = range;
    this.inputUri = inputUri;
  }

  public ResolveException(@NonNull String message, @NonNull Path sourcePath, @NonNull Exception e) {
    this(message, sourcePath, NoPositionInformation.getInstance(), e);
  }

  public ResolveException(
      @NonNull String message,
      @NonNull Path sourcePath,
      @NonNull Position position,
      @NonNull Exception e) {
    this(message, "file:/" + sourcePath.toAbsolutePath(), position, e);
  }

  private ResolveException(
      @NonNull String message,
      @NonNull String inputUri,
      @NonNull Position range,
      @NonNull Exception e) {
    super(message + " " + inputUri + " " + range, e);
    this.range = range;
    this.inputUri = inputUri;
  }

  @NonNull
  public String getInputUri() {
    return inputUri;
  }

  @NonNull
  public Position getRange() {
    return range;
  }
}
