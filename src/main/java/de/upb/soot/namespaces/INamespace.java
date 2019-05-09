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

import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.IdentifierFactory;
import de.upb.soot.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Public interface to a namespace. Namespaces are sources for {@link SootClass}es, e.g. Java
 * Classpath, Android APK, JAR file, etc. The strategy to traverse something.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface INamespace {

  /**
   * Create or find a class source for a given signature.
   *
   * @param signature The signature of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<ClassSource> getClassSource(@Nonnull JavaClassType signature);

  /**
   * The class provider attached to this namespace.
   *
   * @return An instance of {@link IClassProvider} to be used.
   */
  @Nonnull
  IClassProvider getClassProvider();

  @Nonnull
  Collection<ClassSource> getClassSources(@Nonnull IdentifierFactory identifierFactory);
}
