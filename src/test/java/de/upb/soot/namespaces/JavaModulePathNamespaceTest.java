package de.upb.soot.namespaces;

import categories.Java9Test;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Optional;

import static org.junit.Assert.*;

@Category(Java9Test.class)

public class JavaModulePathNamespaceTest extends AbstractNamespaceTest{

  @Override
  protected SignatureFactory getSignatureFactory() {
    return new ModuleSignatureFactory();
  }

  @Test
  public void singleDir() {
    ModuleSignatureFactory factory = new ModuleSignatureFactory();
    final JavaModulePathNamespace javaClassPathNamespace = new JavaModulePathNamespace(getClassProvider(), "target/test-classes/de/upb/soot/namespaces/modules");
    final ClassSignature sig = factory.getClassSignature("module-info", "","fancyMod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());
  }

  @Test
  public void singleDir2() {
    ModuleSignatureFactory factory = new ModuleSignatureFactory();
    final JavaModulePathNamespace javaClassPathNamespace = new JavaModulePathNamespace(getClassProvider(), "target/test-classes/de/upb/soot/namespaces/modules");
    final ClassSignature sig = factory.getClassSignature("module-info", "","fancyMod");
    //TODO: check for a better minClassFoundNumber
    //also all JDK classes are loaded
    testClassReceival(javaClassPathNamespace,sig,50);
  }

  @Test
  public void singleJar() {
    ModuleSignatureFactory factory = new ModuleSignatureFactory();
    final JavaModulePathNamespace javaClassPathNamespace = new JavaModulePathNamespace(getClassProvider(), "target/test-classes/de/upb/soot/namespaces/modules/de.upb.mod.jar");
    final ClassSignature sig = factory.getClassSignature("module-info", "","de.upb.mod");
    Optional<ClassSource> classSource = javaClassPathNamespace.getClassSource(sig);
    assertTrue(classSource.isPresent());

  }
}