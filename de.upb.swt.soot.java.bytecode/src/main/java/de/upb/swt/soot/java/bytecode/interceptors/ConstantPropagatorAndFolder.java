package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/scalar/ConstantPropagatorAndFolder.java

/**
 * Does constant propagation and folding. Constant folding is the compile-time evaluation of
 * constant expressions (i.e. 2 * 3).
 */
public class ConstantPropagatorAndFolder implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
