package de.upb.soot.core.buildactor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import de.upb.soot.core.frontend.AbstractClassSource;
import de.upb.soot.core.frontend.ClassSource;
import de.upb.soot.core.frontend.ResolveException;
import de.upb.soot.core.model.AbstractClass;
import de.upb.soot.core.model.SootClass;
import de.upb.soot.core.model.SootMethod;
import de.upb.soot.core.views.View;

/* INFO: test can be found in de.upb.soot.tests package */
public class ClassBuilderActor extends AbstractLoggingActor {

  private final class ResolveMethodMessage {}

  private final View view;
  private final ClassSource classSource;
  private AbstractClass<? extends AbstractClassSource> sootClass;

  public ClassBuilderActor(View view, ClassSource classSource) {
    this.view = view;
    this.classSource = classSource;
  }

  public static Props props(View view, ClassSource classSource) {
    return Props.create(ClassBuilderActor.class, view, classSource);
  }

  public static Props props(
      AbstractClass<? extends AbstractClassSource> sootClass, SootMethod sootMethod) {
    return Props.create(MethodBuilderActor.class, sootClass, sootMethod);
  }

  // this should become just logic for akka, e.g., when to create new actors, etc, the resolving
  // should happen in the
  // classprovider

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(ReifyMessage.class, this::reify)
        .match(ResolveMessage.class, this::resolve)
        .matchEquals("done", m -> getContext().stop(getSelf()))
        .build();
  }

  private void reify(ReifyMessage m) {
    log().info("Start reifying for [{}].", classSource.getClassType().toString());
    // FIXME: new content
    AbstractClassSource content = classSource;

    // FIXME --- if module info ... dispatch
    // actually I don't want if clauses. I want to dispatch based on the type of the classSource?

    // FIXME: somewhere a soot class needs to be created or returned???
    AbstractClass<? extends AbstractClassSource> sootClass = null;
    try {
      // TODO Fix this, resolveClass no longer exists
      //      sootClass = content.resolveClass(ResolvingLevel.DANGLING, view);
    } catch (ResolveException e) {
      e.printStackTrace();
      // FIXME: error handling
    }

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reifying for [{}].", classSource.getClassType().toString());
  }

  private void resolve(ResolveMessage m) {
    log().info("Full reify for [{}].", classSource.getClassType().toString());
    if (sootClass == null) {
      throw new IllegalStateException();
    }

    for (Object i : sootClass.getMethods()) {
      SootMethod method = (SootMethod) i;
      akka.actor.ActorRef methodActor =
          getContext().actorOf(ClassBuilderActor.props(sootClass, method));
      akka.util.Timeout timeout =
          new akka.util.Timeout(scala.concurrent.duration.Duration.create(5, "seconds"));
      scala.concurrent.Future<Object> cbFuture =
          akka.pattern.Patterns.ask(methodActor, new ResolveMethodMessage(), timeout);
    }

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reify for [{}]", classSource.getClassType().toString());

    // we are done
    this.getSelf().tell("done", this.getSelf());
  }

  private class MethodBuilderActor extends akka.actor.AbstractLoggingActor {

    private final SootClass sootClass;
    private SootMethod method;

    public MethodBuilderActor(SootClass sootClass, SootMethod method) {
      this.sootClass = sootClass;
      this.method = method;
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder()
          .match(ResolveMethodMessage.class, this::resolveMethod)
          .matchEquals("done", m -> getContext().stop(getSelf()))
          .build();
    }

    private void resolveMethod(ResolveMethodMessage m) {
      log().info("Start reifying methodRef [{}].", method.getSignature().toString());

      // methodRef.retrieveActiveBody();

      sender().tell(method, this.getSelf());

      log().info("Completed reifying methodRef [{}].", method.getSignature().toString());
    }
  }
}
