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

package de.upb.soot.jimple.common.stmt;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.EagerJavaClassSource;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.NoPositionInformation;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.StringConstant;
import de.upb.soot.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JInvokeStmtTest {

  @Test
  public void test() {
    PositionInfo nop = PositionInfo.createNoPositionInfo();

    DefaultFactories factories = DefaultFactories.create();
    DefaultSignatureFactory dsm = factories.getSignatureFactory();
    DefaultTypeFactory dtf = factories.getTypeFactory();

    Path dummyPath = Paths.get(URI.create("file:/C:/nonexistent.java"));
    JavaClassType superClassSignature = dtf.getClassType("java.lang.Object");
    Set<SootField> fields = new LinkedHashSet<>();
    Set<SootMethod> methods = new LinkedHashSet<>();
    EagerJavaClassSource javaClassSource =
        new EagerJavaClassSource(
            new JavaClassPathNamespace("src/main/java/de/upb/soot"),
            dummyPath,
            dtf.getClassType("de.upb.soot.instructions.stmt.IdentityStmt"),
            superClassSignature,
            new HashSet<>(),
            null,
            fields,
            methods,
            new NoPositionInformation(),
            EnumSet.of(Modifier.PUBLIC));

    SootClass sootClass = new SootClass(javaClassSource, ClassType.Application);

    // JStaticInvokeExpr
    MethodSignature statMethodSig =
        dsm.getMethodSignature("print", "java.system.Out", "void", Arrays.asList("String"));
    IStmt staticInvokeStmt =
        new JInvokeStmt(
            new JStaticInvokeExpr(
                statMethodSig, Arrays.asList(StringConstant.getInstance("Towel"))),
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
        dsm.getMethodSignature("<init>", "java.lang.Object", "void", Arrays.asList());
    IStmt specialInvokeStmt =
        new JInvokeStmt(
            new JSpecialInvokeExpr(
                new Local("$r0", sootClass.getType()), smethodSig, Arrays.asList()),
            nop);

    // toString
    Assert.assertEquals(
        "specialinvoke $r0.<java.lang.Object: void <init>()>()", specialInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(specialInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(specialInvokeStmt.equivTo(specialInvokeStmt));

    // JInterfaceInvoke
    MethodSignature imethodSig =
        dsm.getMethodSignature("remove", "java.util.Iterator", "void", Arrays.asList());
    IStmt interfaceInvokeStmt =
        new JInvokeStmt(
            new JInterfaceInvokeExpr(
                new Local("r2", sootClass.getType()), imethodSig, Arrays.asList()),
            nop);

    // toString
    Assert.assertEquals(
        "interfaceinvoke r2.<java.util.Iterator: void remove()>()", interfaceInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(interfaceInvokeStmt.equivTo(new JNopStmt(nop)));
    Assert.assertTrue(interfaceInvokeStmt.equivTo(interfaceInvokeStmt));

    // JDynamicInvoke
    MethodSignature dmethodSig =
        dsm.getMethodSignature(
            "mylambda", SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME, "void", Arrays.asList());
    MethodSignature bootstrapMethodSig =
        dsm.getMethodSignature("run", "Runnable", "void", Arrays.asList());
    List<? extends Value> bootstrapArgs = Arrays.asList();
    List<? extends Value> methodArgs = Arrays.asList();

    IStmt dynamicInvokeStmt =
        new JInvokeStmt(
            new JDynamicInvokeExpr(bootstrapMethodSig, bootstrapArgs, dmethodSig, methodArgs), nop);

    // toString
    Assert.assertEquals(
        "dynamicinvoke \"<soot.dummy.InvokeDynamic: void mylambda()>\" <void mylambda()>() <Runnable: void run()>()",
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
