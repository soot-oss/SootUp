package de.upb.soot.signatures;

import java.util.Optional;

public class PackageSignature {

  public final String packageID;

  public final Optional<ModuleSignature> moduleSignature;

  protected PackageSignature(String packageID, ModuleSignature moduleSignature) {
    this.packageID = packageID;
    this.moduleSignature = Optional.ofNullable(moduleSignature);
  }
}
