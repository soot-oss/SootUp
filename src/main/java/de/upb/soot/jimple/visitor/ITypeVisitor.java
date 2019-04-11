package de.upb.soot.jimple.visitor;

import com.google.common.graph.ElementOrder.Type;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.types.NullType;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.VoidType;

public interface ITypeVisitor {

  void caseBooleanType(PrimitiveType t);

  void caseByteType(PrimitiveType t);

  void caseCharType(PrimitiveType t);

  void caseShortType(PrimitiveType t);

  void caseIntType(PrimitiveType t);

  void caseLongType(PrimitiveType t);

  void caseDoubleType(PrimitiveType t);

  void caseFloatType(PrimitiveType t);

  void caseArrayType(PrimitiveType t);

  void caseRefType(JavaClassType t);

  void caseNullType(NullType t);

  void caseUnknownType(/*UnknownType t*/ );

  void caseVoidType(VoidType t);

  void caseDefault(Type t);
}
