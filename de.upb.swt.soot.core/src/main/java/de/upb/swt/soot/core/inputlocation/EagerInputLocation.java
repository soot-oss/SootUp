package de.upb.swt.soot.core.inputlocation;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt, Christian Brüggemann
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
import com.google.common.collect.ImmutableMap;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * stores (already loaded) ClassType -> ClassSource associations for retrieval
 *
 * @author Markus Schmidt
 */
public class EagerInputLocation<S extends SootClass<? extends SootClassSource<S>>>
    implements AnalysisInputLocation<S> {

  @Nonnull private final Map<ClassType, ? extends SootClassSource<S>> map;

  /**
   * not useful for retrieval of classes via view. // FIXME: circular dependency on sootclass <->
   * remove inputlocation from sootclass?
   */
  public EagerInputLocation() {
    map = Collections.emptyMap();
  }

  public EagerInputLocation(@Nonnull Map<ClassType, ? extends SootClassSource<S>> map) {
    this.map = ImmutableMap.copyOf(map);
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource<S>> getClassSource(
      @Nonnull ClassType type, @Nullable View<?> view) {
    // FIXME: add classloadingoptions
    return Optional.ofNullable(map.get(type));
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClassSource<S>> getClassSources(
      @Nonnull IdentifierFactory identifierFactory, @Nullable View<?> view) {
    // FIXME: add classloadingoptions
    return map.values();
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EagerInputLocation)) {
      return false;
    }
    return map.equals(((EagerInputLocation<?>) o).map);
  }
}
