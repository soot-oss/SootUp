package de.upb.swt.soot.core.views;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Options;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract class for view.
 *
 * @author Linghui Luo
 */
public abstract class AbstractView<S extends AnalysisInputLocation> implements View {

  @Nonnull private final Project<S> project;

  @Nonnull private final Options options = new Options();

  @Nonnull private final Map<ModuleDataKey<?>, Object> moduleData = new HashMap<>();

  public AbstractView(@Nonnull Project<S> project) {
    this.project = project;
  }

  @Override
  @Nonnull
  public IdentifierFactory getIdentifierFactory() {
    return this.getProject().getIdentifierFactory();
  }

  @Override
  @Nonnull
  public Optional<Scope> getScope() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  @Nonnull
  public Options getOptions() {
    return this.options;
  }

  @Nonnull
  public Project<S> getProject() {
    return project;
  }

  @SuppressWarnings("unchecked") // Safe because we only put T in putModuleData
  @Override
  @Nullable
  public <T> T getModuleData(@Nonnull ModuleDataKey<T> key) {
    return (T) moduleData.get(key);
  }

  @Override
  public <T> void putModuleData(@Nonnull ModuleDataKey<T> key, @Nonnull T value) {
    moduleData.put(key, value);
  }
}
