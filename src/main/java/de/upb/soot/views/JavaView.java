package de.upb.soot.views;

import de.upb.soot.Project;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.NullType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.ArrayTypeSignature;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.NullTypeSignature;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.SignatureFactory;
import de.upb.soot.signatures.TypeSignature;

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

  @Override
  public SignatureFactory getSignatureFacotry() {
    return new DefaultSignatureFactory();
  }

  @Override
  public Type getType(TypeSignature signature) {
    if (signature instanceof PrimitiveTypeSignature) {
      if (signature.equals(PrimitiveTypeSignature.BYTE_TYPE_SIGNATURE)) {
        return ByteType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.SHORT_TYPE_SIGNATURE)) {
        return ShortType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.INT_TYPE_SIGNATURE)) {
        return IntType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.LONG_TYPE_SIGNATURE)) {
        return LongType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.FLOAT_TYPE_SIGNATURE)) {
        return FloatType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.DOUBLE_TYPE_SIGNATURE)) {
        return DoubleType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.BOOLEAN_TYPE_SIGNATURE)) {
        return BooleanType.getInstance();
      } else if (signature.equals(PrimitiveTypeSignature.CHAR_TYPE_SIGNATURE)) {
        return CharType.getInstance();
      } else {
        throw new RuntimeException("Unsupported PrimitiveTypeSignature: " + signature.toString());
      }
    }

    else if (signature instanceof NullTypeSignature) {
      return NullType.getInstance();
    } else if (signature instanceof JavaClassSignature) {
      return getRefType(signature);
    } else if (signature instanceof ArrayTypeSignature) {
      // TODO:
      throw new RuntimeException("Unsupported ArrayTypeSignature: " + signature.toString());
    } else {
      throw new RuntimeException("Unsupported TypeSignature: " + signature.toString());
    }
  }

}
