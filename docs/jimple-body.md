# Jimple Body
A SootMethod `Body` consists of the `Modifiers` and its `StmtGraph` - SootUps Control Flow Graph Structure.
The StmtGraph models the flow of [Stmts](jimple-stmts.md).

### Control Flow Graph
- unexceptional flow -> like FallsThroughStmts and BranchingStmts for if,goto etc.
- exceptional flow -> for exceptions, handled by traps.

### Stmts
Learn more about the types of [Stmts](jimple-stmts.md).

### Traps
A Trap is a mechanism to model exceptional flow.
A Trap represents the try-catch (finally) construct and therefore defines the type of the caught exception, the try-catch range (from-to) and the actual code that handles the exception (handler).
In serialized(!) Jimple Labels are used to denote from,to and handler Stmts.

=== "Jimple"

    ```jimple hl_lines="39"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public void divideExample(int, int)
      {
        int x, y, $stack4;
        java.io.PrintStream $stack5, $stack7;
        java.lang.Exception $stack6;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;
        y := @parameter1: int;

       label1:
          $stack5 = <java.lang.System: java.io.PrintStream out>;
          $stack4 = x / y;
          virtualinvoke $stack5.<java.io.PrintStream: void println(int)>($stack4);

       label2:
          goto label4;

       label3:
          $stack6 := @caughtexception;
          $stack7 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack7.<java.io.PrintStream: 
            void println(java.lang.String)>("Exception caught");

       label4:
          return;

          catch java.lang.Exception from label1 to label2 with label3;
      }
    }
    /*
      By calling getTraps() method, we can get the Traip chain.
      For the above jimple code, we have the below trap:
      Trap :
      begin  : $stack5 = <java.lang.System: java.io.PrintStream out>
      end    : goto [?= return]
      handler: $stack6 := @caughtexception
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public void divideExample(int x, int y){
	    try {
	      System.out.println(x / y);
	    }catch (Exception e){
	      System.out.println("Exception caught");
	    }
	  }
	}
    ```

=== "Bytecode"

    ```
    // class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

    // access flags 0x1
	public <init>()V
      L0
        LINENUMBER 3 L0
        ALOAD 0
        INVOKESPECIAL java/lang/Object.<init> ()V
        RETURN
	    L1
	    LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L1 0
	    MAXSTACK = 1
	    MAXLOCALS = 1

    // access flags 0x1
    public divideExample(II)V
      TRYCATCHBLOCK L0 L1 L2 java/lang/Exception
      L0
	    LINENUMBER 6 L0
	    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
	    ILOAD 1
	    ILOAD 2
	    IDIV
	    INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L1
	    LINENUMBER 9 L1
	    GOTO L3
      L2
	    LINENUMBER 7 L2
	    FRAME SAME1 java/lang/Exception
	    ASTORE 3
      L4
	    LINENUMBER 8 L4
	    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
	    LDC "Exception caught"
	    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L3
        LINENUMBER 10 L3
	    FRAME SAME
	    RETURN
      L5
	    LOCALVARIABLE e Ljava/lang/Exception; L4 L3 3
	    LOCALVARIABLE this Land Ttarget/exercise1/DemoClass; L0 L5 0
	    LOCALVARIABLE x I L0 L5 1
	    LOCALVARIABLE y I L0 L5 2
	    MAXSTACK = 3
	    MAXLOCALS = 4
    }
    ```
