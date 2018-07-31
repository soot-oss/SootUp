package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.common.type.AnySubType;
import de.upb.soot.jimple.common.type.ArrayType;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.NullType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.jimple.common.type.VoidType;

import com.google.common.graph.ElementOrder.Type;

public interface ITypeVisitor {

  void caseBooleanType(BooleanType t);

  void caseByteType(ByteType t);

  void caseCharType(CharType t);

  void caseShortType(ShortType t);

  void caseIntType(IntType t);

  void caseLongType(LongType t);

  void caseDoubleType(DoubleType t);

  void caseFloatType(FloatType t);

  void caseArrayType(ArrayType t);

  void caseRefType(RefType t);

  void caseAnySubType(AnySubType t);

  void caseNullType(NullType t);

  void caseUnknownType(UnknownType t);

  void caseVoidType(VoidType t);

  void caseDefault(Type t);
}
