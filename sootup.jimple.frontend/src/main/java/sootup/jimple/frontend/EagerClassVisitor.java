package sootup.jimple.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Jan Martin Persch, Christian Brüggemann and others
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

import java.nio.file.Path;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.model.Body;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class EagerClassVisitor extends ClassVisitor {

  public EagerClassVisitor(
      @NonNull Path path, @NonNull List<BodyInterceptor> bodyInterceptors, @NonNull View view) {
    super(path, bodyInterceptors, view);
  }

  @Override
  protected MethodVisitor createMethodVisitor() {
    return new EagerMethodVisitor(this);
  }

  @Override
  protected JavaSootMethod processMethod(JavaSootMethod m) {
    if (!m.isConcrete()) {
      return m;
    }
    Body.BodyBuilder bodyBuilder = Body.builder(m.getBody(), m.getModifiers());
    for (BodyInterceptor bodyInterceptor : bodyInterceptors) {
      try {
        bodyInterceptor.interceptBody(bodyBuilder, view);
        bodyBuilder.getControlFlowGraph().validateStmtConnectionsInGraph();
      } catch (Exception e) {
        throw new IllegalStateException(
            "Failed to apply " + bodyInterceptor + " to " + m.getSignature(), e);
      }
    }
    Body modifiedBody = bodyBuilder.build();
    return new JavaSootMethod(
        new OverridingBodySource(m.getBodySource()).withBody(modifiedBody),
        m.getSignature(),
        m.getModifiers(),
        m.getExceptionSignatures(),
        m.getPosition());
  }
}
