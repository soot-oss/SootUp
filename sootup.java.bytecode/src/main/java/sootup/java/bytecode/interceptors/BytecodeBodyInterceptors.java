package sootup.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.transform.BodyInterceptor;

/** Built-in sets of {@link BodyInterceptor}s for the bytecode frontend */
public enum BytecodeBodyInterceptors {
  Default(
  /*
          new CastAndReturnInliner(),
          new UnreachableCodeEliminator(),
    //      // new LocalSplitter(),
    //      new Aggregator(),
          new TypeAssigner(),
    //      new LocalNameStandardizer(),
          new CopyPropagator(),
          new DeadAssignmentEliminator(),
          new ConditionalBranchFolder(),
          new EmptySwitchEliminator(),
            new NopEliminator(),
            new UnusedLocalEliminator()
  */
  );

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  BytecodeBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @Nonnull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
