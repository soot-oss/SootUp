package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.DefaultIdentifierFactory;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.inputlocation.SourceTypeSpecifier;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Convenience Project that will analyze Java.
 *
 * @author Markus Schmidt
 */
public class JavaProject extends Project<AnalysisInputLocation> {

  /** TODO */
  public JavaProject(@Nonnull AnalysisInputLocation inputLocation) {
    super(
        inputLocation,
        DefaultIdentifierFactory.getInstance(),
        DefaultSourceTypeSpecifier.getInstance());
  }
  /** TODO */
  public JavaProject(
      @Nonnull AnalysisInputLocation inputLocation, @Nonnull IdentifierFactory identifierFactory) {
    super(inputLocation, identifierFactory, DefaultSourceTypeSpecifier.getInstance());
  }
  /** TODO */
  public JavaProject(
      @Nonnull AnalysisInputLocation inputLocation,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(inputLocation, DefaultIdentifierFactory.getInstance(), sourceTypeSpecifier);
  }

  /** TODO */
  public JavaProject(@Nonnull Set<AnalysisInputLocation> inputLocations) {
    super(
        inputLocations,
        DefaultIdentifierFactory.getInstance(),
        DefaultSourceTypeSpecifier.getInstance());
  }
  /** TODO */
  public JavaProject(
      @Nonnull Set<AnalysisInputLocation> inputLocations,
      @Nonnull IdentifierFactory identifierFactory) {
    super(inputLocations, identifierFactory, DefaultSourceTypeSpecifier.getInstance());
  }
  /** TODO */
  public JavaProject(
      @Nonnull Set<AnalysisInputLocation> inputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(inputLocations, DefaultIdentifierFactory.getInstance(), sourceTypeSpecifier);
  }

  /** TODO */
  public JavaProject(
      @Nonnull AnalysisInputLocation inputLocation,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(inputLocation, identifierFactory, sourceTypeSpecifier);
  }
}
