package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootClassMember;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Optional;

// based on:
// https://github.com/soot-oss/soot/blob/master/src/main/java/soot/jimple/toolkits/invoke/AccessManager.java
public class AccessUtil {

  /**
   * Returns true iff target is legally accessible from callerMethodSig. Illegal access occurs when
   * any of the following cases holds: 1. target is private, but callerMethodSig.declaringClass() !=
   * target.declaringClass(); or, 2. target is package-visible, and its package differs from that of
   * callerMethodSig; or, 3. target is protected, and either: a. callerMethodSig doesn't belong to
   * target.declaringClass, or any subclass of ;
   */
  public static boolean isAccessible(
      JavaView view, MethodSignature callerMethodSig, SootClassMember<?> target) {
    final Optional<JavaSootClass> aClass = view.getClass(target.getDeclaringClassType());
    if (!aClass.isPresent()) {
      return false;
    }
    JavaSootClass targetClass = aClass.get();

    if (!isAccessible(callerMethodSig, targetClass)) {
      return false;
    }

    // Condition 1 above.
    if (target.isPrivate() && !targetClass.getType().equals(callerMethodSig.getDeclClassType())) {
      return false;
    }

    // Condition 2. Check the package names.
    if (!target.isPrivate() && !target.isProtected() && !target.isPublic()) {
      if (!target
          .getDeclaringClassType()
          .getPackageName()
          .equals(callerMethodSig.getDeclClassType().getPackageName())) {
        return false;
      }
    }

    /* FIXME: [ms] would add a circular dependency!
    // Condition 3.
    if (target.isProtected()) {
      TypeHierarchy h = new ViewTypeHierarchy(view);

      // protected means that you can be accessed by your children.
      // i.e. callerMethodSig must be in a child of target.
      if (h.isClassSuperclassOfIncluding(targetClass, containerClass)) {
        return true;
      }

      return false;
    }
     */

    return true;
  }

  /**
   * Returns true if an access to <code>target</code> is legal from code in <code>methodSig</code>.
   */
  public static boolean isAccessible(ClassType classType, JavaSootClass target) {
    return target.isPublic()
        || classType.getPackageName().equals(target.getType().getPackageName());
  }

  public static boolean isAccessible(MethodSignature methodSig, JavaSootClass target) {
    return target.isPublic()
        || methodSig.getDeclClassType().getPackageName().equals(target.getType().getPackageName());
  }

  /**
   * Returns true if the statement <code>stmt</code> contains an illegal access to a field or
   * methodSig, assuming the statement is in methodSig <code>methodSig</code>
   *
   * @param methodSig
   * @param stmt
   * @return
   */
  public static boolean isAccessible(JavaView view, MethodSignature methodSig, Stmt stmt) {
    if (stmt.containsInvokeExpr()) {
      final Optional<? extends SootMethod> method =
          view.getMethod(stmt.getInvokeExpr().getMethodSignature());
      if (!method.isPresent()) {
        return false;
      }
      return AccessUtil.isAccessible(view, methodSig, method.get());
    } else if (stmt instanceof JAssignStmt) {
      JAssignStmt as = (JAssignStmt) stmt;
      if (as.getRightOp() instanceof JFieldRef) {
        JFieldRef r = (JFieldRef) as.getRightOp();
        final Optional<? extends SootField> target = view.getField(r.getFieldSignature());
        if (!target.isPresent()) {
          return false;
        }
        return AccessUtil.isAccessible(view, methodSig, target.get());
      }
      if (as.getLeftOp() instanceof JFieldRef) {
        JFieldRef r = (JFieldRef) as.getLeftOp();
        final Optional<? extends SootField> field = view.getField(r.getFieldSignature());
        if (!field.isPresent()) {
          return false;
        }
        return AccessUtil.isAccessible(view, methodSig, field.get());
      }
    }
    return true;
  }
}
