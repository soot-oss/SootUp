package de.upb.swt.soot.core.util;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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

import de.upb.swt.soot.core.inputlocation.FileType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Common functionality useful to cope with {@link Path}s.
 *
 * @author Manuel Benz created on 06.06.18
 */
public class PathUtils {

  /**
   * Matches the given {@link Path} with the file extensions of the given {@link FileType}s.
   *
   * @param path An arbitrary {@link Path}
   * @param extensions One or more {@link FileType}s to check against
   * @return True if the given {@link Path} has the given {@link FileType}, i.e., the path ends with
   *     a dot followed by either of the extensions defined by the given {@link FileType}s
   *     otherwise.
   */
  public static boolean hasExtension(@Nonnull Path path, @Nonnull FileType... extensions) {
    return hasExtension(path, Arrays.asList(extensions));
  }

  /**
   * Matches the given {@link Path} with the file extensions of the given {@link FileType}s.
   *
   * @see PathUtils#hasExtension(Path, FileType...)
   */
  public static boolean hasExtension(@Nonnull Path path, @Nonnull Collection<FileType> extensions) {
    if (Files.isDirectory(path)) {
      return false;
    }
    final String extensionList =
        extensions.stream().map(FileType::getExtension).collect(Collectors.joining(","));
    return path.getFileSystem()
        .getPathMatcher("glob:*.{" + extensionList + "}")
        .matches(path.getFileName());
  }

  public static boolean isArchive(@Nonnull Path path) {
    return hasExtension(path, FileType.ARCHIVE_TYPES);
  }
}
