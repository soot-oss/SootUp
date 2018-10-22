package de.upb.soot.namespaces.classprovider.asm.modules;

import de.upb.soot.signatures.ModuleSignature;
import de.upb.soot.signatures.ModuleSignatureFactory;
import de.upb.soot.views.IView;

public class SootModuleBuilder extends org.objectweb.asm.ClassVisitor {

  private final org.objectweb.asm.ModuleVisitor visitor;
  public de.upb.soot.core.SootModuleInfo result;
  private de.upb.soot.namespaces.classprovider.AbstractClassSource source;
  private IView view;

  /**
   * The module builder extends the @see org.objectweb.asm.ClassVisitor
   * 
   * @param source
   *          the class source from whicht to read the module info
   * @param visitor
   *          the visitor to build the module-info file
   */
  public SootModuleBuilder(IView view, de.upb.soot.namespaces.classprovider.AbstractClassSource source,
      org.objectweb.asm.ModuleVisitor visitor) {
    super(org.objectweb.asm.Opcodes.ASM6);

    this.source = source;
    this.visitor = visitor;
    this.view = view;
  }

  @Override
  public org.objectweb.asm.ModuleVisitor visitModule(String name, int access, String version) {
    ModuleSignature moduleSignature = new ModuleSignatureFactory().getModuleSignature(name);
    result = new de.upb.soot.core.SootModuleInfo(view, source, moduleSignature, access, version);

    return visitor;
  }
}
