package de.upb.soot.namespaces.classprovider.asm.modules;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootModuleInfo;
import de.upb.soot.namespaces.classprovider.IClassProvider;

public class ResolveModuleVisitor extends org.objectweb.asm.ModuleVisitor {
  private final SootModuleInfo moduleInfo;

  private final de.upb.soot.signatures.ModuleSignatureFactory moduleSignatureFactory
      = new de.upb.soot.signatures.ModuleSignatureFactory();
  private final de.upb.soot.namespaces.classprovider.IClassProvider classProvider;

  /**
   * A @see org.objectweb.asm.ModuleVisitor to fully resolve a module-info.class file.
   * 
   * @param moduleInfo
   *          the @see SootModuleInfo to resolve
   * @param classProvider
   *          used to trigger resolving of dependent module-info
   */
  public ResolveModuleVisitor(de.upb.soot.core.SootModuleInfo moduleInfo, IClassProvider classProvider) {
    super(org.objectweb.asm.Opcodes.ASM6);
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
    de.upb.soot.signatures.ClassSignature moduleSignature
        = moduleSignatureFactory.getClassSignature("module-info", "", module);

    return (de.upb.soot.core.SootModuleInfo) classProvider.resolveSootClass(moduleSignature);
  }

  private Iterable<de.upb.soot.core.SootModuleInfo> resolveModules(String[] modules) {
    if (modules == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(modules).map(p -> resolveModule(p)).collect(java.util.stream.Collectors.toList());
  }

  private AbstractClass resolveService(String service) {

    return classProvider.resolveSootClass(moduleSignatureFactory.getClassSignature(service));
  }

  private Iterable<AbstractClass> resolveServices(String[] providers) {
    if (providers == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(providers).map(p -> resolveService(p)).collect(java.util.stream.Collectors.toList());
  }
}
