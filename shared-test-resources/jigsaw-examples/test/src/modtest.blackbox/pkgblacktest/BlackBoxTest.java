package pkgblacktest;

import org.junit.Assert;
import org.junit.Test;

import pkgfib.Fibonacci;

public class BlackBoxTest {

    @Test
    public void doBlackboxTest() {
        System.out.println(  "Running blackbox test " + BlackBoxTest.class + ".doBlackboxTest(): "
                           + "Testing modfib's exported " + Fibonacci.class);

        Assert.assertEquals  (0L,               Fibonacci.fib(0L));  
        Assert.assertEquals  (1L,               Fibonacci.fib(1L));  
        Assert.assertEquals  (1L,               Fibonacci.fib(2L));  
        Assert.assertEquals  (2L,               Fibonacci.fib(3L));  
        Assert.assertEquals  (3L,               Fibonacci.fib(4L));  
        Assert.assertEquals  (5L,               Fibonacci.fib(5L));  
        Assert.assertEquals  (8L,               Fibonacci.fib(6L));  
        Assert.assertEquals  (13L,              Fibonacci.fib(7L));  
        Assert.assertEquals  (21L,              Fibonacci.fib(8L));  
        Assert.assertEquals  (34L,              Fibonacci.fib(9L));  
        Assert.assertEquals  (55L,              Fibonacci.fib(10L)); 
        Assert.assertEquals  (89L,              Fibonacci.fib(11L)); 
        Assert.assertEquals  (144L,             Fibonacci.fib(12L)); 
        Assert.assertEquals  (233L,             Fibonacci.fib(13L)); 
        Assert.assertEquals  (377L,             Fibonacci.fib(14L)); 
        Assert.assertEquals  (610L,             Fibonacci.fib(15L)); 
        Assert.assertEquals  (987L,             Fibonacci.fib(16L)); 
        Assert.assertEquals  (1597L,            Fibonacci.fib(17L)); 
        Assert.assertEquals  (2584L,            Fibonacci.fib(18L)); 
        Assert.assertEquals  (4181L,            Fibonacci.fib(19L)); 
        Assert.assertEquals  (6765L,            Fibonacci.fib(20L)); 
        Assert.assertEquals  (10946L,           Fibonacci.fib(21L)); 
        Assert.assertEquals  (17711L,           Fibonacci.fib(22L)); 
        Assert.assertEquals  (28657L,           Fibonacci.fib(23L)); 
        Assert.assertEquals  (46368L,           Fibonacci.fib(24L)); 
        Assert.assertEquals  (75025L,           Fibonacci.fib(25L)); 
        Assert.assertEquals  (121393L,          Fibonacci.fib(26L)); 
        Assert.assertEquals  (196418L,          Fibonacci.fib(27L)); 
        Assert.assertEquals  (317811L,          Fibonacci.fib(28L)); 
        Assert.assertEquals  (514229L,          Fibonacci.fib(29L)); 
        Assert.assertEquals  (832040L,          Fibonacci.fib(30L)); 
        Assert.assertEquals  (1346269L,         Fibonacci.fib(31L)); 
        Assert.assertEquals  (2178309L,         Fibonacci.fib(32L)); 
        Assert.assertEquals  (3524578L,         Fibonacci.fib(33L)); 
        Assert.assertEquals  (5702887L,         Fibonacci.fib(34L)); 
        Assert.assertEquals  (9227465L,         Fibonacci.fib(35L)); 
        Assert.assertEquals  (14930352L,        Fibonacci.fib(36L)); 
        Assert.assertEquals  (24157817L,        Fibonacci.fib(37L)); 
        Assert.assertEquals  (39088169L,        Fibonacci.fib(38L)); 
        Assert.assertEquals  (63245986L,        Fibonacci.fib(39L)); 
        Assert.assertEquals  (102334155L,       Fibonacci.fib(40L)); 
        Assert.assertEquals  (165580141L,       Fibonacci.fib(41L)); 
        Assert.assertEquals  (267914296L,       Fibonacci.fib(42L)); 
    }
}
