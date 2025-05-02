package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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

import java.util.Collections;
import sootup.core.model.SootMethod;

public class JavaSootMethodAdapter extends JavaSootMethod {

  private final SootMethod sootMethod;

  public JavaSootMethodAdapter(SootMethod sootMethod) {
    super(
        sootMethod.getBodySource(),
        sootMethod.getSignature(),
        sootMethod.getModifiers(),
        sootMethod.getExceptionSignatures(),
        Collections.emptyList(), // SootMethod doesn't have annotations
        sootMethod.getPosition());
    this.sootMethod = sootMethod;
  }
}
