package de.upb.soot.buildactor;

import de.upb.soot.core.SootModule;
import de.upb.soot.namespaces.classprovider.ClassSource;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class ModuleBuilderActor extends AbstractLoggingActor {

  private final ClassSource classSource;

  public ModuleBuilderActor(ClassSource classSource) {
    this.classSource = classSource;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().matchAny(this::resolve).build();

  }

  private void resolve(Object o) {

    log().info("resolve");

    // Create the class
    SootModule module = new SootModule(classSource);

    // ?? for all dependencies??
  }

  public static Props props(ClassSource classSource) {
    return Props.create(ModuleBuilderActor.class, classSource);
  }
}
