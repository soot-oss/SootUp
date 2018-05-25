package de.upb.soot.signatures;

public class ClassSignature {

  public final String className;

  public final PackageSignature packageSignature;

  protected ClassSignature(String className, PackageSignature packageSignature) {
    this.className = className;
    this.packageSignature = packageSignature;
  }
}
