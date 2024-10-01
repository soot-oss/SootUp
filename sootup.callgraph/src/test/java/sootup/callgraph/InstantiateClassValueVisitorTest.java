package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.BooleanConstant;
import sootup.core.jimple.common.constant.DoubleConstant;
import sootup.core.jimple.common.constant.FloatConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.MethodType;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JAndExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JCmpExpr;
import sootup.core.jimple.common.expr.JCmpgExpr;
import sootup.core.jimple.common.expr.JCmplExpr;
import sootup.core.jimple.common.expr.JDivExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLengthExpr;
import sootup.core.jimple.common.expr.JMulExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.expr.JNegExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.expr.JOrExpr;
import sootup.core.jimple.common.expr.JPhiExpr;
import sootup.core.jimple.common.expr.JRemExpr;
import sootup.core.jimple.common.expr.JShlExpr;
import sootup.core.jimple.common.expr.JShrExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JSubExpr;
import sootup.core.jimple.common.expr.JUshrExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.expr.JXorExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class InstantiateClassValueVisitorTest {
  @Test
  public void testVisitor() {
    View view = new JavaView(new DefaultRuntimeAnalysisInputLocation());
    IdentifierFactory identifierFactory = view.getIdentifierFactory();

    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();
    List<Value> listWithAllValues = new ArrayList<>();
    fillList(listWithAllValues, view);
    List<ClassType> foundClassTypes = new ArrayList<>();

    for (Value value : listWithAllValues) {
      instantiateVisitor.init();
      value.accept(instantiateVisitor);
      ClassType classType = instantiateVisitor.getResult();
      if (classType != null) {
        foundClassTypes.add(classType);
      }
    }

    assertFalse(foundClassTypes.isEmpty());
    assertEquals(foundClassTypes.size(), 4);
    assertTrue(foundClassTypes.contains(identifierFactory.getClassType("java.lang.Object")));
    assertTrue(foundClassTypes.contains(identifierFactory.getClassType("java.lang.Boolean")));
    assertTrue(foundClassTypes.contains(identifierFactory.getClassType("java.lang.Byte")));
    assertTrue(foundClassTypes.contains(identifierFactory.getClassType("java.lang.Double")));
    assertFalse(foundClassTypes.contains(identifierFactory.getClassType("java.lang.String")));
  }

  private void fillList(List<Value> listWithAllValues, View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    // interesting cases
    PrimitiveType charType = identifierFactory.getPrimitiveType("char").orElse(null);
    assertNotNull(charType);

    listWithAllValues.add(new JNewExpr(identifierFactory.getClassType("java.lang.Object")));

    ArrayType charArrayType = new ArrayType(charType, 3);
    listWithAllValues.add(
        new JNewArrayExpr(
            identifierFactory.getClassType("java.lang.Boolean"),
            IntConstant.getInstance(3),
            identifierFactory));
    listWithAllValues.add(
        new JNewArrayExpr(
            new ArrayType(identifierFactory.getClassType("java.lang.Byte"), 3),
            IntConstant.getInstance(3),
            identifierFactory));
    listWithAllValues.add(
        new JNewArrayExpr(charType, IntConstant.getInstance(3), identifierFactory));
    listWithAllValues.add(
        new JNewArrayExpr(charArrayType, IntConstant.getInstance(3), identifierFactory));

    listWithAllValues.add(
        new JNewMultiArrayExpr(
            new ArrayType(identifierFactory.getClassType("java.lang.Double"), 3),
            Collections.singletonList(IntConstant.getInstance(3))));
    listWithAllValues.add(
        new JNewMultiArrayExpr(
            new ArrayType(charType, 3), Collections.singletonList(IntConstant.getInstance(3))));

    // default cases
    ClassType StringClass = identifierFactory.getClassType("java.lang.String");
    MethodSignature toStringMethod =
        identifierFactory.getMethodSignature(
            StringClass, new MethodSubSignature("toString", Collections.emptyList(), StringClass));
    FieldSignature stringField = new FieldSignature(StringClass, "a", StringClass);
    Immediate stringConstant = new StringConstant("String", StringClass);
    listWithAllValues.add(BooleanConstant.getInstance(true));
    listWithAllValues.add(DoubleConstant.getInstance(2.5));
    listWithAllValues.add(FloatConstant.getInstance(2.5f));
    listWithAllValues.add(IntConstant.getInstance(3));
    listWithAllValues.add(LongConstant.getInstance(3L));
    listWithAllValues.add(stringConstant);
    listWithAllValues.add(JavaJimple.getInstance().newEnumConstant("3", "EnumTest"));
    listWithAllValues.add(JavaJimple.getInstance().newClassConstant("java/lang/String"));
    listWithAllValues.add(JavaJimple.getInstance().newMethodHandle(toStringMethod, 5));
    listWithAllValues.add(new MethodType(toStringMethod.getSubSignature(), StringClass));
    listWithAllValues.add(new JAddExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JAndExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JCmpExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JCmpgExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JCmplExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JDivExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JEqExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JNeExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JGeExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JGtExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JLeExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JMulExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JOrExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JRemExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JShlExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JShrExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JUshrExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JSubExpr(stringConstant, stringConstant));
    listWithAllValues.add(new JXorExpr(stringConstant, stringConstant));
    listWithAllValues.add(
        new JSpecialInvokeExpr(
            new Local("a", StringClass),
            toStringMethod,
            Collections.singletonList(stringConstant)));
    listWithAllValues.add(
        new JVirtualInvokeExpr(
            new Local("a", StringClass),
            toStringMethod,
            Collections.singletonList(stringConstant)));
    listWithAllValues.add(
        new JInterfaceInvokeExpr(
            new Local("a", StringClass),
            toStringMethod,
            Collections.singletonList(stringConstant)));
    listWithAllValues.add(
        new JDynamicInvokeExpr(
            toStringMethod,
            Collections.singletonList(stringConstant),
            new MethodSignature(
                identifierFactory.getClassType(JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME),
                toStringMethod.getSubSignature()),
            Collections.singletonList(stringConstant)));
    listWithAllValues.add(new JCastExpr(stringConstant, StringClass));
    listWithAllValues.add(new JInstanceOfExpr(stringConstant, StringClass));
    listWithAllValues.add(new JLengthExpr(stringConstant));
    listWithAllValues.add(new JNegExpr(stringConstant));
    listWithAllValues.add(new JStaticFieldRef(stringField));
    listWithAllValues.add(new JInstanceFieldRef(new Local("a", StringClass), stringField));
    listWithAllValues.add(new JArrayRef(new Local("a", StringClass), stringConstant));
    listWithAllValues.add(new JParameterRef(StringClass, 3));
    listWithAllValues.add(new JCaughtExceptionRef(StringClass));
    listWithAllValues.add(new JThisRef(StringClass));
    listWithAllValues.add(new Local("a", StringClass));
    listWithAllValues.add(
        new JPhiExpr(Collections.singletonList(new Local("a", StringClass)), new HashMap<>()));
  }
}
