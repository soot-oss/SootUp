package de.upb.soot.signatures;

public class MethodSignature {

  public final String methodID;
  public final ClassSignature classSignature;

  protected MethodSignature(String methodID, ClassSignature classSignature) {
    this.methodID = methodID;
    this.classSignature = classSignature;
  }
}
