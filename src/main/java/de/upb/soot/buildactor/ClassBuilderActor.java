package de.upb.soot.buildactor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

import de.upb.soot.classprovider.ClassSource;
import de.upb.soot.views.Scene;

public class ClassBuilderActor extends AbstractLoggingActor {

  private final Scene project;
  private final ClassSource classSource;
  private de.upb.soot.core.SootClass sootClass;

  public ClassBuilderActor(Scene project, ClassSource classSource) {
    this.project = project;
    this.classSource = classSource;
  }

  public static Props props(Scene scene, ClassSource classSource) {
    return Props.create(ClassBuilderActor.class, scene, classSource);
  }

  // this should become just logic for akka, e.g., when to create new actors, etc, the resolving should happen in the
  // classprovider

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(ReifyMessage.class, this::reify).match(ResolveMessage.class, this::resolve)
        .matchEquals("done", m -> getContext().stop(getSelf())).build();
  }

  private void reify(ReifyMessage m) {
    log().info("Start reifying for [{}].", classSource.getClassSignature().toString());
    de.upb.soot.classprovider.ClassProvider classProvider = getClassProvider(classSource);
    sootClass = classProvider.reify(classSource);

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reifying for [{}].", classSource.getClassSignature().toString());
  }

  private void resolve(ResolveMessage m) {
    log().info("Full reify for [{}].", classSource.getClassSignature().toString());
    if (sootClass == null) {
      throw new IllegalStateException();
    }

    de.upb.soot.classprovider.ClassProvider classProvider = getClassProvider(classSource);
    sootClass = classProvider.resolve(sootClass);

    /**
     * TODO: add logic here, to create actors for Methods, and fields ....
     */
    // process methods
    // for each method
    // TODO: make a actor, e.g., maybe different levels, reify = init, resolve=Signature, fullRes = body?

    // ?? for all dependencies??

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reify for [{}]", classSource.getClassSignature().toString());

    // we are done
    this.getSelf().tell("done", this.getSelf());
  }

  private de.upb.soot.classprovider.ClassProvider getClassProvider(de.upb.soot.classprovider.ClassSource source) {
    // use a service registry or whatever
    return source.getClassProvider();
  }

}
