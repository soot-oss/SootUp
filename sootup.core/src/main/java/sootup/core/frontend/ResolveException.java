package sootup.core.frontend;

import java.nio.file.Path;
import javax.annotation.Nonnull;
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

  @Nonnull private final String inputUri;
  @Nonnull private final Position range;

  // FIXME: [ms] fix usages to give a file uri
  @Deprecated
  public ResolveException(@Nonnull String message) {
    this(message, "./file-does-not-exist", NoPositionInformation.getInstance());
  }

  public ResolveException(@Nonnull String message, @Nonnull Path sourcePath) {
    this(message, sourcePath, NoPositionInformation.getInstance());
  }

  public ResolveException(
      @Nonnull String message, @Nonnull Path sourcePath, @Nonnull Position position) {
    this(message, "file:/" + sourcePath.toAbsolutePath().toString(), position);
  }

  private ResolveException(
      @Nonnull String message, @Nonnull String inputUri, @Nonnull Position range) {
    super(message + " " + inputUri + " " + range);
    this.range = range;
    this.inputUri = inputUri;
  }

  public ResolveException(@Nonnull String message, @Nonnull Path sourcePath, @Nonnull Exception e) {
    this(message, sourcePath, NoPositionInformation.getInstance(), e);
  }

  public ResolveException(
      @Nonnull String message,
      @Nonnull Path sourcePath,
      @Nonnull Position position,
      @Nonnull Exception e) {
    this(message, "file:/" + sourcePath.toAbsolutePath().toString(), position, e);
  }

  private ResolveException(
      @Nonnull String message,
      @Nonnull String inputUri,
      @Nonnull Position range,
      @Nonnull Exception e) {
    super(message + " " + inputUri + " " + range, e);
    this.range = range;
    this.inputUri = inputUri;
  }

  @Nonnull
  public String getInputUri() {
    return inputUri;
  }

  @Nonnull
  public Position getRange() {
    return range;
  }
}
