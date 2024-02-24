package org.sootup.java.codepropertygraph.evaluation.normalizers;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class HashSuffixEliminator implements BodyInterceptor {
  @Override
  public void interceptBody(@Nonnull BodyBuilder builder, @Nonnull View view) {
    Map<Local, Local> localsToReplace = new HashMap<>();
    for (Local local : builder.getLocals()) {
      String localName = local.getName();
      if (!localName.contains("#")) continue;
      String newName = localName.split("#")[0];
      Local newLocal = local.withName(newName);
      localsToReplace.put(local, newLocal);
    }

    for (Map.Entry<Local, Local> entry : localsToReplace.entrySet()) {
      builder.replaceLocal(entry.getKey(), entry.getValue());
    }
  }
}
