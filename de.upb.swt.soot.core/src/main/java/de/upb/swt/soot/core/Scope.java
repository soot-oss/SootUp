package de.upb.swt.soot.core;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.NotYetImplementedException;

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
  public Scope(ClassType... classSignatures) {
    // TODO Auto-generated constructor stub
  }

  public Scope withStartingSignature(ClassType classSignature) {
    throw new NotYetImplementedException();
  }
}
