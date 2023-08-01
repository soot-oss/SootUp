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

import categories.Java8Test;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.language.JavaJimple;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JInvokeStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();

    JavaIdentifierFactory dif = JavaIdentifierFactory.getInstance();

    Path dummyPath = Paths.get(URI.create("file:/nonexistent.java"));
    ClassType superClassSignature = dif.getClassType("java.lang.Object");
    Set<SootField> fields = new LinkedHashSet<>();
    Set<SootMethod> methods = new LinkedHashSet<>();
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
            "print", "java.system.Out", "void", Collections.singletonList("String"));
    Stmt staticInvokeStmt =
        new JInvokeStmt(
            new JStaticInvokeExpr(
                statMethodSig,
                Collections.singletonList(JavaJimple.getInstance().newStringConstant("Towel"))),
            nop);

    // toString
    Assert.assertEquals(
        "staticinvoke <java.system.Out: void print(String)>(\"Towel\")",
        staticInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(staticInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(staticInvokeStmt.equivTo(staticInvokeStmt));

    // JSpecialInvoke
    MethodSignature smethodSig =
        dif.getMethodSignature("<init>", "java.lang.Object", "void", Collections.emptyList());
    Stmt specialInvokeStmt =
        new JInvokeStmt(
            new JSpecialInvokeExpr(
                new Local("$r0", sootClass.getType()), smethodSig, Collections.emptyList()),
            nop);

    // toString
    Assert.assertEquals(
        "specialinvoke $r0.<java.lang.Object: void <init>()>()", specialInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(specialInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(specialInvokeStmt.equivTo(specialInvokeStmt));

    // JInterfaceInvoke
    MethodSignature imethodSig =
        dif.getMethodSignature("remove", "java.util.Iterator", "void", Collections.emptyList());
    Stmt interfaceInvokeStmt =
        new JInvokeStmt(
            new JInterfaceInvokeExpr(
                new Local("r2", sootClass.getType()), imethodSig, Collections.emptyList()),
            nop);

    // toString
    Assert.assertEquals(
        "interfaceinvoke r2.<java.util.Iterator: void remove()>()", interfaceInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(interfaceInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(interfaceInvokeStmt.equivTo(interfaceInvokeStmt));

    // JDynamicInvoke
    MethodSignature dmethodSig =
        dif.getMethodSignature(
            "mylambda",
            JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME,
            "void",
            Collections.emptyList());
    MethodSignature bootstrapMethodSig =
        dif.getMethodSignature("run", "Runnable", "void", Collections.emptyList());
    List<Immediate> bootstrapArgs = Collections.emptyList();
    List<Immediate> methodArgs = Collections.emptyList();

    Stmt dynamicInvokeStmt =
        new JInvokeStmt(
            new JDynamicInvokeExpr(bootstrapMethodSig, bootstrapArgs, dmethodSig, methodArgs), nop);

    // toString
    Assert.assertEquals(
        "dynamicinvoke \"mylambda\" <void ()>() <Runnable: void run()>()",
        dynamicInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(dynamicInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(dynamicInvokeStmt.equivTo(dynamicInvokeStmt));

    // general
    Assert.assertFalse(staticInvokeStmt.equivTo(specialInvokeStmt));
    Assert.assertFalse(staticInvokeStmt.equivTo(interfaceInvokeStmt));
    Assert.assertFalse(staticInvokeStmt.equivTo(dynamicInvokeStmt));

    Assert.assertFalse(specialInvokeStmt.equivTo(staticInvokeStmt));
    Assert.assertFalse(specialInvokeStmt.equivTo(interfaceInvokeStmt));
    Assert.assertFalse(specialInvokeStmt.equivTo(dynamicInvokeStmt));

    Assert.assertFalse(interfaceInvokeStmt.equivTo(staticInvokeStmt));
    Assert.assertFalse(interfaceInvokeStmt.equivTo(specialInvokeStmt));
    Assert.assertFalse(interfaceInvokeStmt.equivTo(dynamicInvokeStmt));

    Assert.assertFalse(dynamicInvokeStmt.equivTo(staticInvokeStmt));
    Assert.assertFalse(dynamicInvokeStmt.equivTo(specialInvokeStmt));
    Assert.assertFalse(dynamicInvokeStmt.equivTo(interfaceInvokeStmt));
  }
}
