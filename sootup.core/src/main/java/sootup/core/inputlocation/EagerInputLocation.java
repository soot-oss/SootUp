package sootup.core.inputlocation;

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
import java.util.*;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.SootClassSource;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * stores (already loaded) ClassType -&gt; ClassSource associations for retrieval
 *
 * @author Markus Schmidt
 */
public class EagerInputLocation implements AnalysisInputLocation {

  protected final SourceType sourceType;
  @NonNull private final Map<ClassType, SootClassSource> map;
  @NonNull private final List<BodyInterceptor> bodyInterceptors;

  /** not useful for retrieval of classes via view. remove inputlocation from sootclass? */
  public EagerInputLocation() {
    this(Collections.emptyMap(), SourceType.Application);
  }

  public EagerInputLocation(
      @NonNull Map<ClassType, SootClassSource> map, @Nullable SourceType sourceType) {
    this(map, sourceType, Collections.emptyList());
  }

  public EagerInputLocation(
      @NonNull Map<ClassType, SootClassSource> map,
      @Nullable SourceType sourceType,
      @NonNull List<BodyInterceptor> bodyInterceptors) {
    this.sourceType = sourceType;
    this.map = ImmutableMap.copyOf(map);
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  public @NonNull Optional<SootClassSource> getClassSource(
      @NonNull ClassType type, @Nullable View view) {
    // FIXME: add classloadingoptions
    return Optional.ofNullable(map.get(type));
  }

  @NonNull
  @Override
  public Stream<SootClassSource> getClassSources(@Nullable View view) {
    // FIXME: add classloadingoptions
    return map.values().stream();
  }

  @NonNull
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @Override
  @NonNull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
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
    return map.equals(((EagerInputLocation) o).map);
  }
}
