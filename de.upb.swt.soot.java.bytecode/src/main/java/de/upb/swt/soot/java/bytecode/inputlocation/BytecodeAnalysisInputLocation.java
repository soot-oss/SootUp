package de.upb.swt.soot.java.bytecode.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019 Christian Brüggemann
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
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * An {@link AnalysisInputLocation} containing Java bytecode. Supplies default {@link
 * de.upb.swt.soot.core.inputlocation.ClassLoadingOptions} from {@link BytecodeClassLoadingOptions}.
 */
public interface BytecodeAnalysisInputLocation extends AnalysisInputLocation {

  @Nonnull
  @Override
  default Optional<? extends AbstractClassSource> getClassSource(@Nonnull ClassType type) {
    return getClassSource(type, BytecodeClassLoadingOptions.Default);
  }

  @Nonnull
  @Override
  default Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {
    return getClassSources(identifierFactory, BytecodeClassLoadingOptions.Default);
  }
}
