package de.upb.soot.jimple.common.stmt;

import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import referencejimple.NoPositionInformation;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.constant.StringConstant;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.namespaces.classprovider.java.JavaClassSource;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JInvokeStmtTest {

  @Test
  public void test() {

    IView view = new JavaView(null);
    DefaultSignatureFactory dsm = new DefaultSignatureFactory();

    Path dummyPath = Paths.get(URI.create("file:/C:/nonexistent.java"));
    JavaClassSource javaClassSource = new JavaClassSource(new JavaClassPathNamespace("src/main/java/de/upb/soot"), dummyPath,
        dsm.getClassSignature("de.upb.soot.instructions.stmt.IdentityStmt"));

    JavaClassSignature superClassSignature = dsm.getClassSignature("java.lang.Object");
    JavaClassSignature classSignature = dsm.getClassSignature("java.lang.LinkedHashSet");

    Set<SootField> fields = new LinkedHashSet<SootField>();
    Set<SootMethod> methods = new LinkedHashSet<>();
    // methods.add( init() );

    SootClass sootClass = new SootClass(view, ResolvingLevel.BODIES, javaClassSource, ClassType.Application,
        java.util.Optional.ofNullable(superClassSignature), new HashSet<>(), null, fields, methods,
        new NoPositionInformation(), EnumSet.of(Modifier.PUBLIC));

    // JStaticInvokeExpr
    MethodSignature imethodSig = dsm.getMethodSignature("print", "java.system.Out", "void", Arrays.asList("String"));
    IStmt staticInvokeStmt
        = new JInvokeStmt(new JStaticInvokeExpr(view, imethodSig, Arrays.asList(StringConstant.getInstance("Towel"))));

    // JSpecialInvoke
    MethodSignature smethodSig = dsm.getMethodSignature("<init>", "java.lang.Object", "void", Arrays.asList());
    IStmt specialInvokeStmt = new JInvokeStmt(new JSpecialInvokeExpr(view,
        new Local("$r0", new RefType(view, sootClass.getSignature())), smethodSig, Arrays.asList()));

    // TODO: JInterfaceInvoke
    // TODO: JDynamicInvoke (lambdas)






    // toString
    Assert.assertEquals("staticinvoke <java.system.Out: void print(String)>(\"Towel\")", staticInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(staticInvokeStmt.equivTo(new JNopStmt()));
    Assert.assertTrue(staticInvokeStmt.equivTo(staticInvokeStmt));
    // TODO:

    // toString
    Assert.assertEquals("specialinvoke $r0.<java.lang.Object: void <init>()>()", specialInvokeStmt.toString());

    // equivTo
    Assert.assertFalse(specialInvokeStmt.equivTo(new JNopStmt()));
    Assert.assertTrue(specialInvokeStmt.equivTo(specialInvokeStmt));
    // TODO:

    Assert.assertFalse(staticInvokeStmt.equivTo(specialInvokeStmt));

  }

}
