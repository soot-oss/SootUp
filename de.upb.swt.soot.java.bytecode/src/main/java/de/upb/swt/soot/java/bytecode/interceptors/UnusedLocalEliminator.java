package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/toolkits/scalar/UnusedLocalEliminator.java

/**
 * A BodyTransformer that removes all unused local variables from a given Body. Implemented as a
 * singleton.
 */
public class UnusedLocalEliminator implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
