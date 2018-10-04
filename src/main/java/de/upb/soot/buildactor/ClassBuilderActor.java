package de.upb.soot.buildactor;

import de.upb.soot.namespaces.classprovider.ClassSource;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class ClassBuilderActor extends AbstractLoggingActor {

  private final ClassSource classSource;

  public ClassBuilderActor(ClassSource classSource) {
    this.classSource = classSource;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().matchAny(this::resolve).build();
  }

  private void resolve(Object o) {
    log().info("reify");

    // process field
    // private TypeY x;

    // FieldDefinition(name:=x, visibility:=private, declaredType:=reify("TypeY"))

    // process methods
    // for each method
    // TODO: make a actor

    // ?? for all dependencies??
  }

  public static Props props(ClassSource classSource) {
    return Props.create(ClassBuilderActor.class, classSource);
  }
}
