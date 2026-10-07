package sootup.interceptors.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/**
 * {@code ObjectMethods.bootstrap} - the body of a record's {@code toString}, {@code hashCode} and
 * {@code equals} - lowered to the code it stands for, component by component (the {@code
 * REF_GET_FIELD} bootstrap arguments):
 *
 * <ul>
 *   <li>{@code toString}: {@code "R[a=" + a + ", b=" + b + "]"}.
 *   <li>{@code hashCode}: {@code h = 31 * h + hash(c)} from {@code h = 0}, with {@code
 *       Objects.hashCode} or the wrapper's static {@code hashCode} as {@code hash}.
 *   <li>{@code equals}: {@code this == o}, else {@code o instanceof R} and each component equal -
 *       {@code Objects.equals}, {@code Float/Double.compare(a, b) == 0} or {@code ==} - checked
 *       last to first, as {@code ObjectMethods} does.
 * </ul>
 *
 * Calls on object components are declared by the component type, so call graphs see the overriding
 * methods.
 */
public final class RecordMethodsDesugarizer implements InvokeDynamicDesugarizer {

  private static final String OBJECT_METHODS = "java.lang.runtime.ObjectMethods";

  private record Component(String name, FieldSignature field) {
    Type type() {
      return field.getType();
    }
  }

  @Override
  public boolean applies(@NonNull JDynamicInvokeExpr expr) {
    return InvokeDynamicDesugarizer.bootstrappedBy(expr, OBJECT_METHODS);
  }

  @Override
  public boolean desugar(
      @NonNull JDynamicInvokeExpr expr, @Nullable LValue result, @NonNull Fragment code) {
    List<Component> components = components(expr.getBootstrapArgs());
    if (components == null
        || expr.getArgCount() == 0
        || !(expr.getArg(0) instanceof Local self)
        || !(expr.getMethodSignature().getParameterType(0) instanceof ClassType record)) {
      return false;
    }
    switch (expr.getMethodSignature().getName()) {
      case "toString" -> toString(self, record, components, result, code);
      case "hashCode" -> hashCode(self, components, result, code);
      case "equals" -> {
        if (expr.getArgCount() < 2 || !(expr.getArg(1) instanceof Local other)) {
          return false;
        }
        equals(self, other, record, components, result, code);
      }
      default -> {
        return false;
      }
    }
    return true;
  }

  /** Bootstrap args: record class, {@code "a;b"} component names, one getter per component. */
  @Nullable
  private static List<Component> components(List<Immediate> bootstrapArgs) {
    if (bootstrapArgs.size() < 2 || !(bootstrapArgs.get(1) instanceof StringConstant names)) {
      return null;
    }
    String[] componentNames =
        names.getValue().isEmpty() ? new String[0] : names.getValue().split(";");
    if (componentNames.length != bootstrapArgs.size() - 2) {
      return null;
    }
    List<Component> components = new ArrayList<>();
    for (int i = 0; i < componentNames.length; i++) {
      if (!(bootstrapArgs.get(i + 2) instanceof MethodHandle handle)
          || handle.getKind() != MethodHandle.Kind.REF_GET_FIELD
          || !(handle.getReferenceSignature() instanceof FieldSignature field)) {
        return null;
      }
      components.add(new Component(componentNames[i], field));
    }
    return components;
  }

  private static void toString(
      Local self,
      ClassType record,
      List<Component> components,
      @Nullable LValue result,
      Fragment code) {
    StringBuilding string = new StringBuilding(code);
    String className = record.getClassName();
    String prefix = className.substring(className.lastIndexOf('$') + 1) + "[";
    for (Component component : components) {
      string.appendText(prefix + component.name() + "=");
      string.appendValue(load(self, component, code), component.type());
      prefix = ", ";
    }
    string.appendText(components.isEmpty() ? prefix + "]" : "]");
    string.finish(result);
  }

  private static void hashCode(
      Local self, List<Component> components, @Nullable LValue result, Fragment code) {
    PrimitiveType.IntType intType = PrimitiveType.getInt();
    Local hash = code.newLocal(intType);
    code.assign(hash, IntConstant.getInstance(0));
    for (Component component : components) {
      Local value = load(self, component, code);
      Local componentHash = code.newLocal(intType);
      if (component.type() instanceof PrimitiveType primitive) {
        code.assign(
            componentHash,
            code.staticCall(
                code.classType(wrapper(primitive)),
                "hashCode",
                intType,
                List.of(primitive),
                List.of(value)));
      } else {
        // Objects.hashCode(value)
        Fragment.Label isNull = code.newLabel();
        code.assign(componentHash, IntConstant.getInstance(0));
        code.ifGoto(Jimple.newEqExpr(value, NullConstant.getInstance()), isNull);
        code.assign(
            componentHash,
            code.instanceCall(
                value, declaringType(component, code), "hashCode", intType, List.of(), List.of()));
        code.bind(isNull);
      }
      Local scaled = code.newLocal(intType);
      code.assign(scaled, Jimple.newMulExpr(hash, IntConstant.getInstance(31)));
      code.assign(hash, Jimple.newAddExpr(scaled, componentHash));
    }
    if (result != null) {
      code.assign(result, hash);
    }
  }

