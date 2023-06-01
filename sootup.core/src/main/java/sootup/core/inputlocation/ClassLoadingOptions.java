package sootup.core.inputlocation;
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
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.transform.BodyInterceptor;

/**
 * Options that are passed through to the respective frontend. These define how the frontend should
 * behave while loading classes.
 *
 * <p>Besides being able to create your own by implementing this interface, each frontend has
 * built-in sets of options such as {@code java.bytecode.inputlocation.BytecodeClassLoadingOptions}
 * and {@code java.sourcecode.inputlocation.SourcecodeClassLoadingOptions}
 *
 * @author Christian Brüggemann
 */
public interface ClassLoadingOptions {

  /**
   * The interceptors are executed in order on each loaded method body, allowing it to be inspected
   * and manipulated.
   */
  @Nonnull
  List<BodyInterceptor> getBodyInterceptors();
}
