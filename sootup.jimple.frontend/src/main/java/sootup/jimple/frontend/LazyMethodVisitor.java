package sootup.jimple.frontend;

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
