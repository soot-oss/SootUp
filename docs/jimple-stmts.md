# Jimple Stmt ("Statement")
[Stmts]{formerly known as Units} represent instructions of the JVM.
Jimple is a 3-address form code so there are max 3 operands used in a ("manipulating") Stmt - i.e. this does not apply to invokes as this is just operand/parameter passing.

Stmts can be roughly grouped by the amount of successors (in the `StmtGraph` of a `Body` of a `Method`).

- A `FallsThroughStmt` has always one successor - it basically represents `program counter++`.
- A `BranchingStmt` can have one, two or even n successors.
- All others (neither FallsThrough nor BranchingStmt) have no successors and therefore end the execution of the current method.

## Branching Stmts
A BranchingStmt's job is to model the jumps or conditional branching flow between Stmts.

### JGotoStmt
represents unconditional jumps to another Stmt.  

=== "Jimple"

    ```jimple hl_lines="18 22"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }
      public static void sampleMethod()
      {
        int i;
        i = 0;

        label1:
          if i >= 5 goto label3;
          if i != 3 goto label2;
          goto label3;

        label2:
          i = i + 1;
          goto label1;

        label3:
          return;
      }
    }
    /*
      Here for statements "goto label3;" and "goto label1;", 
      we have two instances of JGotoStmt : 
        "goto[?=return]" and "goto[?=(branch)]".
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public static void sampleMethod(){
	    label1:
	    for (int i = 0; i < 5; i++){
	      if(i == 3){
	        break label1;
	      }
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

    // access flags 0x9
    public static sampleMethod()V
      L0
		LINENUMBER 6 L0
		ICONST_0
		ISTORE 0
      L1
		FRAME APPEND [I]
		ILOAD 0
		ICONST_5
		IF_ICMPGE L2
      L3
		LINENUMBER 7 L3
		ILOAD 0
		ICONST_3
		IF_ICMPNE L4
      L5
		LINENUMBER 8 L5
		GOTO L2
      L4
		LINENUMBER 6 L4
		FRAME SAME
		IINC 0 1
		GOTO L1
      L2
		LINENUMBER 11 L2
		FRAME CHOP 1
		RETURN
		LOCALVARIABLE i I L1 L2 0
		MAXSTACK = 2
		MAXLOCALS = 1
	}
    ```

### JIfStmt
For conditional jumps depending on the result of the conditional expression `AbstractConditionExpr` which needs to have boolean result.
If the conditional expression is false, the next Stmt is the successor as the JIFStmt is also a `FallsthroughStmt`.
Therefore, the JIfStmt has two successor Stmt's.

=== "Jimple"

    ```jimple hl_lines="19"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
       public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public static void sampleMethod(int)
      {
        int x, $stack1;
        java.io.PrintStream $stack2, $stack3;

        x := @parameter0: int;

        $stack1 = x % 2;
        if $stack1 != 0 goto label1;

        $stack3 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack3.<java.io.PrintStream: 
          void println(java.lang.String)>("Even");
        goto label2;

        label1:
          $stack2 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack2.<java.io.PrintStream: 
            void println(java.lang.String)>("Odd");

        label2:
          return;
      }
    }
    /*
      For statement "if $stack1 != 0 goto label1;", 
      we have an instance of JIfStmt :
        "if $stack1 != 0 goto $stack2 
            = <java.lang.System:java.io.PrintStream out>".
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public static void sampleMethod(int x){
	    if(x % 2 == 0){
	      System.out.println("Even");
	    }else{
	      System.out.println("Odd");
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

    // access flags 0x9
    public static sampleMethod(I)V
      L0
		LINENUMBER 5 L0
		ILOAD 0
		ICONST_2
		IREM
		IFNE L1
      L2
		LINENUMBER 6 L2
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Even"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
		GOTO L3
      L1
		LINENUMBER 8 L1
		FRAME SAME
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Odd"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L3
		LINENUMBER 10 L3
		FRAME SAME
		RETURN
      L4
		LOCALVARIABLE x I L0 L4 0
		MAXSTACK = 2
		MAXLOCALS = 1
	}
    ```

### JSwitchStmt
for conditional flow that behaves like a switch-case. It has #numberOfCaseLabels+1 (for default) successor Stmt's. 

