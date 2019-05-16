package de.upb.soot.namespaces;

import de.upb.soot.IdentifierFactory;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.ModuleClassSource;
import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.scope.JavaModule;
import de.upb.soot.types.GlobalTypeScope;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.util.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for {@link INamespace}s that can be located by a {@link Path} object.
 *
 * @author Andreas Dann created on 06.06.18
 */
public class JrtFileSystemNamespace extends AbstractNamespace {

  private FileSystem theFileSystem = FileSystems.getFileSystem(URI.create("jrt:/"));

  private final HashMap<String, JavaSystemModule> javaSystemModuleHashMap = new HashMap<>();

  public JrtFileSystemNamespace(IClassProvider classProvider) {
    super(classProvider);
  }

  @Override
  public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
      @Nonnull JavaClassType signature) {

    if (signature.getScope() instanceof JavaSystemModule) {
      return this.getClassSourceInternalForModule(
          signature, (JavaSystemModule) signature.getScope());
    } else if (signature.getScope() instanceof GlobalTypeScope) {
      // FIXME: return any matching class name, by checking the complete path...
      this.getClassSourceInternalForClassPath(signature);
    }
    return Optional.empty();
  }

  private @Nonnull Optional<AbstractClassSource> getClassSourceInternalForClassPath(
      @Nonnull JavaClassType classSignature) {

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path moduleRoot = theFileSystem.getPath("modules");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          // check each module folder for the class
          Path foundfile = entry.resolve(filepath);
          if (Files.isRegularFile(foundfile)) {
            return Optional.of(classProvider.createClassSource(this, foundfile, classSignature));
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  private @Nonnull Optional<? extends AbstractClassSource> getClassSourceInternalForModule(
      @Nonnull JavaClassType classSignature, JavaSystemModule moduleSignature) {

    Path filepath = classSignature.toPath(classProvider.getHandledFileType(), theFileSystem);
    final Path module = theFileSystem.getPath("modules", moduleSignature.getName());
    Path foundClass = module.resolve(filepath);

    if (Files.isRegularFile(foundClass)) {
      return Optional.of(classProvider.createClassSource(this, foundClass, classSignature));

    } else {
      return Optional.empty();
    }
  }

  // get the factory, which I should use the create the correspond class signatures
  @Override
  public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
      @Nonnull IdentifierFactory identifierFactory) {

    final Path archiveRoot = theFileSystem.getPath("modules");
    return walkDirectory(archiveRoot, identifierFactory);
  }

  protected @Nonnull Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull IdentifierFactory identifierFactory) {

    final FileType handledFileType = classProvider.getHandledFileType();
    try {
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  Utils.optionalToStream(
                      Optional.of(
                          classProvider.createClassSource(
                              this,
                              p,
                              this.fromPath(
                                  p.subpath(2, p.getNameCount()),
                                  p.subpath(1, 2),
                                  identifierFactory)))))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Discover and return all modules contained in the jrt filesystem.
   *
   * @return Collection of found module names.
   */
  // FIXME: I'm not a fan of defining the code here, but I cannot think of a better solution for
  // now...
  public @Nonnull Collection<JavaModule> discoverModules() {
    final Path moduleRoot = theFileSystem.getPath("modules");
    List<JavaModule> foundModules = new ArrayList<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(moduleRoot)) {
      {
        for (Path entry : stream) {
          if (Files.isDirectory(entry)) {
            // get the module-info.class
            Path moduledesc =
                entry.resolve(
                    ModuleSignature.MODULE_INFO_CLASS.toPath(
                        classProvider.getHandledFileType(), theFileSystem));
            AbstractClassSource classSource =
                classProvider.createClassSource(
                    this, moduledesc, ModuleSignature.MODULE_INFO_CLASS);

            // create the system Java Modules
            SootModuleInfo moduleInfo = new SootModuleInfo((ModuleClassSource) classSource);

            String moduleName = entry.subpath(1, 2).toString();
            JavaSystemModule systemModule =
                new JavaSystemModule(moduleInfo, entry, moduleName, this);

            this.javaSystemModuleHashMap.put(moduleName, systemModule);

            foundModules.add(systemModule);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return foundModules;
  }

  private static class JavaSystemModule extends JavaModule {

    private String systemModuleName;

    public JavaSystemModule(
        @Nonnull SootModuleInfo sootModuleInfo,
        @Nonnull Path aPath,
        String name,
        INamespace namespace) {
      super(sootModuleInfo, aPath, namespace);
      this.systemModuleName = name;
    }

    @Override
    public String getName() {
      return systemModuleName;
    }
  }

  private @Nonnull JavaClassType fromPath(
      final Path filename, final Path moduleDir, final IdentifierFactory identifierFactory) {

    JavaClassType sig = identifierFactory.fromPath(filename);
    String moduleName = moduleDir.toString();

    // FIXME: IMHO: it does not make sense to discover all JDK modules directly...
    if (this.javaSystemModuleHashMap.isEmpty()) {
      this.discoverModules();
    }

    JavaSystemModule scope = this.javaSystemModuleHashMap.get(moduleName);

    sig.setScope(scope);
    return sig;
  }
}
