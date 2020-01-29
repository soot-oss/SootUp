package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.util.ImmutableUtils;
import javax.annotation.Nonnull;

public class JavaPackageName extends PackageName {

  @Nonnull private final Iterable<AnnotationType> annotations;

  public Iterable<AnnotationType> getAnnotations() {
    return annotations;
  }

  /**
   * Internal: Constructs a Package Signature of a Java package. Instances should only be created by
   * a {@link de.upb.swt.soot.core.IdentifierFactory }
   *
   * @param packageName the package's name
   */
  public JavaPackageName(@Nonnull String packageName) {
    this(packageName, ImmutableUtils.emptyImmutableList());
  }

  /**
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
}
