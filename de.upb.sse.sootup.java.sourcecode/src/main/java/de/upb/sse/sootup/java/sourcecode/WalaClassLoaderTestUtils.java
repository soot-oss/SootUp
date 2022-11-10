package de.upb.sse.sootup.java.sourcecode;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Markus Schmidt
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

import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.model.SourceType;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.java.sourcecode.frontend.WalaJavaClassProvider;
import java.util.Optional;

public class WalaClassLoaderTestUtils {
  public static Optional<SootMethod> getSootMethod(
      WalaJavaClassProvider WalaJavaClassProvider, MethodSignature signature) {
    // We let getClassSource do the hard work for us. This also
    // initializes the SootMethod correctly to know about its declaring
    // class.
    return WalaJavaClassProvider.getClassSource(signature.getDeclClassType())
        .map(cs -> new SootClass(cs, SourceType.Application))
        .flatMap(
            sootClass -> (Optional<SootMethod>) sootClass.getMethod(signature.getSubSignature()));
  }
}
