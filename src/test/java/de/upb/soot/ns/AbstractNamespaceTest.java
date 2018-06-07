package de.upb.soot.ns;

import org.junit.Before;

import de.upb.soot.ns.classprovider.IClassProvider;
import de.upb.soot.signatures.SignatureFactory;

/**
 * @author Manuel Benz created on 07.06.18
 */
public abstract class AbstractNamespaceTest {

  private SignatureFactory signatureFactory;
  private IClassProvider classProvider;

  @Before
  public void setUp() {
    signatureFactory = createSignatureFactory();
    classProvider = createClassProvider();
  }

  protected SignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  protected IClassProvider getClassProvider() {
    return classProvider;
  }

  protected SignatureFactory createSignatureFactory() {
    return new SignatureFactory() {
    };
  }

  protected IClassProvider createClassProvider() {
    return new DummyClassProvider(getSignatureFactory());
  }

}