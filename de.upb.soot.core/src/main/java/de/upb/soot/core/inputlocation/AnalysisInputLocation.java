package de.upb.soot.core.inputlocation;

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

import de.upb.soot.core.IdentifierFactory;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.frontend.ClassProvider;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.types.JavaClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Public interface to an input location. Namespaces are sources for {@link SootClass}es, e.g. Java
 * Classpath, Android APK, JAR file, etc. The strategy to traverse something.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 */
public interface AnalysisInputLocation {

  /**
   * Create or find a class source for a given signature.
   *
   * @param signature The signature of the class to be found.
   * @return The source entry for that class.
   */
  @Nonnull
  Optional<? extends AbstractClassSource> getClassSource(@Nonnull JavaClassType signature);

  /**
   * The class provider attached to this input location.
   *
   * @return An instance of {@link ClassProvider} to be used.
   */
  @Nonnull
  ClassProvider getClassProvider();

  @Nonnull
  Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory);
}
