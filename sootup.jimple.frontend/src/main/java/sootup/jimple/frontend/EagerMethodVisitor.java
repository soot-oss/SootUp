package sootup.jimple.frontend;

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
