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

/**
 * This class is a container for language specific information
 *
 * @author Markus Schmidt
 */
public abstract class Language {

  public abstract String getName();

  public abstract int getVersion();

  public abstract IdentifierFactory getIdentifierFactory();

  @Override
  public String toString() {
    return getName() + " " + getVersion();
  }
}
