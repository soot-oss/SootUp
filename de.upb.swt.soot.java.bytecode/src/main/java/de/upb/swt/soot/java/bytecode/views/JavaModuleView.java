package de.upb.swt.soot.java.bytecode.views;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.modules.SootModuleInfo;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaModulePathAnalysisInputLocation;
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
