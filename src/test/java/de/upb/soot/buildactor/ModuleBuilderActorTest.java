package de.upb.soot.buildactor;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.namespaces.JrtFileSystemNamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.namespaces.classprovider.asm.AsmJavaClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.views.Scene;

import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.powermock.reflect.Whitebox;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import categories.Java9Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@Category(Java9Test.class)

public class ModuleBuilderActorTest {

  private Scene getScene() {
    ModuleSignatureFactory signatureFactory = new ModuleSignatureFactory() {
    };

    Scene scene = new Scene(signatureFactory);
    IClassProvider classProvider = new AsmJavaClassProvider(scene);

    final JavaModulePathNamespace javaClassPathNamespace
        = new JavaModulePathNamespace(classProvider, "target/test-classes/de/upb/soot/namespaces/modules");
    scene.addNameSpace(javaClassPathNamespace);

    return scene;
  }

  @Test
  public void refiyMessageModuleInfoTest() throws Exception {

    // FIXME: this casting is so ugly
    final ClassSignature sig
        = ((ModuleSignatureFactory) getScene().getSignatureFactory()).getClassSignature("module-info", "", "fancyMod");
    Optional<ClassSource> source = getScene().pollNamespaces(sig);

    assertTrue(source.isPresent());

    Optional<SootClass> result = getScene().reifyClass(source.get());
    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

  @Test
  public void resolveMessageModuleInfoTest() throws Exception {

    IClassProvider classProvider = new AsmJavaClassProvider(getScene());

    final ClassSignature sig
        = ((ModuleSignatureFactory) getScene().getSignatureFactory()).getClassSignature("module-info", "", "fancyMod");
    Optional<ClassSource> source = getScene().pollNamespaces(sig);

    assertTrue(source.isPresent());

    Optional<SootClass> result = getScene().resolveClass(source.get());

    assertTrue(result.isPresent());
    assertTrue(result.get() instanceof SootModuleInfo);
  }

}