=== "Jimple"

    ```jimple hl_lines="20-25"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public void switchExample(int)
      {
        int x;
        java.io.PrintStream $stack2, $stack3, $stack4;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;

        lookupswitch(x)
        {
          case 1: goto label1;
          case 2: goto label2;
          default: goto label3;
        };

        label1:
          $stack3 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack3.<java.io.PrintStream: 
            void println(java.lang.String)>("Input 1");
          goto label4;

        label2:
          $stack2 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack2.<java.io.PrintStream: 
            void println(java.lang.String)>("Input 2");
          goto label4;

        label3:
          $stack4 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack4.<java.io.PrintStream: 
            void println(java.lang.String)>("Input more than 2");

        label4:
          return;
      }
    }
    /*
      Here for below statement:
        lookupswitch(x)
          {
            case 1: goto label1;
            case 2: goto label2;
            default: goto label3;
          };

      we have an instance of JLookupSwitchStmt :
        lookupswitch(x) 
          {     
            case 1: goto $stack3 
                            = <java.lang.System: java.io.PrintStream out>;     
            case 2: goto $stack2 
                            = <java.lang.System: java.io.PrintStream out>;     
            default: goto $stack4 
                            = <java.lang.System: java.io.PrintStream out>; 
          }
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
      public void switchExample(int x){
        switch (x){
          case 1:
            System.out.println("Input 1");
            break;

          case 2:
            System.out.println("Input 2");
            break;

          default:
            System.out.println("Input more than 2");
            break;

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
	public switchExample(I)V
      L0
		LINENUMBER 5 L0
		ILOAD 1
		LOOKUPSWITCH
		1: L1
		2: L2
		default: L3
      L1
		LINENUMBER 7 L1
		FRAME SAME
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Input 1"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L4
		LINENUMBER 8 L4
		GOTO L5
      L2
		LINENUMBER 11 L2
		FRAME SAME
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Input 2"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L6
		LINENUMBER 12 L6
		GOTO L5
      L3
		LINENUMBER 15 L3
		FRAME SAME
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Input more than 2"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L5
		LINENUMBER 19 L5
		FRAME SAME
		RETURN
      L7
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L7 0
		LOCALVARIABLE x I L0 L7 1
		MAXSTACK = 2
		MAXLOCALS = 2
	}
    ```

## FallsThrough Stmts
The execution of a FallsthroughStmt goes on with the following Stmt (if no exception was thrown). 

### JInvokeStmt
transfers the control flow to another method until the called method returns.  

=== "Jimple"

    ```jimple  hl_lines="7 20 22 24 26"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public void print(int)
      {
        target.exercise1.DemoClass this;
        int x, a;
        java.io.PrintStream $stack4, $stack6;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;

        a = virtualinvoke this.<target.exercise1.DemoClass: int increment(int)>(x);
        $stack4 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack4.<java.io.PrintStream: void println(int)>(a);

        a = virtualinvoke this.<target.exercise1.DemoClass: int increment(int)>(a);
        $stack6 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack6.<java.io.PrintStream: void println(int)>(a);

        return;
      }

      public int increment(int)
      {
        int x, $stack2;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;

        $stack2 = x + 1;
        return $stack2;
      }
    }
    /*
      "specialinvoke this.<java.lang.Object: void <init>()>()", 
      "virtualinvoke this.<target.exercise1.DemoClass: int increment(int)>(x)", 
      "virtualinvoke this.<target.exercise1.DemoClass: int increment(int)>(a)" 
        are JInvokeStmts.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public void print(int x){
	    int a = increment(x);
	    System.out.println(a);
	    a = increment(a);
	    System.out.println(a);
	  }
	  public int increment(int x){
	    return x + 1;
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
	public print(I)V
      L0
		LINENUMBER 5 L0
		ALOAD 0
		ILOAD 1
		INVOKEVIRTUAL target/exercise1/DemoClass.increment (I)I
		ISTORE 2
      L1
		LINENUMBER 6 L1
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 2
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L2
		LINENUMBER 7 L2
		ALOAD 0
		ILOAD 2
		INVOKEVIRTUAL target/exercise1/DemoClass.increment (I)I
		ISTORE 2
      L3
		LINENUMBER 8 L3
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 2
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L4
		LINENUMBER 9 L4
		RETURN
      L5
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L5 0
		LOCALVARIABLE x I L0 L5 1
		LOCALVARIABLE a I L1 L5 2
		MAXSTACK = 2
		MAXLOCALS = 3

	// access flags 0x1
	public increment(I)I
      L0
		LINENUMBER 11 L0
		ILOAD 1
		ICONST_1
		IADD
		IRETURN
      L1
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L1 0
		LOCALVARIABLE x I L0 L1 1
		MAXSTACK = 2
		MAXLOCALS = 2
	}
    ```


