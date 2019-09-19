package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.inputlocation.JavaModulePathAnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.Type;
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
      @Nonnull JavaClassType classType) {
    // FIXME: get the first class you find, in the annouymous module...
    return null;
  }
}
