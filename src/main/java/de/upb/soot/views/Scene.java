package de.upb.soot.views;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import de.upb.soot.buildactor.ClassBuilderActor;
import de.upb.soot.buildactor.ModuleBuilderActor;
import de.upb.soot.buildactor.ReifyMessage;
import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Optional;

public class Scene {
    private ActorSystem system;

    public Optional<SootClass> getClass(ClassSignature signature) {
        Optional<SootClass> result = Optional.empty();
        // TODO: cache

        // TODO: decide for phantom

        Optional<ClassSource> source = pollNamespaces(signature).getClassSource(signature);

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
        if (source.getClassSignature().isModuleInfo())
            return system.actorOf(ModuleBuilderActor.props(source));
        return system.actorOf(ClassBuilderActor.props(source));
    }

    private INamespace pollNamespaces(ClassSignature signature) {
        // TODO: Traverse through namespaces

        return new JavaClassPathNamespace(null, "");
    }




}
