package de.upb.soot.namespaces;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.signatures.scope.JavaModule;
import de.upb.soot.signatures.scope.JavaModuleGraph;
import de.upb.soot.signatures.scope.ModuleFinder;
import de.upb.soot.types.GlobalTypeScope;
import de.upb.soot.types.JavaClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles
 * directories, archives (including wildcard denoted archives) as stated in the official
 * documentation:
 *
 * @author Andreas Dann created on 28.05.18
 * @see <a
 *     href=http://docs.oracle.com/javase/9/docs/api/java/lang/module/ModuleFinder.html#of-java.nio.file.Path...->ModuleFinder</a>
 */
public class JavaModulePathNamespace extends AbstractNamespace {
  private static final @Nonnull Logger logger =
      LoggerFactory.getLogger(JavaModulePathNamespace.class);

  private final JavaModuleGraph moduleGraph;

  public JavaModulePathNamespace(@Nonnull String modulePath) {
    this(modulePath, getDefaultClassProvider());
  }



  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes in the given module path.
   *
   * @param modulePath The class path to search in The {@link IClassProvider} for generating {@link
   *     ClassSource}es for the files found on the class path
   */
  public JavaModulePathNamespace(
      @Nonnull String modulePath, @Nonnull IClassProvider classProvider) {
    super(classProvider);

    moduleGraph = new JavaModuleGraph(new ModuleFinder(classProvider, modulePath));
  }

  public JavaModuleGraph getModuleGraph() {
    return moduleGraph;
  }

  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {

    Set<AbstractClassSource> found = new HashSet<>();
    Collection<JavaModule> availableModules = moduleGraph.getAllModules();
    for (JavaModule javaModule : availableModules) {
      AbstractNamespace ns = javaModule.getNamespace();
      if (ns == null) {
        continue;
      }
      // enrich the class sources with the Module scope...
      Collection<? extends AbstractClassSource> classSources =
          ns.getClassSources(identifierFactory);
      for (AbstractClassSource classSource : classSources) {
        classSource.getClassType().setScope(javaModule);
      }
      found.addAll(classSources);
    }

    return found;
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {

    if (signature.getScope() instanceof JavaModule) {
      return getClassSource(signature, (JavaModule) signature.getScope());
    } else if (signature.getScope() instanceof GlobalTypeScope) {
      // FIXME: return any matching class name, by checking the complete path...

    }
    return Optional.empty();
  }

  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature, JavaModule javaModule) {

    // lookup the ns for the class provider from the cache and use him...
    AbstractNamespace ns = javaModule.getNamespace();

    if (ns == null) {
      try {
        throw new ClassResolvingException("No Namespace for class " + signature);
      } catch (ClassResolvingException e) {
        e.printStackTrace();
        // FIXME: [JMP] Throwing exception and catching it immediately? This causes `ns` to remain
        // `null`.
      }
    }
    Optional<? extends AbstractClassSource> classSource = ns.getClassSource(signature);
    classSource.ifPresent(x -> x.getClassType().setScope(javaModule));
    return classSource;
  }
}
