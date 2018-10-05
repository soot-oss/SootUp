package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.namespaces.classprovider.asm.AsmJavaClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.views.Scene;

import java.util.Collection;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

import categories.Java8Test;

/**
 * @author Manuel Benz created on 07.06.18
 */
@Category(Java8Test.class)
public abstract class AbstractNamespaceTest {

  protected static final int CLASSES_IN_JAR = 25;
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
    return new DefaultSignatureFactory() {
    };
  }

  protected IClassProvider createClassProvider() {
    Scene scene = new Scene();
    return new AsmJavaClassProvider(scene);
  }

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound) {
    testClassReceival(ns, sig, minClassesFound, -1);
  }

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound, int maxClassesFound) {
    final Optional<ClassSource> clazz = ns.getClassSource(sig);

    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = ns.getClassSources(getSignatureFactory());
    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertThat(classSources.size(), new GreaterOrEqual<>(minClassesFound));
    if (maxClassesFound != -1) {
      Assert.assertThat(classSources.size(), new LessOrEqual<>(maxClassesFound));
    }
  }
}