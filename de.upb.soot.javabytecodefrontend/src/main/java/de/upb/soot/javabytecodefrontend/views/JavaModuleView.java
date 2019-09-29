package de.upb.soot.javabytecodefrontend.views;

import de.upb.soot.core.Project;
import de.upb.soot.core.model.AbstractClass;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.model.SootModuleInfo;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.javabytecodefrontend.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.types.Type;
import de.upb.soot.core.views.View;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;

public abstract class JavaModuleView implements View {

  @Nonnull private final Map<Pair<SootModuleInfo, Type>, SootClass> map = new HashMap<>();

  public JavaModuleView(Project<JavaModulePathAnalysisInputLocation> project) {}

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull JavaClassType type) {
    // FIXME: get the first class you find, in the annouymous module...
    return null;
  }
}