### JAssignStmt
assigns a Value from the right hand-side to the left hand-side.
Left hand-side of an assignment can be a Local referencing a variable (i.e. a Local) or a FieldRef referencing a Field.
Right hand-side of an assignment can be an expression (Expr), a Local, a FieldRef or a Constant.  

=== "Jimple"

    ```jimple  hl_lines="8 19-22"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        this.<target.exercise1.DemoClass: int counter> = 0;
        return;
      }

      public int updateCounter()
      {
        target.exercise1.DemoClass this;
        int $stack1, $stack2, $stack3;

        this := @this: target.exercise1.DemoClass;

        $stack1 = this.<target.exercise1.DemoClass: int counter>;
        $stack2 = $stack1 + 1;
        this.<target.exercise1.DemoClass: int counter> = $stack2;
        $stack3 = this.<target.exercise1.DemoClass: int counter>;

        return $stack3;
      }
    }
    /*
      "this.<target.exercise1.DemoClass: int counter> = 0", 
      "$stack1 = this.<target.exercise1.DemoClass: int counter>",
      "$stack2 = $stack1 + 1"
      "this.<target.exercise1.DemoClass: int counter> = $stack2"
      "$stack3 = this.<target.exercise1.DemoClass: int counter>"
        are JAssignStmts.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  private int counter = 0;
	  public int updateCounter(){
	    counter = counter + 1;
	    return counter;
	  }
	}
    ```

=== "Bytecode"

    ```
	// class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

	// access flags 0x2
	private I counter

	// access flags 0x1
	public <init>()V
      L0
		LINENUMBER 3 L0
		ALOAD 0
		INVOKESPECIAL java/lang/Object.<init> ()V
      L1
		LINENUMBER 4 L1
		ALOAD 0
		ICONST_0
		PUTFIELD target/exercise1/DemoClass.counter : I
		RETURN
      L2
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
		MAXSTACK = 2
		MAXLOCALS = 1

	// access flags 0x1
	public updateCounter()I
      L0
		LINENUMBER 6 L0
		ALOAD 0
		ALOAD 0
		GETFIELD target/exercise1/DemoClass.counter : I
		ICONST_1
		IADD
		PUTFIELD target/exercise1/DemoClass.counter : I
      L1
		LINENUMBER 7 L1
		ALOAD 0
		GETFIELD target/exercise1/DemoClass.counter : I
		IRETURN
      L2
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
		MAXSTACK = 3
		MAXLOCALS = 1
	}
    ```


### JIdentityStmt
is similar to the `JAssignStmt` and but handles assignments of `IdentityRef`s to make implicit assignments explicit into the `StmtGraph`.

- Assigns parameters to a `Local` via `JParameterRef` like `@parameter0: int` refering to the first argument of the method (which is of Type int in this case).
- Assigns exceptions to a `Local` via `JCaughtExceptionRef` like `@caughtexception: java.lang.NullpointerException`
- Assigns the `this` Variable to a `Local` via a `JThisRef`

=== "Jimple"

    ```jimple hl_lines="16 17"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public void DemoClass(int)
      {
        target.exercise1.DemoClass this;
        int counter;

        this := @this: target.exercise1.DemoClass;
        counter := @parameter0: int;
        this.<target.exercise1.DemoClass: int counter> = counter;
        return;
      }
    }
    /*
      "this := @this: target.exercise1.DemoClass" and 
        "counter := @parameter0: int" are JIdentityStmts
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
      private int counter;
      public void DemoClass(int counter){
        this.counter = counter;
      }
	}
    ```

=== "Bytecode"

    ```
	// class version 52.0 (52)
    // access flags 0x21
    public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

    // access flags 0x2
    private I counter

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
    public DemoClass(I)V
      L0
        LINENUMBER 6 L0
        ALOAD 0
        ILOAD 1
        PUTFIELD target/exercise1/DemoClass.counter : I
      L1
        LINENUMBER 7 L1
        RETURN
      L2
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
        LOCALVARIABLE counter I L0 L2 1
        MAXSTACK = 2
        MAXLOCALS = 2
    }
    ```


###JEnterMonitorStmt & JExitMonitorStmt
marks synchronized blocks of code from JEnterMonitorStmt to JExitMonitorStmt.  

