package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import sootup.core.IdentifierFactory;
import sootup.core.Language;
import sootup.java.core.JavaIdentifierFactory;

public class JimpleLanguage extends Language {

  private static JimpleLanguage INSTANCE = new JimpleLanguage();

  public static JimpleLanguage getInstance() {
    return INSTANCE;
  }

  @Override
  public String getName() {
    return "Jimple";
  }

  @Override
  public int getVersion() {
    return -1; // there is no real versioning other than "old" Soot and FutureSoot at the moment
  }

  @Override
  public IdentifierFactory getIdentifierFactory() {
    // FIXME [ms] ?
    return JavaIdentifierFactory.getInstance();
  }
}
