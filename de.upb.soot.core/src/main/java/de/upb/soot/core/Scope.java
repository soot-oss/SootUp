package de.upb.soot.core;

import de.upb.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.soot.core.signatures.PackageName;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.util.NotYetImplementedException;

/**
 * Definition of a scope
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Scope {

  /** Define a scope consists of multiple inputLocations. */
  public Scope(AnalysisInputLocation... inputLocations) {
    // TODO Auto-generated constructor stub
  }

  /** Define a scope consists of multiple packages. */
  public Scope(PackageName... packages) {
    // TODO Auto-generated constructor stub
  }

  /** Define a scope consists of multiple classes. */
  public Scope(JavaClassType... classSignatures) {
    // TODO Auto-generated constructor stub
  }

  public Scope withStartingSignature(JavaClassType classSignature) {
    throw new NotYetImplementedException();
  }
}
