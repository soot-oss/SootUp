package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JavaPackageName extends PackageName {

  @Nullable private Iterable<AnnotationType> annotations;

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link de.upb.swt.soot.core.IdentifierFactory }
   *
   * @param packageName the package's name
   */
  public JavaPackageName(@Nonnull String packageName) {
    this(packageName, null);
  }

  /*
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link de.upb.swt.soot.core.IdentifierFactory }
   *
   * @param annotations
   * @param packageName the package's name
   */
  public JavaPackageName(
      @Nonnull String packageName, @Nonnull Iterable<AnnotationType> annotations) {
    super(packageName);
    this.annotations = annotations;
  }

  public Iterable<AnnotationType> getAnnotations(JavaView view, String packageName) {
    if (annotations != null) {
      Optional<SootClass> sc =
          view.getClass(
              JavaIdentifierFactory.getInstance().getClassType(PACKAGE_INFO, packageName));
      annotations =
          sc.isPresent() ? ((JavaSootClass) sc.get()).getAnnotations() : Collections.emptyList();
    }
    return annotations;
  }
}