=== "Jimple"

    ```jimple hl_lines="20 27 35"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        this.<target.exercise1.DemoClass: int counter> = 0;
        return;
      }

      public int updateCounter()
      {
        target.exercise1.DemoClass this;
        int $stack4, $stack5, $stack7;
        java.lang.Throwable $stack8;

        this := @this: target.exercise1.DemoClass;

        entermonitor this;

        label1:
          $stack4 = this.<target.exercise1.DemoClass: int counter>;
          $stack5 = $stack4 + 1;
          this.<target.exercise1.DemoClass: int counter> = $stack5;

          exitmonitor this;

        label2:
          goto label5;

        label3:
          $stack8 := @caughtexception;

          exitmonitor this;

        label4:
          throw $stack8;

        label5:
          $stack7 = this.<target.exercise1.DemoClass: int counter>;
          return $stack7;

          catch java.lang.Throwable from label1 to label2 with label3;
          catch java.lang.Throwable from label3 to label4 with label3;
      }
    }
    /*
      "entermonitor this" is JEnterMonitorStmt.
      "exitmonitor this" is JExitMonitorStmt.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  private int counter = 0;
	  public int updateCounter(){
	    synchronized (this) {
	      counter = counter + 1;
	    }
	    return counter;
	  }
	}
    ```

=== "Bytecode"

    ```
	// class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

	// access flags 0x2
	private I counter

	// access flags 0x1
	public <init>()V
      L0
		LINENUMBER 3 L0
		ALOAD 0
		INVOKESPECIAL java/lang/Object.<init> ()V
      L1
		LINENUMBER 4 L1
		ALOAD 0
		ICONST_0
		PUTFIELD target/exercise1/DemoClass.counter : I
		RETURN
      L2
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
		MAXSTACK = 2
		MAXLOCALS = 1
	
	// access flags 0x1
	public updateCounter()I
		TRYCATCHBLOCK L0 L1 L2 null
		TRYCATCHBLOCK L2 L3 L2 null
      L4
		LINENUMBER 6 L4
		ALOAD 0
		DUP
		ASTORE 1
		MONITORENTER
      L0
		LINENUMBER 7 L0
		ALOAD 0
		ALOAD 0
		GETFIELD target/exercise1/DemoClass.counter : I
		ICONST_1
		IADD
		PUTFIELD target/exercise1/DemoClass.counter : I
      L5
		LINENUMBER 8 L5
		ALOAD 1
		MONITOREXIT
      L1
		GOTO L6
      L2
		FRAME FULL [target/exercise1/DemoClass java/lang/Object] 
          [java/lang/Throwable]
		ASTORE 2
		ALOAD 1
		MONITOREXIT
      L3
		ALOAD 2
		ATHROW
      L6
		LINENUMBER 9 L6
		FRAME CHOP 1
		ALOAD 0
		GETFIELD target/exercise1/DemoClass.counter : I
		IRETURN
      L7
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L4 L7 0
		MAXSTACK = 3
		MAXLOCALS = 3
	}
    ```

### JRetStmt
// TODO: [java 1.6 spec](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.ret)

### JBreakpointStmt
models a Breakpoint set by a Debugger. Therefore, not really relevant for static analyses but useful for code generation.

## Other Stmts

### JReturnStmt & JReturnVoidStmt
They end the execution/flow inside the current method and return (a value) to its caller.

=== "Jimple"

    ```jimple hl_lines="20 32"
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public int increment(int)
      {
        int x, $stack2;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;

        $stack2 = x + 1;
        return $stack2;
      }

      public void print()
      {
        java.io.PrintStream $stack1;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        $stack1 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack1.<java.io.PrintStream: 
          void println(java.lang.String)>("Inside method print");
        return;
      }
    }
    /*
      "return $stack2" is JReturnStmt.
      "return" is JReturnVoidStmt.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public int increment(int x){
	    return x + 1;
	  }
	  public void print(){
	    System.out.println("Inside method print");
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
	public increment(I)I
      L0
		LINENUMBER 5 L0
		ILOAD 1
		ICONST_1
		IADD
		IRETURN
      L1
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L1 0
		LOCALVARIABLE x I L0 L1 1
		MAXSTACK = 2
		MAXLOCALS = 2

	// access flags 0x1
	public print()V
      L0
		LINENUMBER 8 L0
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LDC "Inside method print"
		INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L1
		LINENUMBER 9 L1
		RETURN
      L2
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
		MAXSTACK = 2
		MAXLOCALS = 1
	}
    ```


### JThrowStmt
Ends the execution inside the current Method if the thrown exception is not caught by a Trap, which redirects the execution to an exceptionhandler.


