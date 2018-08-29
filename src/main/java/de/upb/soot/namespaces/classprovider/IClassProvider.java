package de.upb.soot.namespaces.classprovider;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 22.05.2018 Manuel Benz
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

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface IClassProvider {

  /**
   * Creates and returns a {@link ClassSource} for a specific source file. The file should be passed as {@link Path} and can
   * be located in an arbitrary {@link java.nio.file.FileSystem}. Implementations should use
   * {@link java.nio.file.Files#newInputStream(Path, OpenOption...)} to access the file.
   * 
   * @param ns
   *          The {@link INamespace} that holds the given file
   * @param sourcePath
   *          Path to the source file of the to-be-created {@link ClassSource}. The given path has to exist and requires to
   *          be handled by this {@link IClassProvider}. Implementations might double check this if wanted.
   * @return A not yet resolved {@link ClassSource}, backed up by the given file
   */
  Optional<ClassSource> getClass(INamespace ns, Path sourcePath);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();
}
