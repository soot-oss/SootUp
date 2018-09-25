package de.upb.soot.views;

import de.upb.soot.buildactor.ClassBuilderActor;
import de.upb.soot.buildactor.ModuleBuilderActor;
import de.upb.soot.buildactor.ReifyMessage;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.util.Numberer;
import de.upb.soot.util.StringNumberer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Scene {
  private ActorSystem system;

  public Optional<SootClass> getClass(ClassSignature signature) {
    Optional<SootClass> result = Optional.empty();
    // TODO: cache

    // TODO: decide for phantom

    Optional<ClassSource> source = pollNamespaces(signature).getClassSource(signature);
    // MB: consider using source.flatMap(#methodRef) here. methodRef can than point to the actual logic for class resolution
    if (source.isPresent()) {

      ActorRef cb = createActor(source.get());
      Timeout timeout = new Timeout(Duration.create(5, "seconds"));
      Future<Object> cbFuture = Patterns.ask(cb, new ReifyMessage(), timeout);
      try {
        result = Optional.of((SootClass) Await.result(cbFuture, timeout.duration()));
      } catch (Exception e) {
        // TODO: Do something meaningful here
      }
    }

    return result;
  }

  private ActorRef createActor(ClassSource source) {
    if (source.getClassSignature().isModuleInfo()) {
      return system.actorOf(ModuleBuilderActor.props(source));
    }
    return system.actorOf(ClassBuilderActor.props(source));
  }

  private INamespace pollNamespaces(ClassSignature signature) {
    // TODO: Traverse through namespaces
    // MB this does not make sense in our current impl. you would rather poll the namespaces one after another and if one
    // returns a non-empty optional, you would pass that to the caller
    return new JavaClassPathNamespace(null, "");
  }

  public static Scene getInstance() {
    return new Scene();
  }

  public List<Type> getTypeNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Local> getLocalNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public SootClass getSootClass(String readObject) {
    // TODO Auto-generated method stub
    return null;
  }

  public RefType getObjectType() {
    // TODO Auto-generated method stub
    return null;
  }

  public String quotedNameOf(String s) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean doneResolving() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean allowsPhantomRefs() {
    // TODO Auto-generated method stub
    return false;
  }

  public Numberer<Object> getFieldNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public StringNumberer getSubSigNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getClassNumberer() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<Object> getApplicationClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getLibraryClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection<SootClass> getPhantomClasses() {
    // TODO Auto-generated method stub
    return null;

  }

  public void addRefType(RefType refType) {
    // TODO Auto-generated method stub
    return;

  }

  public Numberer<SootMethod> getMethodNumberer() {
    // TODO Auto-generated method stub
    return null;

  }
}