=== "Jimple"

    ```jimple hl_lines="29"
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
        int y, x, $stack6;
        java.lang.StringBuilder $stack3, $stack5, $stack7;
        java.io.PrintStream $stack4;
        java.lang.String $stack8;
        java.lang.RuntimeException $stack9;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        x := @parameter0: int;
        y := @parameter1: int;

        if y != 0 goto label1;

        $stack9 = new java.lang.RuntimeException;
        specialinvoke $stack9.<java.lang.RuntimeException: 
          void <init>(java.lang.String)>("Divide by zero error");
        throw $stack9;

        label1:
          $stack4 = <java.lang.System: java.io.PrintStream out>;
          $stack3 = new java.lang.StringBuilder;
          specialinvoke $stack3.<java.lang.StringBuilder: void <init>()>();

          $stack5 = virtualinvoke $stack3.<java.lang.StringBuilder: 
            java.lang.StringBuilder append(java.lang.String)>("Divide result : ");
          $stack6 = x / y;
          $stack7 = virtualinvoke $stack5.<java.lang.StringBuilder: 
            java.lang.StringBuilder append(int)>($stack6);
          $stack8 = virtualinvoke $stack7.<java.lang.StringBuilder: 
            java.lang.String toString()>();

          virtualinvoke $stack4.<java.io.PrintStream: 
            void println(java.lang.String)>($stack8);
          return;
      }
    }
    /*
      "throw $stack9" is JThrowStmt.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public void divideExample(int x, int y){
	    if(y == 0){
	      throw new RuntimeException("Divide by zero error");
	    }
	    System.out.println("Divide result : " + x / y);
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
      L0
		LINENUMBER 5 L0
		ILOAD 2
		IFNE L1
      L2
		LINENUMBER 6 L2
		NEW java/lang/RuntimeException
		DUP
		LDC "Divide by zero error"
		INVOKESPECIAL java/lang/RuntimeException.<init> 
          (Ljava/lang/String;)V
		ATHROW
      L1
		LINENUMBER 8 L1
		FRAME SAME
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		NEW java/lang/StringBuilder
		DUP
		INVOKESPECIAL java/lang/StringBuilder.<init> ()V
		LDC "Divide result : "
		INVOKEVIRTUAL java/lang/StringBuilder.append 
          (Ljava/lang/String;)Ljava/lang/StringBuilder;
		ILOAD 1
		ILOAD 2
		IDIV
		INVOKEVIRTUAL java/lang/StringBuilder.append 
          (I)Ljava/lang/StringBuilder;
		INVOKEVIRTUAL java/lang/StringBuilder.toString 
          ()Ljava/lang/String;
		INVOKEVIRTUAL java/io/PrintStream.println 
          (Ljava/lang/String;)V
      L3
		LINENUMBER 9 L3
		RETURN
      L4
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L4 0
		LOCALVARIABLE x I L0 L4 1
		LOCALVARIABLE y I L0 L4 2
		MAXSTACK = 4
		MAXLOCALS = 3
	}
    ```

## Good to know
A lot of the SootUp APIs return the `Stmt` Interface. To determine and handle its subtypes you can make use of instanceof.
=== "instanceOf & If-Else"
    ```java
    
        List<Stmt> stmts = ... ;
        for( Stmt stms : stmts ){
            if(stmt instanceof JAssignStmt){
                // found a JAssignStmt
                Value rhsOp = ((JAssignStmt) stmt).getRightOp();
                ...
            }else if(stmt instanceof JInvokeStmt){
                // found a JInvokeStmt
                JInvokeStmt ivkStmt = ((JInvokeStmt) stmt);
                MethodSignature rhsOp = ivkStmt.getInvokeExpr().getMethodSignature();
                    ...
            }else ...
        }
                        
    ```

But this could escalate to a huge if-else-tree - almost a forest. To mitigate such scenario you can implement a subclass of `AbstractStmtVisitor`.
Just subclass the methods to the respective Stmts you need to handle. This is visitor acts like a switch-case, implemented via two dynamic calls.
=== "StmtVisitor"
    ```java
    
        List<Stmt> stmts = ...;
        AbstractStmtVisitor visitor = new AbstractStmtVisitor<Integer>() {
            private int ifStmtsCounter = 0;
            @Override
            public void caseIfStmt(@NonNull  JIfStmt stmt) {
                ifStmtsCounter++;
                setResult(ifStmtCounter);
            }
        };
        
        for( Stmt stms : stmts ){
            stmt.accept(visitor);
        }
        
        int amountOfIfStmts = visitor.getResult();
    ```
    
    Sidenote: Of course its possible can create a subclass instead of an anonymous class.