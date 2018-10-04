package de.upb.soot.views;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import de.upb.soot.buildactor.ResolveMessage;
import de.upb.soot.namespaces.JavaModulePathNamespace;
import de.upb.soot.signatures.SignatureFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import de.upb.soot.buildactor.ClassBuilderActor;
import de.upb.soot.buildactor.ModuleBuilderActor;
import de.upb.soot.buildactor.ReifyMessage;
import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class Scene {
  private ActorSystem system;

  public SignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  private SignatureFactory signatureFactory;

  private Collection<INamespace> namespaces;

  public Scene(SignatureFactory signatureFactory) {
    this.system = ActorSystem.create("myActorToRunTests");
    this.signatureFactory = signatureFactory;
    this.namespaces = new HashSet<>();
  }

  public Scene() {
    this(null);
  }

  public Optional<SootClass> getClass(ClassSignature signature) {
    Optional<SootClass> result = Optional.empty();
    // TODO: cache

    // TODO: decide for phantom

    Optional<ClassSource> source = pollNamespaces(signature);
    // MB: consider using source.flatMap(#methodRef) here. methodRef can than point to the actual logic for class resolution
    if (source.isPresent()) {
      result = reifyClass(source.get());
    }

    return result;
  }

  public Optional<SootClass> resolveClass(ClassSource classSource) {
    Optional<SootClass> result = Optional.empty();
    ActorRef cb = createActor(classSource);
    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
    Future<Object> cbFuture = Patterns.ask(cb, new ResolveMessage(), timeout);
    try {
      result = Optional.of((SootClass) Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return result;
  }

  public Optional<SootClass> reifyClass(ClassSource classSource) {
    Optional<SootClass> result = Optional.empty();
    ActorRef cb = createActor(classSource);
    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
    Future<Object> cbFuture = Patterns.ask(cb, new ReifyMessage(), timeout);
    try {
      result = Optional.of((SootClass) Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return result;
  }

  private ActorRef createActor(ClassSource source) {
    if (source.getClassSignature().isModuleInfo()) {
      return system.actorOf(ModuleBuilderActor.props(this, source));
    }
    return system.actorOf(ClassBuilderActor.props(source));
  }

  public Optional<ClassSource> pollNamespaces(ClassSignature signature) {
    // TODO: Traverse through namespaces
    // MB this does not make sense in our current impl. you would rather poll the namespaces one after another and if one
    // returns a non-empty optional, you would pass that to the caller
    Optional<ClassSource> result = null;
    for (INamespace namespace : this.namespaces) {
      result = namespace.getClassSource(signature);
      if (result.isPresent()) {
        return result;
      }
    }
    return Optional.ofNullable(null);
  }

  public void addNameSpace(INamespace namespace) {
    this.namespaces.add(namespace);
  }
}
