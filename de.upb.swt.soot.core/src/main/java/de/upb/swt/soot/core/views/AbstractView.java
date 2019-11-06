package de.upb.swt.soot.core.views;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.Scope;
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
public abstract class AbstractView implements View {

  @Nonnull private final Project project;

  @Nonnull private final Map<ModuleDataKey<?>, Object> moduleData = new HashMap<>();

  public AbstractView(@Nonnull Project project) {
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

  @Nonnull
  public Project getProject() {
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
