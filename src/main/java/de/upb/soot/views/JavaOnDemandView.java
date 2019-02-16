package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.JavaClassSignature;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * The Class JavaOnDemandView loads Java Source Files on Demand.
 * 
 * @author Andreas Dann created on 31.10.2018
 */
public class JavaOnDemandView extends JavaView {

  private final AkkaClassResolver akkaClassResolver = new AkkaClassResolver();

  /**
   * Instantiates a new view.
   */
  public JavaOnDemandView(@Nonnull Project project) {
    super(project);
  }

  // Where and why should we decide which phantom classes to create?

  @Override
  public @Nonnull Optional<AbstractClass> getClass(@Nonnull ISignature signature) {
    if(!(signature instanceof JavaClassSignature)) {
      throw new IllegalArgumentException("The signature must be a `JavaClassSignature`.");
    }

    Optional<AbstractClass> foundClass =
      this.classes()
      .filter(c -> c.getClassSource().getClassSignature().equals(signature))
      .findFirst();

    if (!foundClass.isPresent()) {
      // query the namespace for the class source
      Optional<ClassSource> source = this.project.getNamespace().getClassSource((JavaClassSignature) signature);

      if (source.isPresent()) {
        // resolve it ... using akka
        Optional<AbstractClass> resolvedClass  = akkaClassResolver.reifyClass(source.get(), this);

        // add it to the existing
        resolvedClass.ifPresent(it -> this.classes.put(signature, it));

        return resolvedClass;
      }

    }

    return foundClass;
  }

}
