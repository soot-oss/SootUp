package de.upb.soot.namespaces.classprovider.asm.modules;

public class ResolveModuleVisitor extends org.objectweb.asm.ModuleVisitor {
  private final de.upb.soot.core.SootModuleInfo moduleInfo;
  private final de.upb.soot.namespaces.classprovider.ClassProvider classProvider;

  /**
   * A @see org.objectweb.asm.ModuleVisitor to fully resolve a module-info.class file.
   * @param moduleInfo the @see SootModuleInfo to resolve
   * @param classProvider used to trigger resolving of dependent module-info
   */
  public ResolveModuleVisitor(de.upb.soot.core.SootModuleInfo moduleInfo,
      de.upb.soot.namespaces.classprovider.ClassProvider classProvider) {
    super(org.objectweb.asm.Opcodes.ASM7);
    this.moduleInfo = moduleInfo;
    this.classProvider = classProvider;
  }

  @Override
  public void visitRequire(String module, int access, String version) {

    moduleInfo.addRequire(resolveModule(module), access, version);
  }

  @Override
  public void visitExport(String packaze, int access, String... modules) {

    moduleInfo.addExport(packaze, access, resolveModules(modules));
  }

  @Override
  public void visitOpen(String packaze, int access, String... modules) {
    moduleInfo.addOpen(packaze, access, resolveModules(modules));

  }

  @Override
  public void visitUse(String service) {
    moduleInfo.addUse(resolveService(service));

  }

  @Override
  public void visitProvide(String service, String... providers) {

    moduleInfo.addProvide(service, resolveServices(providers));
  }

  private de.upb.soot.core.SootModuleInfo resolveModule(String module) {
    // TODO: I don't like this cast
    de.upb.soot.signatures.ClassSignature moduleSignature
        = ((de.upb.soot.signatures.ModuleSignatureFactory) classProvider.getScene().getSignatureFactory())
            .getClassSignature("module-info", "", module);
    java.util.Optional<de.upb.soot.core.SootClass> moduleClass = classProvider.getScene().getClass(moduleSignature);

    // FIXME Ugly ugly cast... *würg*
    return (de.upb.soot.core.SootModuleInfo) moduleClass.get();
  }

  private Iterable<de.upb.soot.core.SootModuleInfo> resolveModules(String[] modules) {
    if (modules == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(modules).map(p -> resolveModule(p)).collect(java.util.stream.Collectors.toList());
  }

  private de.upb.soot.core.SootClass resolveService(String service) {

    return classProvider.resolveSootClass(classProvider.getScene().getSignatureFactory().getClassSignature(service));
  }

  private Iterable<de.upb.soot.core.SootClass> resolveServices(String[] providers) {
    if (providers == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(providers).map(p -> resolveService(p)).collect(java.util.stream.Collectors.toList());
  }
}
