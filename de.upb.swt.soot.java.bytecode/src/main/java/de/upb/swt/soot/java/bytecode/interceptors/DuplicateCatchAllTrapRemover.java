package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/toolkits/exceptions/DuplicateCatchAllTrapRemover.java

/**
 * Some compilers generate duplicate traps:
 *
 * <p>Exception table: from to target type 9 30 37 Class java/lang/Throwable 9 30 44 any 37 46 44
 * any
 *
 * <p>The semantics is as follows:
 *
 * <p>try { // block } catch { // handler 1 } finally { // handler 2 }
 *
 * <p>In this case, the first trap covers the block and jumps to handler 1. The second trap also
 * covers the block and jumps to handler 2. The third trap covers handler 1 and jumps to handler 2.
 * If we treat "any" as java.lang. Throwable, the second handler is clearly unnecessary. Worse, it
 * violates Soot's invariant that there may only be one handler per combination of covered code
 * region and jump target.
 *
 * <p>This transformer detects and removes such unnecessary traps.
 *
 * @author Steven Arzt
 */
public class DuplicateCatchAllTrapRemover implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
