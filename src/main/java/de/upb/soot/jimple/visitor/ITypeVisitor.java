package de.upb.soot.jimple.visitor;

import com.google.common.graph.ElementOrder.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.NullTypeSignature;
import de.upb.soot.signatures.PrimitiveTypeSignature;
import de.upb.soot.signatures.VoidTypeSignature;

public interface ITypeVisitor {

  void caseBooleanType(PrimitiveTypeSignature t);

  void caseByteType(PrimitiveTypeSignature t);

  void caseCharType(PrimitiveTypeSignature t);

  void caseShortType(PrimitiveTypeSignature t);

  void caseIntType(PrimitiveTypeSignature t);

  void caseLongType(PrimitiveTypeSignature t);

  void caseDoubleType(PrimitiveTypeSignature t);

  void caseFloatType(PrimitiveTypeSignature t);

  void caseArrayType(PrimitiveTypeSignature t);

  void caseRefType(JavaClassSignature t);

  void caseNullType(NullTypeSignature t);

  void caseUnknownType(/*UnknownType t*/ );

  void caseVoidType(VoidTypeSignature t);

  void caseDefault(Type t);
}