  private static void equals(
      Local self,
      Local other,
      ClassType record,
      List<Component> components,
      @Nullable LValue result,
      Fragment code) {
    PrimitiveType.BooleanType booleanType = PrimitiveType.getBoolean();
    IntConstant falseValue = IntConstant.getInstance(0);
    Fragment.Label isTrue = code.newLabel();
    Fragment.Label done = code.newLabel();
    Local equal = code.newLocal(booleanType);
    code.assign(equal, falseValue);
    code.ifGoto(Jimple.newEqExpr(self, other), isTrue);
    Local isInstance = code.newLocal(booleanType);
    code.assign(isInstance, Jimple.newInstanceOfExpr(other, record));
    code.ifGoto(Jimple.newEqExpr(isInstance, falseValue), done);
    Local that = code.newLocal(record);
    code.assign(that, Jimple.newCastExpr(other, record));
    for (int i = components.size() - 1; i >= 0; i--) {
      Component component = components.get(i);
      Local mine = load(self, component, code);
      Local theirs = load(that, component, code);
      if (component.type() instanceof PrimitiveType.FloatType
          || component.type() instanceof PrimitiveType.DoubleType) {
        Local comparison = code.newLocal(PrimitiveType.getInt());
        code.assign(
            comparison,
            code.staticCall(
                code.classType(wrapper((PrimitiveType) component.type())),
                "compare",
                PrimitiveType.getInt(),
                List.of(component.type(), component.type()),
                List.of(mine, theirs)));
        code.ifGoto(Jimple.newNeExpr(comparison, falseValue), done);
      } else if (component.type() instanceof PrimitiveType.LongType) {
        Local comparison = code.newLocal(PrimitiveType.getInt());
        code.assign(comparison, Jimple.newCmpExpr(mine, theirs));
        code.ifGoto(Jimple.newNeExpr(comparison, falseValue), done);
      } else if (component.type() instanceof PrimitiveType) {
        code.ifGoto(Jimple.newNeExpr(mine, theirs), done);
      } else {
        // Objects.equals(mine, theirs)
        Fragment.Label same = code.newLabel();
        code.ifGoto(Jimple.newEqExpr(mine, theirs), same);
        code.ifGoto(Jimple.newEqExpr(mine, NullConstant.getInstance()), done);
        Local componentEqual = code.newLocal(booleanType);
        code.assign(
            componentEqual,
            code.instanceCall(
                mine,
                declaringType(component, code),
                "equals",
                booleanType,
                List.of(code.classType("java.lang.Object")),
                List.of(theirs)));
        code.ifGoto(Jimple.newEqExpr(componentEqual, falseValue), done);
        code.bind(same);
      }
    }
    code.bind(isTrue);
    code.assign(equal, IntConstant.getInstance(1));
    code.bind(done);
    if (result != null) {
      code.assign(result, equal);
    }
  }

  private static Local load(Local base, Component component, Fragment code) {
    Local value = code.newLocal(component.type());
    code.assign(value, Jimple.newInstanceFieldRef(base, component.field()));
    return value;
  }

  /** The component's class, or {@code Object} for an array. */
  private static ClassType declaringType(Component component, Fragment code) {
    return component.type() instanceof ClassType type ? type : code.classType("java.lang.Object");
  }

  private static String wrapper(PrimitiveType type) {
    if (type instanceof PrimitiveType.BooleanType) return "java.lang.Boolean";
    if (type instanceof PrimitiveType.CharType) return "java.lang.Character";
    if (type instanceof PrimitiveType.ByteType) return "java.lang.Byte";
    if (type instanceof PrimitiveType.ShortType) return "java.lang.Short";
    if (type instanceof PrimitiveType.IntType) return "java.lang.Integer";
    if (type instanceof PrimitiveType.LongType) return "java.lang.Long";
    if (type instanceof PrimitiveType.FloatType) return "java.lang.Float";
    return "java.lang.Double";
  }
}
