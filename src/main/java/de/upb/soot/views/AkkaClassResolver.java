package de.upb.soot.views;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.frontends.ClassSource;

public class AkkaClassResolver {

  // How to look up an actor?
  /**
   * according to the docs https://doc.akka.io/docs/akka/current/actors.html#identifying-actors-via-actor-selection "It is
   * always preferable to communicate with other Actors using their ActorRef instead of relying upon ActorSelection"
   *
   */
  private java.util.HashMap<ClassSource, akka.actor.ActorRef> createdActors = new java.util.HashMap<>();

  public akka.actor.ActorSystem system = akka.actor.ActorSystem.create("myActorToRunTests");

  /**
   * Resolve a SootClass from a given ClassSignature.
   * 
   * @param signature
   *          the signature of the class to resolve
   * @return the initial resolved SootClass or an empty optional, if resolving fails
   */
  public java.util.Optional<AbstractClass> getClass(de.upb.soot.signatures.JavaClassSignature signature,
      de.upb.soot.views.IView view, ClassSource source) {
    java.util.Optional<AbstractClass> result = java.util.Optional.empty();
    // TODO: cache

    // TODO: decide for phantom ---> That's a good question, and how to create them ...

    // MB: consider using source.flatMap(#methodRef) here. methodRef can than point to the actual logic for class resolution
    result = reifyClass(source, view);

    return result;
  }

  public java.util.Optional<AbstractClass> resolveClass(ClassSource classSource, de.upb.soot.views.IView view) {
    java.util.Optional<AbstractClass> result = java.util.Optional.empty();
    akka.actor.ActorRef cb = getOrCreateActor(classSource, view);
    akka.util.Timeout timeout = new akka.util.Timeout(scala.concurrent.duration.Duration.create(5, "seconds"));
    scala.concurrent.Future<Object> cbFuture
        = akka.pattern.Patterns.ask(cb, new de.upb.soot.buildactor.ResolveMessage(), timeout);
    try {
      result
          = java.util.Optional.of((de.upb.soot.core.SootClass) scala.concurrent.Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return result;
  }

  /**
   * Initialize a SootClass from a ClassSource.
   * 
   * @param classSource
   *          to resolve
   * @return the initial resolved class or an empty Optional, if the class initialization fails
   */
  public java.util.Optional<AbstractClass> reifyClass(ClassSource classSource, de.upb.soot.views.IView view) {
    java.util.Optional<AbstractClass> result = java.util.Optional.empty();
    akka.actor.ActorRef cb = getOrCreateActor(classSource, view);
    akka.util.Timeout timeout = new akka.util.Timeout(scala.concurrent.duration.Duration.create(5, "seconds"));
    scala.concurrent.Future<Object> cbFuture
        = akka.pattern.Patterns.ask(cb, new de.upb.soot.buildactor.ReifyMessage(), timeout);
    try {
      result = java.util.Optional.of((AbstractClass) scala.concurrent.Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return result;
  }

  private akka.actor.ActorRef getOrCreateActor(ClassSource source, de.upb.soot.views.IView view) {
    akka.actor.ActorRef actorRef = null;
    if (this.createdActors.containsKey(source)) {
      return createdActors.get(source);
    }
    // does not work for some reason
    // actorRef = this.createdActors.getOrDefault(source, createActorRef(source));
    // return actorRef;
    return createActorRef(source, view);
  }

  private akka.actor.ActorRef createActorRef(ClassSource source, de.upb.soot.views.IView view) {
    akka.actor.ActorRef actorRef = null;
    actorRef = system.actorOf(de.upb.soot.buildactor.ClassBuilderActor.props(view, source));
    this.createdActors.put(source, actorRef);
    return actorRef;
  }

}
