package sootup.java.bytecode.inputlocation;
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
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.transform.BodyInterceptor;
import sootup.java.bytecode.interceptors.BytecodeBodyInterceptors;

/**
 * Built-in sets of {@link ClassLoadingOptions} for the bytecode frontend.
 *
 * @author Christian Brüggemann
 */
public enum BytecodeClassLoadingOptions implements ClassLoadingOptions {
  Default {
    @Nonnull
    @Override
    public List<BodyInterceptor> getBodyInterceptors() {
      return BytecodeBodyInterceptors.Default.bodyInterceptors();
    }
  }
}
