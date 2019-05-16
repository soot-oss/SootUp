package de.upb.soot.namespaces;

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

import static com.google.common.base.Strings.isNullOrEmpty;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java class path. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 *
 * @author Manuel Benz created on 22.05.18
 */
public class JavaClassPathNamespace extends AbstractNamespace {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaClassPathNamespace.class);

  protected @Nonnull Collection<AbstractNamespace> cpEntries;

  /**
   * Creates a {@link JavaClassPathNamespace} which locates classes in the given class path.
   *
   * @param classPath The class path to search in
   */
  public JavaClassPathNamespace(@Nonnull String classPath) {
    this(classPath, getDefaultClassProvider());
  }

  public JavaClassPathNamespace(@Nonnull String classPath, @Nonnull IClassProvider provider) {
    super(provider);

    if (isNullOrEmpty(classPath)) {
      throw new InvalidClassPathException("Empty class path given");
    }

    try {
      cpEntries =
          JavaNamespaceUtils.explode(classPath)
              .flatMap(cp -> Utils.optionalToStream(nsForPath(cp)))
              .collect(Collectors.toList());
    } catch (IllegalArgumentException e) {
      throw new InvalidClassPathException("Malformed class path given: " + classPath, e);
    }

    if (cpEntries.isEmpty()) {
      throw new InvalidClassPathException("Empty class path given");
    }

    logger.trace("{} class path entries registered", cpEntries.size());
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<AbstractClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {
      found.addAll(ns.getClassSources(identifierFactory));
    }
    return found;
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {
    for (AbstractNamespace ns : cpEntries) {
      final Optional<? extends AbstractClassSource> classSource = ns.getClassSource(signature);
      if (classSource.isPresent()) {
        return classSource;
      }
    }
    return Optional.empty();
  }

  private @Nonnull Optional<AbstractNamespace> nsForPath(@Nonnull Path path) {
    if (Files.exists(path)
        && (java.nio.file.Files.isDirectory(path) || PathUtils.isArchive(path))) {
      return Optional.of(PathBasedNamespace.createForClassContainer(path));
    } else {
      logger.warn("Invalid/Unknown class path entry: " + path);
      return Optional.empty();
    }
  }

  protected static final class InvalidClassPathException extends IllegalArgumentException {
    /** */
    private static final long serialVersionUID = -5130658516046902470L;

    public InvalidClassPathException(@Nullable String message) {
      super(message);
    }

    public InvalidClassPathException(@Nullable String message, @Nullable Throwable cause) {
      super(message, cause);
    }

    public InvalidClassPathException(@Nullable Throwable cause) {
      super(cause);
    }
  }
}
