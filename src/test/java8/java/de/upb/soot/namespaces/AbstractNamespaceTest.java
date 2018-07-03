package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.util.Collection;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

/**
 * @author Manuel Benz created on 07.06.18
 */
public abstract class AbstractNamespaceTest {

  protected static final int CLASSES_IN_JAR = 25;
  protected static final String TARGET_CLASSES_DIR = "target/classes";
  protected static final String TARGET_TEST_CLASSES_DIR = "target/test-classes-java8";
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

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound) {
    testClassReceival(ns, sig, minClassesFound, -1);
  }

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound, int maxClassesFound) {
    final Optional<ClassSource> clazz = ns.getClassSource(sig);

    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = ns.getClassSources();
    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertThat(classSources.size(), new GreaterOrEqual<>(minClassesFound));
    if (maxClassesFound != -1) {
      Assert.assertThat(classSources.size(), new LessOrEqual<>(maxClassesFound));
    }
  }
}