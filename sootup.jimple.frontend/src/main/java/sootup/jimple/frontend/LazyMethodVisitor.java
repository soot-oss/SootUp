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

import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.java.core.JavaSootMethod;
import sootup.jimple.JimpleParser;

public class LazyMethodVisitor extends MethodVisitor {

  public LazyMethodVisitor(@NonNull ClassVisitor classVisitor) {
    super(classVisitor);
  }

  @Override
  protected SootMethod resolveMethod(
      JimpleParser.MethodContext ctx,
      MethodSignature methodSignature,
      EnumSet<MethodModifier> modifier,
      List<ClassType> exceptions,
      Position methodPosition) {

    LazyJimpleMethodSource lazySource =
        new LazyJimpleMethodSource(
            methodSignature,
            ctx,
            classVisitor.path,
            classVisitor.bodyInterceptors,
            classVisitor.view,
            classVisitor.util,
            classVisitor.clazz,
            modifier,
            exceptions,
            methodPosition);
    return new JavaSootMethod(lazySource, methodSignature, modifier, exceptions, methodPosition);
  }
}
