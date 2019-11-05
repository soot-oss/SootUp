package de.upb.swt.soot.core.jimple.visitor;

import com.google.common.graph.ElementOrder.Type;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.VoidType;

public interface TypeVisitor {

  void caseBooleanType(PrimitiveType t);

  void caseByteType(PrimitiveType t);

  void caseCharType(PrimitiveType t);

  void caseShortType(PrimitiveType t);

  void caseIntType(PrimitiveType t);

  void caseLongType(PrimitiveType t);

  void caseDoubleType(PrimitiveType t);

  void caseFloatType(PrimitiveType t);

  void caseArrayType(PrimitiveType t);

  void caseRefType(ClassType t);

  void caseNullType(NullType t);

  void caseUnknownType(/*UnknownType t*/ );

  void caseVoidType(VoidType t);

  void caseDefault(Type t);
}
