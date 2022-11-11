package sootup.core;
/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Markus Schmidt
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
import javax.annotation.Nonnull;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.model.SourceType;

/**
 * @author Christian Brüggemann
 * @author Markus Schmidt
 */
public interface SourceTypeSpecifier {

  /**
   * Specifies which {@link SourceType} a specific ClassType maps to.
   *
   * @param clsSource the type
   * @return the source type
   */
  @Nonnull
  SourceType sourceTypeFor(AbstractClassSource clsSource);
}
