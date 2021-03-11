package pkgfib;

import org.junit.Assert;
import org.junit.Test;

import pkgfib.internal.MathHelper;

public class WhiteBoxTest {

    @Test
    public void doWhiteboxTest() {
        System.out.println(  "Running whitebox test " + WhiteBoxTest.class + ".doWhiteboxTest(): "
                           + "Testing modfib's internal " + MathHelper.class);

        Assert.assertEquals  (0L,  MathHelper.add(0L, 0L));
        Assert.assertEquals  (1L,  MathHelper.add(0L, 1L));
        Assert.assertEquals  (2L,  MathHelper.add(1L, 1L));
        Assert.assertEquals  (42L, MathHelper.add(21L, 21L));

        Assert.assertEquals  (0L,  MathHelper.mult(0L, 0L));
        Assert.assertEquals  (0L,  MathHelper.mult(0L, 1L));
        Assert.assertEquals  (42L, MathHelper.mult(2L, 21L));
    }
}
