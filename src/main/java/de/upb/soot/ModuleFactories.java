package de.upb.soot;

import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.types.ModuleTypeFactory;

public class ModuleFactories {
  private final ModuleSignatureFactory signatureFactory;
  private final ModuleTypeFactory typeFactory;

  public static ModuleFactories create() {
    class Holder {
      ModuleSignatureFactory signatureFactory;
      ModuleTypeFactory typeFactory;
    }
    Holder holder = new Holder();
    holder.signatureFactory = new ModuleSignatureFactory(() -> holder.typeFactory);
    holder.typeFactory = new ModuleTypeFactory(() -> holder.signatureFactory);

    return new ModuleFactories(holder.signatureFactory, holder.typeFactory);
  }

  private ModuleFactories(ModuleSignatureFactory signatureFactory, ModuleTypeFactory typeFactory) {
    this.signatureFactory = signatureFactory;
    this.typeFactory = typeFactory;
  }

  public ModuleSignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  public ModuleTypeFactory getTypeFactory() {
    return typeFactory;
  }
}
