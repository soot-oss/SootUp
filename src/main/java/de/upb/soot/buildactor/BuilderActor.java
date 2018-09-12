package de.upb.soot.buildactor;

import de.upb.soot.namespaces.classprovider.ClassSource;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;

public class BuilderActor extends AbstractLoggingActor {

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(BuildSootClass.class, this::buildClass).match(BuildSootModule.class, this::buildModule)
        .build();
  }

  private void buildClass(BuildSootClass buildSootClass) {
    log().debug("resolve the Class");

    ActorRef classBuilderActor = context().actorOf(ClassBuilderActor.props(buildSootClass.classSource),
        buildSootClass.classSource.getClassSignature().toString());
    /*
     * Future<Object> future = Patterns.ask(classBuilderActor, "resolve", new Timeout(Duration.create(5, "seconds")));
     * future.onComplete(new OnSuccess<Try<Object>>() {
     * 
     * @Override public void onSuccess(Try<Object> result) throws Throwable {
     * 
     * } }, getContext().dispatcher());
     */
  }

  private void buildModule(BuildSootModule buildSootModule) {
    log().debug("resolve the module");

    ActorRef classBuilderActor = context().actorOf(ClassBuilderActor.props(buildSootModule.classSource),
        buildSootModule.classSource.getClassSignature().toString());
    /*
     * Future<Object> future = Patterns.ask(classBuilderActor, "resolve", new Timeout(Duration.create(5, "seconds")));
     * future.onComplete(new OnSuccess<Try<Object>>() {
     *
     * @Override public void onSuccess(Try<Object> result) throws Throwable {
     *
     * } }, getContext().dispatcher());
     */
  }

  static public final class BuildSootClass {
    public final ClassSource classSource;

    public BuildSootClass(ClassSource classSource) {
      this.classSource = classSource;
    }
  }

  static public final class BuildSootModule {
    public final ClassSource classSource;

    public BuildSootModule(ClassSource classSource) {
      this.classSource = classSource;
    }
  }
}
