package de.upb.soot;

import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;

public class DefaultFactories {
  private final DefaultSignatureFactory signatureFactory;
  private final DefaultTypeFactory typeFactory;

  public static DefaultFactories create() {
    class Holder {
      DefaultSignatureFactory signatureFactory;
      DefaultTypeFactory typeFactory;
    }
    Holder holder = new Holder();
    holder.signatureFactory = new DefaultSignatureFactory(() -> holder.typeFactory);
    holder.typeFactory = new DefaultTypeFactory(() -> holder.signatureFactory);

    return new DefaultFactories(holder.signatureFactory, holder.typeFactory);
  }

  private DefaultFactories(
      DefaultSignatureFactory signatureFactory, DefaultTypeFactory typeFactory) {
    this.signatureFactory = signatureFactory;
    this.typeFactory = typeFactory;
  }

  public DefaultSignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  public DefaultTypeFactory getTypeFactory() {
    return typeFactory;
  }
}
