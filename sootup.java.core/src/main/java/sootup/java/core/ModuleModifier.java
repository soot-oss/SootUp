package sootup.java.core;

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

// Modifier for java 9 modules corresponding to the bytecodes used in module-info.class
public enum ModuleModifier {
  OPENS(0x0020), // a module is accessible to reflection (deep&shallow)
  REQUIRES_TRANSITIVE(
      0x0020), // indicates a dependency that is accessible to other modules which require the
  // module
  REQUIRES_STATIC(0x0040), // static: needed at compile time but not necessarily at run time
  REQUIRES_SYNTHETIC(0x1000), // ?
  REQUIRES_MANDATED(0x8000); // e.g. (i.e.?) implicit dependenciy to java.base

  // USES(0),                       // dependencies which are resolved at runtime
  // PROVIDES(0);

  private final int bytecode;

  ModuleModifier(int i) {
    bytecode = i;
  }

  public int getBytecode() {
    return bytecode;
  }
}
