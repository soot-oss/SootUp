package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;

import java.util.HashSet;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 * 
 * @author Linghui Luo created on 31.07.2018
 */
public class JavaView extends AbstractView {

  /**
   * Instantiates a new view.
   */
  public JavaView(Project project) {
    super(project);
  }


  @Override
  protected void setReservedNames() {
    this.reservedNames = new HashSet<>();
    this.reservedNames.add("newarray");
    this.reservedNames.add("newmultiarray");
    this.reservedNames.add("nop");
    this.reservedNames.add("ret");
    this.reservedNames.add("specialinvoke");
    this.reservedNames.add("staticinvoke");
    this.reservedNames.add("tableswitch");
    this.reservedNames.add("virtualinvoke");
    this.reservedNames.add("null_type");
    this.reservedNames.add("unknown");
    this.reservedNames.add("cmp");
    this.reservedNames.add("cmpg");
    this.reservedNames.add("cmpl");
    this.reservedNames.add("entermonitor");
    this.reservedNames.add("exitmonitor");
    this.reservedNames.add("interfaceinvoke");
    this.reservedNames.add("lengthof");
    this.reservedNames.add("lookupswitch");
    this.reservedNames.add("neg");
    this.reservedNames.add("if");
    this.reservedNames.add("abstract");
    this.reservedNames.add("annotation");
    this.reservedNames.add("boolean");
    this.reservedNames.add("break");
    this.reservedNames.add("byte");
    this.reservedNames.add("case");
    this.reservedNames.add("catch");
    this.reservedNames.add("char");
    this.reservedNames.add("class");
    this.reservedNames.add("enum");
    this.reservedNames.add("final");
    this.reservedNames.add("native");
    this.reservedNames.add("public");
    this.reservedNames.add("protected");
    this.reservedNames.add("private");
    this.reservedNames.add("static");
    this.reservedNames.add("synchronized");
    this.reservedNames.add("transient");
    this.reservedNames.add("volatile");
    this.reservedNames.add("interface");
    this.reservedNames.add("void");
    this.reservedNames.add("short");
    this.reservedNames.add("int");
    this.reservedNames.add("long");
    this.reservedNames.add("float");
    this.reservedNames.add("double");
    this.reservedNames.add("extends");
    this.reservedNames.add("implements");
    this.reservedNames.add("breakpoint");
    this.reservedNames.add("default");
    this.reservedNames.add("goto");
    this.reservedNames.add("instanceof");
    this.reservedNames.add("new");
    this.reservedNames.add("return");
    this.reservedNames.add("throw");
    this.reservedNames.add("throws");
    this.reservedNames.add("null");
    this.reservedNames.add("from");
    this.reservedNames.add("to");
    this.reservedNames.add("with");
    this.reservedNames.add("cls");
    this.reservedNames.add("dynamicinvoke");
    this.reservedNames.add("strictfp");
  }

  public void addSootClass(SootClass klass) {
    this.classes.add(klass);
  }

  @Override
  public SignatureFactory getSignatureFacotry() {
    return new DefaultSignatureFactory();
  }

}
