package de.upb.soot.inputlocation;

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
import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link AnalysisInputLocation} interface for the Java class path. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation: https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 *
 * @author Manuel Benz created on 22.05.18
 */
public class JavaClassPathAnalysisInputLocation extends AbstractAnalysisInputLocation {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaClassPathAnalysisInputLocation.class);
  private static final @Nonnull String WILDCARD_CHAR = "*";

  protected @Nonnull Collection<AbstractAnalysisInputLocation> cpEntries;

  /**
   * Creates a {@link JavaClassPathAnalysisInputLocation} which locates classes in the given class
   * path.
   *
   * @param classPath The class path to search in
   */
  public JavaClassPathAnalysisInputLocation(@Nonnull String classPath) {
    this(classPath, getDefaultClassProvider());
  }

  public JavaClassPathAnalysisInputLocation(
      @Nonnull String classPath, @Nonnull ClassProvider provider) {
    super(provider);

    if (isNullOrEmpty(classPath)) {
      throw new InvalidClassPathException("Empty class path given");
    }

    try {
      cpEntries =
          explode(classPath)
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

  /**
   * Explode the class or modulepath entries, separated by {@link File#pathSeparator}.
   *
   * @param paths entries as one string
   * @return path entries
   */
  public static @Nonnull Stream<Path> explode(@Nonnull String paths) {
    // the classpath is split at every path separator which is not escaped
    String regex = "(?<!\\\\)" + Pattern.quote(File.pathSeparator);
    final Stream<Path> exploded =
        Stream.of(paths.split(regex)).flatMap(JavaClassPathAnalysisInputLocation::handleWildCards);
    // we need to filter out duplicates of the same files to not generate duplicate input locations
    return exploded.map(Path::normalize).distinct();
  }

  /**
   * The class path can have directories with wildcards as entries. All jar/JAR files inside those
   * directories have to be added to the class path.
   *
   * @param entry A class path entry
   * @return A stream of class path entries with wildcards exploded
   */
  private static @Nonnull Stream<Path> handleWildCards(@Nonnull String entry) {
    if (entry.endsWith(WILDCARD_CHAR)) {
      Path baseDir = Paths.get(entry.substring(0, entry.indexOf(WILDCARD_CHAR)));
      try {
        return Utils.iteratorToStream(Files.newDirectoryStream(baseDir, "*.{jar,JAR}").iterator());
      } catch (PatternSyntaxException | NotDirectoryException e) {
        throw new InvalidClassPathException("Malformed wildcard entry", e);
      } catch (IOException e) {
        throw new InvalidClassPathException("Couldn't access entries denoted by wildcard", e);
      }
    } else {
      return Stream.of(Paths.get(entry));
    }
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept
    Set<AbstractClassSource> found = new HashSet<>();
    for (AbstractAnalysisInputLocation ns : cpEntries) {
      found.addAll(ns.getClassSources(identifierFactory));
    }
    return found;
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {
    for (AbstractAnalysisInputLocation ns : cpEntries) {
      final Optional<? extends AbstractClassSource> classSource = ns.getClassSource(signature);
      if (classSource.isPresent()) {
        return classSource;
      }
    }
    return Optional.empty();
  }

  private @Nonnull Optional<AbstractAnalysisInputLocation> nsForPath(@Nonnull Path path) {
    if (Files.exists(path)
        && (java.nio.file.Files.isDirectory(path) || PathUtils.isArchive(path))) {
      return Optional.of(PathBasedAnalysisInputLocation.createForClassContainer(path));
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
