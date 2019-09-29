package de.upb.soot.core.views;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import de.upb.soot.core.buildactor.ClassBuilderActor;
import de.upb.soot.core.buildactor.ReifyMessage;
import de.upb.soot.core.buildactor.ResolveMessage;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.frontend.ClassSource;
import de.upb.soot.core.model.AbstractClass;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.types.JavaClassType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AkkaClassResolver {

  // TODO: How to look up an actor?
  /**
   * according to the docs
   * https://doc.akka.io/docs/akka/current/actors.html#identifying-actors-via-actor-selection "It is
   * always preferable to communicate with other Actors using their ActorRef instead of relying upon
   * ActorSelection"
   */
  private Map<ClassSource, ActorRef> createdActors = new HashMap<>();

  public akka.actor.ActorSystem system = akka.actor.ActorSystem.create("myActorToRunTests");

  /**
   * Resolve a SootClass from a given ClassSignature.
   *
   * @param signature the signature of the class to resolve
   * @return the initial resolved SootClass or an empty optional, if resolving fails
   */
  public Optional<AbstractClass<? extends AbstractClassSource>> getClass(
      JavaClassType signature, View view, ClassSource source) {
    // TODO: cache

    // TODO: decide for phantom ---> That's a good question, and how to create them ...

    // MB: consider using source.flatMap(#methodRef) here. methodRef can than point to the actual
    // logic for class resolution
    return reifyClass(source, view);
  }

  public Optional<AbstractClass<ClassSource>> resolveClass(ClassSource classSource, View view) {
    ActorRef cb = getOrCreateActor(classSource, view);
    Timeout timeout = new akka.util.Timeout(Duration.create(5, "seconds"));
    Future<Object> cbFuture = Patterns.ask(cb, new ResolveMessage(), timeout);
    try {
      return Optional.of((SootClass) scala.concurrent.Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return Optional.empty();
  }

  /**
   * Initialize a SootClass from a ClassSource.
   *
   * @param classSource to resolve
   * @return the initial resolved class or an empty Optional, if the class initialization fails
   */
  public Optional<AbstractClass<? extends AbstractClassSource>> reifyClass(
      ClassSource classSource, View view) {
    ActorRef cb = getOrCreateActor(classSource, view);
    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
    scala.concurrent.Future<Object> cbFuture = Patterns.ask(cb, new ReifyMessage(), timeout);
    try {
      return Optional.of(
          (AbstractClass<? extends AbstractClassSource>)
              scala.concurrent.Await.result(cbFuture, timeout.duration()));
    } catch (Exception e) {
      // TODO: Do something meaningful here
    }
    return Optional.empty();
  }

  private ActorRef getOrCreateActor(ClassSource source, View view) {
    if (this.createdActors.containsKey(source)) {
      return createdActors.get(source);
    }
    // does not work for some reason
    // actorRef = this.createdActors.getOrDefault(source, createActorRef(source));
    // return actorRef;
    return createActorRef(source, view);
  }

  private ActorRef createActorRef(ClassSource source, View view) {
    ActorRef actorRef = system.actorOf(ClassBuilderActor.props(view, source));
    this.createdActors.put(source, actorRef);
    return actorRef;
  }
}
