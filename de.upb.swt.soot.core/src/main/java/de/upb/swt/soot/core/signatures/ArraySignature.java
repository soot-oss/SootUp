package de.upb.swt.soot.core.signatures;

/**
 * This Interface represents a Signature for Arrays
 *
 * @author Markus Schmidt
 */
public interface ArraySignature extends ClassSignature {

  int getDimension();

  ClassSignature getBaseType();
}
