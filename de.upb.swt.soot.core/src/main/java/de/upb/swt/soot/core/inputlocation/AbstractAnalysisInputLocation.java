package de.upb.swt.soot.core.inputlocation;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.types.ReferenceType;
import java.util.Optional;
import javax.annotation.Nonnull;

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

/**
 * Basic implementation of {@link AnalysisInputLocation}, encapsulating common behavior. Also used
 * to keep the {@link AnalysisInputLocation} interface clean from internal methods like {@link
 * AbstractAnalysisInputLocation#getClassSource(ReferenceType)}.
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class AbstractAnalysisInputLocation implements AnalysisInputLocation {
  protected final @Nonnull ClassProvider classProvider;

  /**
   * Create the input location.
   *
   * @param classProvider The class provider to be used
   */
  public AbstractAnalysisInputLocation(@Nonnull ClassProvider classProvider) {
    this.classProvider = classProvider;
  }

  /**
   * Returns the {@link ClassProvider} instance for this input location.
   *
   * @return The class provider for this input location
   */
  @Override
  public @Nonnull ClassProvider getClassProvider() {
    return classProvider;
  }

  /*
   * @Override public Collection<SootClass> getClasses(IdentifierFactory factory) {
   *
   * // FIXME: here we must take the classSources and invoke akka... return getClassSources(factory).stream().map(cs ->
   * classProvider.getSootClass(cs)).collect(Collectors.toList()); }
   *
   * @Override public Optional<SootClass> getClass(ClassSignature classSignature) { // FIXME: here we must take the
   * classSources and invoke akka...
   *
   * return getClassSource(classSignature).map(cs -> classProvider.getSootClass(cs)); }
   */

  @Override
  public abstract @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull ReferenceType classSignature);
}
