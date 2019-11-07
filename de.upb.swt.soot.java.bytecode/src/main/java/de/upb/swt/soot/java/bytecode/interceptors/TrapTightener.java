package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/toolkits/exceptions/TrapTightener.java

/**
 * A {@link BodyInterceptor} that shrinks the protected area covered by each {@link
 * de.upb.swt.soot.core.jimple.basic.Trap} in the {@link Body} so that it begins at the first of the
 * {@link Body}'s {@link de.upb.swt.soot.core.jimple.common.stmt.Stmt}s which might throw an
 * exception caught by the {@link de.upb.swt.soot.core.jimple.basic.Trap} and ends just after the
 * last {@link de.upb.swt.soot.core.jimple.common.stmt.Stmt} which might throw an exception caught
 * by the {@link de.upb.swt.soot.core.jimple.basic.Trap}. In the case where none of the {@link
 * de.upb.swt.soot.core.jimple.common.stmt.Stmt}s protected by a {@link
 * de.upb.swt.soot.core.jimple.basic.Trap} can throw the exception it catches, the {@link
 * de.upb.swt.soot.core.jimple.basic.Trap}'s protected area is left completely empty, which will
 * likely cause the {@link UnreachableCodeEliminator} to remove the {@link
 * de.upb.swt.soot.core.jimple.basic.Trap} completely.
 *
 * <p>The {@link TrapTightener} is used to reduce the risk of unverifiable code which can result
 * from the use of {@link ExceptionalUnitGraph}s from which unrealizable exceptional control flow
 * edges have been removed.
 */
public class TrapTightener implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
