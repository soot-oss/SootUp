package de.upb.swt.soot.java.core.views;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.JavaModuleInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Java View since Java 9.
 *
 * @author Linghui Luo
 */
public class JavaModuleView extends JavaView {

  public JavaModuleView(
      @Nonnull Project project,
      Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project);
  }
  /** Creates a new instance of the {@link JavaView} class. */
  public JavaModuleView(@Nonnull Project project) {
    this(project, analysisInputLocation -> null);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Nonnull private final Map<Pair<JavaModuleInfo, Type>, SootClass> map = new HashMap<>();

  @Override
  @Nonnull
  public synchronized Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull ClassType type) {
    // FIXME: get the first class you find, in the annouymous module...
    return null;
  }
}
