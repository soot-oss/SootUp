package de.upb.swt.soot.core.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** @author Markus Schmidt */

// TODO: implement sth useful - more than this dummy
public class EagerInputLocation implements AnalysisInputLocation {

  @Nonnull
  @Override
  public Optional<AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    throw new ResolveException("getClassSources not implemented - No class sources found.");
  }

  @Override
  public @Nonnull Optional<AbstractClassSource> getClassSource(
      @Nonnull ClassType type, @Nullable ClassLoadingOptions classLoadingOptions) {
    return Optional.empty();
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory,
      @Nullable ClassLoadingOptions classLoadingOptions) {
    throw new ResolveException("getClassSources not implemented - No class sources found.");
  }
}
