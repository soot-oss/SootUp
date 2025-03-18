/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package sootup.java.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootField;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * @author Markus Schmidt, Linghui Luo
 */
public class JInvokeStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();

    JavaIdentifierFactory dif = JavaIdentifierFactory.getInstance();

    Path dummyPath = Paths.get(URI.create("file:/nonexistent.java"));
    JavaClassType superClassSignature = dif.getClassType("java.lang.Object");
    Set<JavaSootField> fields = new LinkedHashSet<>();
    Set<JavaSootMethod> methods = new LinkedHashSet<>();
    OverridingJavaClassSource javaClassSource =
        new OverridingJavaClassSource(
            new EagerInputLocation(),
            dummyPath,
            dif.getClassType("de.upb.sootup.instructions.stmt.IdentityStmt"),
            superClassSignature,
            new HashSet<>(),
            null,
            fields,
            methods,
            NoPositionInformation.getInstance(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());

    SootClass sootClass = new SootClass(javaClassSource, SourceType.Application);

    // JStaticInvokeExpr
    MethodSignature statMethodSig =
        dif.getMethodSignature(
            "java.system.Out", "print", "void", Collections.singletonList("String"));
    Stmt staticInvokeStmt =
        new JInvokeStmt(
            new JStaticInvokeExpr(
                statMethodSig,
                Collections.singletonList(JavaJimple.getInstance().newStringConstant("Towel"))),
            nop);

    // toString
    assertEquals(
        "staticinvoke <java.system.Out: void print(String)>(\"Towel\")",
        staticInvokeStmt.toString());

    // equivTo
    assertFalse(staticInvokeStmt.equivTo(new JNopStmt(nop)));
    assertTrue(staticInvokeStmt.equivTo(staticInvokeStmt));

    // JSpecialInvoke
    MethodSignature smethodSig =
        dif.getMethodSignature("java.lang.Object", "<init>", "void", Collections.emptyList());
    Stmt specialInvokeStmt =
        new JInvokeStmt(
            new JSpecialInvokeExpr(
                new Local("r0", sootClass.getType()), smethodSig, Collections.emptyList()),
            nop);

    // toString
    assertEquals(
        "specialinvoke r0.<java.lang.Object: void <init>()>()", specialInvokeStmt.toString());

    // equivTo
    assertFalse(specialInvokeStmt.equivTo(new JNopStmt(nop)));
    assertTrue(specialInvokeStmt.equivTo(specialInvokeStmt));

    // JInterfaceInvoke
    MethodSignature imethodSig =
        dif.getMethodSignature("java.util.Iterator", "remove", "void", Collections.emptyList());
    Stmt interfaceInvokeStmt =
        new JInvokeStmt(
            new JInterfaceInvokeExpr(
                new Local("r2", sootClass.getType()), imethodSig, Collections.emptyList()),
            nop);

    // toString
    assertEquals(
        "interfaceinvoke r2.<java.util.Iterator: void remove()>()", interfaceInvokeStmt.toString());

    // equivTo
    assertFalse(interfaceInvokeStmt.equivTo(new JNopStmt(nop)));
    assertTrue(interfaceInvokeStmt.equivTo(interfaceInvokeStmt));

    // JDynamicInvoke
    MethodSignature dmethodSig =
        dif.getMethodSignature(
            JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME,
            "mylambda",
            "void",
            Collections.emptyList());
    MethodSignature bootstrapMethodSig =
        dif.getMethodSignature("Runnable", "run", "void", Collections.emptyList());
    List<Immediate> bootstrapArgs = Collections.emptyList();
    List<Immediate> methodArgs = Collections.emptyList();

    Stmt dynamicInvokeStmt =
        new JInvokeStmt(
            new JDynamicInvokeExpr(bootstrapMethodSig, bootstrapArgs, dmethodSig, methodArgs), nop);

    // toString
    assertEquals(
        "dynamicinvoke \"mylambda\" <void ()>() <Runnable: void run()>()",
        dynamicInvokeStmt.toString());

    // equivTo
    assertFalse(dynamicInvokeStmt.equivTo(new JNopStmt(nop)));
    assertTrue(dynamicInvokeStmt.equivTo(dynamicInvokeStmt));

    // general
    assertFalse(staticInvokeStmt.equivTo(specialInvokeStmt));
    assertFalse(staticInvokeStmt.equivTo(interfaceInvokeStmt));
    assertFalse(staticInvokeStmt.equivTo(dynamicInvokeStmt));

    assertFalse(specialInvokeStmt.equivTo(staticInvokeStmt));
    assertFalse(specialInvokeStmt.equivTo(interfaceInvokeStmt));
    assertFalse(specialInvokeStmt.equivTo(dynamicInvokeStmt));

    assertFalse(interfaceInvokeStmt.equivTo(staticInvokeStmt));
    assertFalse(interfaceInvokeStmt.equivTo(specialInvokeStmt));
    assertFalse(interfaceInvokeStmt.equivTo(dynamicInvokeStmt));

    assertFalse(dynamicInvokeStmt.equivTo(staticInvokeStmt));
    assertFalse(dynamicInvokeStmt.equivTo(specialInvokeStmt));
    assertFalse(dynamicInvokeStmt.equivTo(interfaceInvokeStmt));
  }
}
