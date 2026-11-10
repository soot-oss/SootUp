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
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.java.core.JavaSootMethod;
import sootup.jimple.JimpleParser;

public class EagerMethodVisitor extends MethodVisitor {

  public EagerMethodVisitor(@NonNull ClassVisitor classVisitor) {
    super(classVisitor);
  }

  @Override
  protected SootMethod resolveMethod(
      JimpleParser.MethodContext ctx,
      MethodSignature methodSignature,
      EnumSet<MethodModifier> modifier,
      List<ClassType> exceptions,
      Position methodPosition) {

    List<Trap> traps = new ArrayList<>();
    List<List<Stmt>> blocks = new ArrayList<>();

    parseMethodBody(ctx, traps, blocks, new HashMap<>());

    var successorMap = resolveSuccessors(ctx);

    Position classPosition = JimpleConverterUtil.buildPositionFromCtx(ctx);

    final Body build;
    try {
      MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
      graph.initializeWith(blocks, successorMap, traps);
      Body.BodyBuilder builder = Body.builder(graph);

      builder.setModifiers(modifier);
      builder.setMethodSignature(methodSignature);
      builder.setLocals(new HashSet<>(locals.values()));
      builder.setPosition(classPosition);

      build = builder.build();
    } catch (Exception e) {
      throw new ResolveException(
          methodSignature.getName() + " " + e.getMessage(), classVisitor.path, methodPosition, e);
    }

    OverridingBodySource oms = new OverridingBodySource(methodSignature, build);
    return new JavaSootMethod(oms, methodSignature, modifier, exceptions, methodPosition);
  }
}
