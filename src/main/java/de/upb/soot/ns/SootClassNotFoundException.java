package de.upb.soot.ns;

import de.upb.soot.signatures.ClassSignature;

/** @author Manuel Benz created on 06.06.18 */
public class SootClassNotFoundException extends Exception {
  public SootClassNotFoundException(ClassSignature sig) {
    super(String.format("Unable to getClass class '%s' in namespace", sig));
  }
}
