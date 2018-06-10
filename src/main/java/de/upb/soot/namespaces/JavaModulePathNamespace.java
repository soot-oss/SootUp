package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.ModulePackageSignature;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link INamespace} interface for the Java modulepath. Handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 * 
 * @author Manuel Benz created on 22.05.18
 */
public class JavaModulePathNamespace extends JavaClassPathNamespace {
  private static final Logger logger = LoggerFactory.getLogger(JavaModulePathNamespace.class);

  private Map<ModuleSignature, AbstractNamespace> moduleNamespace;

  /**
   * Creates a {@link JavaModulePathNamespace} which locates classes based on the provided {@link IClassProvider}.
   *
   * @param classProvider
   *          The {@link IClassProvider} for generating {@link ClassSource}es for the files found on the class path
   * @param modulePath
   *          The class path to search in
   */
  public JavaModulePathNamespace(IClassProvider classProvider, String modulePath) {
    super(classProvider, modulePath);

    // add the namespace for the jrt virtual file system
    cpEntries.add(new JrtFSNamespace(classProvider));

    moduleNamespace = discoverModules();

  }

  private Map<ModuleSignature, AbstractNamespace> discoverModules() {
    Map<ModuleSignature, AbstractNamespace> map = new HashMap<>();

    // TODO, implement module discovery...

    // iterate through all namespaces, and look for module-info file
    Optional<ClassSource> classSource;
    for (AbstractNamespace ns : cpEntries) {
      classSource = ns.getClassSource(ModuleSignatureFactory.MODULE_INFO_CLASS);
      if (classSource.isPresent()) {

      } else {
        // we have an automatic module?
      }
    }
    return map;

  }

  // IDEA: toPath Method wird teil der signature factory, und alle rufen einfach nurnoch topath auf...

  @Override
  public Collection<ClassSource> getClassSources(SignatureFactory factory) {
    // By using a set here, already added classes won't be overwritten and the class which is found
    // first will be kept

    // TODO: problem is the classprovider creates classSignatures without module information
    Set<ClassSource> found = new HashSet<>();
    for (AbstractNamespace ns : cpEntries) {
      found.addAll(ns.getClassSources(factory));
    }
    return found;
  }

  @Override
  public Optional<ClassSource> getClassSource(ClassSignature signature) {

    // here, we don't have any problems wrt. the classSignature, since the classprovider use the given signature
    // which is a module signature

    // take the ns for the class provider from the cache and use him...
    AbstractNamespace ns = moduleNamespace.get(((ModulePackageSignature) signature.packageSignature).moduleSignature);

    final Optional<ClassSource> classSource = ns.getClassSource(signature);
    if (classSource.isPresent()) {
      return classSource;
    }

    return Optional.empty();
  }

}
