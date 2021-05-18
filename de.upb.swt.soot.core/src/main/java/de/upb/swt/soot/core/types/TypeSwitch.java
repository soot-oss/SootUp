package de.upb.swt.soot.core.types;

abstract class TypeSwitch<V> {

  V caseDoubleType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseFloatType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseIntType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseByteType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseShortType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseCharType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseBooleanType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseLongType(PrimitiveType t) {
    return defaultCase(t);
  }

  V caseArrayType(ArrayType t) {
    return defaultCase(t);
  }

  V caseClassType(ClassType t) {
    return defaultCase(t);
  }

  V caseNullType(NullType t) {
    return defaultCase(t);
  }

  V caseVoidType(VoidType t) {
    return defaultCase(t);
  }

  V caseUnknownType(UnknownType t) {
    return defaultCase(t);
  }

  V defaultCase(Type t) {
    // this way (i.e. not as abstract methof) its not mandatory to override it if not necessary and
    // all other relevant cases are covered
    throw new RuntimeException("default case is not overridden for " + t);
  }
}
