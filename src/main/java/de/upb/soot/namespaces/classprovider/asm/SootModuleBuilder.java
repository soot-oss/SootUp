package de.upb.soot.namespaces.classprovider.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

public class SootModuleBuilder extends ClassVisitor {

  public SootModuleBuilder() {
    super(Opcodes.ASM6);
  }

  @Override
  public ModuleVisitor visitModule(String name, int access, String version) {
    return new SootModuleInfoVisitor();
  }

  private class SootModuleInfoVisitor extends ModuleVisitor {
    public SootModuleInfoVisitor() {
      super(Opcodes.ASM6);
    }

    @Override
    public void visitRequire(String module, int access, String version) {
      super.visitRequire(module, access, version);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
      super.visitExport(packaze, access, modules);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
      super.visitOpen(packaze, access, modules);
    }

    @Override
    public void visitUse(String service) {
      super.visitUse(service);
    }

    @Override
    public void visitProvide(String service, String... providers) {
      super.visitProvide(service, providers);
    }
  }

}
