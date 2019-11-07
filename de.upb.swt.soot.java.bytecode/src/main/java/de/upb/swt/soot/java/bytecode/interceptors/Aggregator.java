package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/base/Aggregator.java

public class Aggregator implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    return null;
  }
}
