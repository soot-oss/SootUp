package de.upb.soot.classprovider.asm.modules;

public class SootModuleBuilder extends org.objectweb.asm.ClassVisitor {

  private final org.objectweb.asm.ModuleVisitor visitor;
  public de.upb.soot.core.SootModuleInfo result;
  private de.upb.soot.classprovider.ClassSource source;

  /**
   * The module builder extends the @see org.objectweb.asm.ClassVisitor
   * 
   * @param source
   *          the class source from whicht to read the module info
   * @param visitor
   *          the visitor to build the module-info file
   */
  public SootModuleBuilder(de.upb.soot.classprovider.ClassSource source, org.objectweb.asm.ModuleVisitor visitor) {
    super(org.objectweb.asm.Opcodes.ASM6);
    this.source = source;
    this.visitor = visitor;
  }

  @Override
  public org.objectweb.asm.ModuleVisitor visitModule(String name, int access, String version) {
    result = new de.upb.soot.core.SootModuleInfo(source, name, access, version);

    return visitor;
  }
}
