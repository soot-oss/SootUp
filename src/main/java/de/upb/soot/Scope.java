package de.upb.soot;

import de.upb.soot.namespaces.AnalysisInputLocation;
import de.upb.soot.signatures.PackageName;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.NotYetImplementedException;

/**
 * Definition of a scope
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class Scope {

  /** Define a scope consists of multiple namespaces. */
  public Scope(AnalysisInputLocation... namespaces) {
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
