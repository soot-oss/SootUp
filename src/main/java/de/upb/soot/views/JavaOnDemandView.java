package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.ISignature;
import de.upb.soot.signatures.JavaClassSignature;

import java.util.Optional;

/**
 * The Class JavaOnDemandView loads Java Source Files on Demand.
 * 
 * @author Andreas Dann created on 31.10.2018
 */
public class JavaOnDemandView extends JavaView {

  private final INamespace namespace;
  private final AkkaClassResolver akkaClassResolver = new AkkaClassResolver();

  /**
   * Instantiates a new view.
   */
  public JavaOnDemandView(Project project, INamespace namespace) {
    super(project);
    this.namespace = namespace;
  }

  // Where and why should we decide which phantom classes to create?

  @Override
  public Optional<AbstractClass> getClass(ISignature signature) {
    Optional<AbstractClass> foundClasss
        = this.classes().filter(c -> c.getClassSource().getClassSignature().equals(signature)).findFirst();
    if (!foundClasss.isPresent()) {

      // query the namespace for the class source
      Optional<AbstractClassSource> source = namespace.getClassSource((JavaClassSignature) signature);
      if (source.isPresent()) {
        // resolve it ...
        Optional<AbstractClass> resolvedClass = null;
        // using akka
        resolvedClass = akkaClassResolver.reifyClass(source.get(), this);

        // add it to the existing
        if (resolvedClass.isPresent()) {
          this.classes.put(signature, resolvedClass.get());
        }
        return resolvedClass;
      }

    }

    return foundClasss;
  }

}
