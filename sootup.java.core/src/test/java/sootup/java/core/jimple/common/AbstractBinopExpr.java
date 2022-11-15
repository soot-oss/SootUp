package sootup.java.core.jimple.common;

import categories.Java8Test;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JGeExpr;
import sootup.core.jimple.common.expr.JGtExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.types.PrimitiveType;

/** @author Bastian Haverkamp */
@Category(Java8Test.class)
public class AbstractBinopExpr {

  final JimpleComparator comparator = JimpleComparator.getInstance();

  @Test
  public void caseAbstractBinopExpr() {
    Immediate b0 = Jimple.newLocal("l0", PrimitiveType.getBoolean());
    Immediate b1 = Jimple.newLocal("l1", PrimitiveType.getBoolean());

    JEqExpr cond0_1 = Jimple.newEqExpr(b0, b1);
    JEqExpr cond0_2 = Jimple.newEqExpr(b0, b1);
    JEqExpr cond1 = Jimple.newEqExpr(b1, b0);

    JLtExpr lt0_1 = Jimple.newLtExpr(b0, b1);
    JLtExpr lt0_2 = Jimple.newLtExpr(b0, b1);
    JLtExpr lt1 = Jimple.newLtExpr(b1, b0);

    JGtExpr gt0_1 = Jimple.newGtExpr(b1, b0);
    JGtExpr gt0_2 = Jimple.newGtExpr(b1, b0);
    JGtExpr gt1 = Jimple.newGtExpr(b0, b1);

    JLeExpr le0_1 = Jimple.newLeExpr(b0, b1);
    JLeExpr let0_2 = Jimple.newLeExpr(b0, b1);
    JLeExpr le1 = Jimple.newLeExpr(b1, b0);

    JGeExpr ge0_1 = Jimple.newGeExpr(b1, b0);
    JGeExpr ge0_2 = Jimple.newGeExpr(b1, b0);
    JGeExpr ge1 = Jimple.newGeExpr(b0, b1);

    String wrongObject = "";

    // a==b <=> a==b
    Assert.assertTrue(comparator.caseAbstractBinopExpr(cond0_1, cond0_2));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(cond0_2, cond0_1));
    // a==b <=> b==a
    Assert.assertTrue(comparator.caseAbstractBinopExpr(cond0_1, cond1));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(cond1, cond0_1));

    // (b0<b1 <=> b1<b0)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(lt0_1, lt1));
    // (b1<b0 <=> b0<b1)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(lt1, lt0_1));
    // b1<b0 <=> b1<b0
    Assert.assertTrue(comparator.caseAbstractBinopExpr(lt0_1, lt0_2));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(lt0_2, lt0_1));

    // (b0>b1 <=> b1>b0)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(gt0_1, gt1));
    // (b1>b0 <=> b0>b1)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(gt1, gt0_1));
    // b1>b0 <=> b1>b0
    Assert.assertTrue(comparator.caseAbstractBinopExpr(gt0_1, gt0_2));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(gt0_2, gt0_1));

    // b1<b0 <=> b0>b1
    Assert.assertTrue(comparator.caseAbstractBinopExpr(lt0_1, gt0_1));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(gt0_1, lt0_1));

    // (b0<b1 <=> b1<b0)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(le0_1, le1));
    // (b1<b0 <=> b0<b1)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(le1, le0_1));
    // b1<b0 <=> b1<b0
    Assert.assertTrue(comparator.caseAbstractBinopExpr(le0_1, let0_2));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(let0_2, le0_1));

    // (b0>b1 <=> b1>b0)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(ge0_1, ge1));
    // (b1>b0 <=> b0>b1)
    Assert.assertFalse(comparator.caseAbstractBinopExpr(ge1, ge0_1));
    // b1>b0 <=> b1>b0
    Assert.assertTrue(comparator.caseAbstractBinopExpr(ge0_1, ge0_2));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(ge0_2, ge0_1));

    // b1<b0 <=> b0>b1
    Assert.assertTrue(comparator.caseAbstractBinopExpr(le0_1, ge0_1));
    Assert.assertTrue(comparator.caseAbstractBinopExpr(ge0_1, le0_1));

    Assert.assertFalse(comparator.caseAbstractBinopExpr(cond0_1, wrongObject));
  }
}
