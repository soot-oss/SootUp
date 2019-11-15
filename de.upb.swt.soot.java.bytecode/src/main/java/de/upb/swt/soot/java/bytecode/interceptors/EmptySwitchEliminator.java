package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/scalar/EmptySwitchEliminator.java

/**
 * Removes empty switch statements which always take the default action from a method body, i.e.
 * blocks of the form switch(x) { default: ... }. Such blocks are replaced by the code of the
 * default block.
 *
 * @author Steven Arzt
 */
public class EmptySwitchEliminator implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
