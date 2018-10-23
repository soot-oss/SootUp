package de.upb.soot.buildactor;

import de.upb.soot.core.IMethod;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.views.IView;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class ClassBuilderActor extends AbstractLoggingActor {

  private final class ResolveMethodMessage {
  }

  private final IView view;
  private final AbstractClassSource classSource;
  private de.upb.soot.core.SootClass sootClass;

  public ClassBuilderActor(IView view, AbstractClassSource classSource) {
    this.view = view;
    this.classSource = classSource;
  }

  public static Props props(IView view, AbstractClassSource classSource) {
    return Props.create(ClassBuilderActor.class, view, classSource);
  }

  public static Props props(de.upb.soot.core.SootClass sootClass, de.upb.soot.core.SootMethod sootMethod) {
    return Props.create(MethodBuilderActor.class, sootClass, sootMethod);
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
    // FIXME: new content
    IClassProvider classProvider = classSource.getClassProvider();
    de.upb.soot.namespaces.classprovider.ISourceContent content = classProvider.getContent(classSource);

    // FIXME --- if module info ... dispatch
    // actually I don't want if, but dispatch based on type .. but hard for constructor calls...

    // FIXME: somewhere a soot class needs to be created ....
    sootClass = new SootClass(view, ResolvingLevel.DANGLING, classSource, null, null, null, null, null, null);
    sootClass.resolve(de.upb.soot.core.ResolvingLevel.SIGNATURES);

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reifying for [{}].", classSource.getClassSignature().toString());
  }

  private void resolve(ResolveMessage m) {
    log().info("Full reify for [{}].", classSource.getClassSignature().toString());
    if (sootClass == null) {
      throw new IllegalStateException();
    }

    for (IMethod i : sootClass.getMethods()) {
      SootMethod method = (SootMethod) i;
      akka.actor.ActorRef methodActor
          = getContext().actorOf(de.upb.soot.buildactor.ClassBuilderActor.props(sootClass, method));
      akka.util.Timeout timeout = new akka.util.Timeout(scala.concurrent.duration.Duration.create(5, "seconds"));
      scala.concurrent.Future<Object> cbFuture = akka.pattern.Patterns.ask(methodActor, new ResolveMethodMessage(), timeout);
    }

    sender().tell(sootClass, this.getSelf());

    log().info("Completed reify for [{}]", classSource.getClassSignature().toString());

    // we are done
    this.getSelf().tell("done", this.getSelf());
  }

  private class MethodBuilderActor extends akka.actor.AbstractLoggingActor {

    private final de.upb.soot.core.SootClass sootClass;
    private de.upb.soot.core.SootMethod method;

    public MethodBuilderActor(de.upb.soot.core.SootClass sootClass, de.upb.soot.core.SootMethod method) {
      this.sootClass = sootClass;
      this.method = method;
    }

    @Override
    public Receive createReceive() {
      return receiveBuilder().match(ResolveMethodMessage.class, this::resolveMethod)
          .matchEquals("done", m -> getContext().stop(getSelf())).build();
    }

    private void resolveMethod(ResolveMethodMessage m) {
      log().info("Start reifying method [{}].", method.getSignature().toString());

      // method.retrieveActiveBody();

      sender().tell(method, this.getSelf());

      log().info("Completed reifying method [{}].", method.getSignature().toString());
    }

  }

}
