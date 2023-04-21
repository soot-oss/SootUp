# Jimple
What is Jimple? Jimple is the intermediate representation [**IR**]{A data structure which represents (source) code} of Soot, and thus SootUp.
Soot's intention is to provide a simplified way to analyze JVM bytecode. JVM bytecode is stack-based, which makes it difficult for program analysis.
Java source code, on the other hand, is also not quite suitable for program analysis, due to its nested structures.
Therefore, Jimple aims to bring the best of both worlds, a non-stack-based and flat (non-nested) representation.
For this purpose Jimple was designed as a representation of JVM bytecode which is human readable.

!!! info

    To learn more about jimple, refer to the [thesis](https://courses.cs.washington.edu/courses/cse501/01wi/project/sable-thesis.pdf) by Raja Vallee-Rai.


It might help to visualize how the Jimple version of a Java code looks like. Have a look at the following example on the `HelloWorld` class.

=== "Jimple"

    ```jimple
    public class HelloWorld extends java.lang.Object
    {
      public void <init>()
      {
        HelloWorld r0;
        r0 := @this: HelloWorld;
        specialinvoke r0.<java.lang.Object: void <init>()>();
        return;
      }
    
      public static void main(java.lang.String[])
      {
        java.lang.String[] r0;
        java.io.PrintStream $r1;
            
        r0 := @parameter0: java.lang.String[];
        $r1 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $r1.<java.io.PrintStream: 
        void println(java.lang.String)>("Hello world!");
        return;
      }
    }
    ```

=== "Java"

    ```java
    public class HelloWorld {
    
      public HelloWorld() {
      
      }
    
      public static void main(String[] var0) {
        System.out.println("Hello World!");
      }
      
    }
    ```

=== "Byte Code"

    ```
    // class version 52.0 (52)
    // access flags 0x21
    public class analysis/HelloWorld {

    // compiled from: HelloWorld.java

    // access flags 0x1
    public <init>()V
      L0
        LINENUMBER 4 L0
        ALOAD 0
        INVOKESPECIAL java/lang/Object.<init> ()V
      L1
        LINENUMBER 6 L1
        RETURN
      L2
        LOCALVARIABLE this Lanalysis/HelloWorld; L0 L2 0
        MAXSTACK = 1
        MAXLOCALS = 1

    // access flags 0x9
    public static main([Ljava/lang/String;)V
      L0
        LINENUMBER 9 L0
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        LDC "Hello World!"
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L1
        LINENUMBER 10 L1
        RETURN
      L2
        LOCALVARIABLE var0 [Ljava/lang/String; L0 L2 0
        MAXSTACK = 2
        MAXLOCALS = 1
    }
    ```

## Jimple Grammar Structure
Jimple mimics the JVMs class file structure.
Therefore it is object oriented.
A Single Class (or Interface) per file.
Three-Address-Code which means there are no nested expressions.
(nested expressions can be modeled via Locals that store intermediate calculation results.)


### Class (or Interface)
A class consists of Fields and Methods.
It is referenced by its ClassType.  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }
    }
    ```

=== "Java"

    ```java
    package target.exercise1;

	public class DemoClass {}
    ```

=== "Byte Code"

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
    }
    ```


### Field
A Field is a piece of memory which can store a value that is accessible according to its visibility modifier.
It is referenced by its FieldSignature.  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        this.<target.exercise1.DemoClass: double pi> = 3.14;
        return;
      }
    }
    /*
      "this.<target.exercise1.DemoClass: double pi>" is JInstanceFieldRef
    */
    ```

=== "Java"

    ```java
    package target.exercise1;

    public class DemoClass {
      private final double pi = 3.14;
    }
    ```

=== "Byte Code"

    ```
    // class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

    // access flags 0x12
    private final D pi = 3.14

    // access flags 0x1
    public <init>()V
      L0
	    LINENUMBER 3 L0
        ALOAD 0
        INVOKESPECIAL java/lang/Object.<init> ()V
      L1
        LINENUMBER 4 L1
        ALOAD 0
        LDC 3.14
        PUTFIELD target/exercise1/DemoClass.pi : D
        RETURN
      L2
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
        MAXSTACK = 3
        MAXLOCALS = 1
    }
    ```


### Method and the Body
The interesting part is a method. A method is a "piece of code" that can be executed.
It is referenced by its MethodSignature and contains a [**StmtGraph**]{a control flow graph} that models the sequence of single instructions/statements (Stmts).  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        virtualinvoke this.<target.exercise1.DemoClass: 
        void demoMethod()>();
        return;
      }

      public void demoMethod()
      {
        java.io.PrintStream $stack1;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        $stack1 = <java.lang.System: java.io.PrintStream out>;

        virtualinvoke $stack1.<java.io.PrintStream: 
        void println(java.lang.String)>("Inside method.");
        return;
      }
    }
    /*
      "<target.exercise1.DemoClass: void demoMethod()>" 
            and "<target.exercise1.DemoClass: void <init>()>" 
            are instances of SootMethod 
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
      DemoClass(){
        demoMethod();
      }
	  public void demoMethod(){
	    System.out.println("Inside method.");
	  }
	}
    ```

=== "Byte Code"

    ```
    // class version 52.0 (52)
    // access flags 0x21
    public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

    // access flags 0x0
    <init>()V
      L0
        LINENUMBER 5 L0
        ALOAD 0
        INVOKESPECIAL java/lang/Object.<init> ()V
      L1
        LINENUMBER 6 L1
        ALOAD 0
        INVOKEVIRTUAL target/exercise1/DemoClass.demoMethod ()V
      L2
        LINENUMBER 7 L2
        RETURN
      L3
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L3 0
        MAXSTACK = 1
        MAXLOCALS = 1

    // access flags 0x1
    public demoMethod()V
      L0
        LINENUMBER 10 L0
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        LDC "Inside method."
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L1
        LINENUMBER 11 L1
        RETURN
      L2
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
        MAXSTACK = 2
        MAXLOCALS = 1
    }
    ```


### Signatures
Signatures are required for identifying or referencing things across a method, such as Classes, Interfaces, Methods or Fields. 
Locals, on the other hand, do not need signatures, since they are referenced within method boundaries.  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;

        specialinvoke this.<java.lang.Object: void <init>()>();
        this.<target.exercise1.DemoClass: double pi> = 3.14;
        return;
      }

      public void demoMethod()
      {
        java.io.PrintStream $stack3, $stack5;
        java.lang.StringBuilder $stack4, $stack6, $stack7;
        java.lang.String $stack8;
        target.exercise1.DemoClass this;

        this := @this: target.exercise1.DemoClass;
        $stack3 = <java.lang.System: java.io.PrintStream out>;

        virtualinvoke $stack3.<java.io.PrintStream: 
          void println(java.lang.String)>("pi : 3.14");
        $stack5 = <java.lang.System: java.io.PrintStream out>;
        $stack4 = new java.lang.StringBuilder;

        specialinvoke $stack4.<java.lang.StringBuilder: void <init>()>();
        $stack6 = virtualinvoke $stack4.<java.lang.StringBuilder: 
          java.lang.StringBuilder append(java.lang.String)>
            ("pi : ");
        $stack7 = virtualinvoke $stack6.<java.lang.StringBuilder: 
          java.lang.StringBuilder append(double)>(3.1415);
        $stack8 = virtualinvoke $stack7.<java.lang.StringBuilder: 
          java.lang.String toString()>();

        virtualinvoke $stack5.<java.io.PrintStream:     
          void println(java.lang.String)>($stack8);
        return;
      }
    }
    /*
      For JInstanceFieldRef "this.<target.exercise1.DemoClass: double pi>" 
        signature is <target.exercise1.DemoClass: double pi>
      Similarly, we have other signatures like 
        <java.lang.Object: void <init>()>, 
        <java.io.PrintStream: void println(java.lang.String)> 
        and so on. 
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  private final double pi = 3.14;

	  public void demoMethod(){
	    double localPi = 3.1415;
	    System.out.println("pi : " + pi);
	    System.out.println("pi : " + localPi);
	  }
	}
    ```

=== "Byte Code"

    ```
    // class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

    // compiled from: DemoClass.java

	// access flags 0x12
	private final D pi = 3.14

	// access flags 0x1
	public <init>()V
      L0
        LINENUMBER 3 L0
        ALOAD 0
        INVOKESPECIAL java/lang/Object.<init> ()V
      L1
        LINENUMBER 4 L1
        ALOAD 0
        LDC 3.14
        PUTFIELD target/exercise1/DemoClass.pi : D
        RETURN
      L2
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
        MAXSTACK = 3
        MAXLOCALS = 1

    // access flags 0x1
	public demoMethod()V
      L0
        LINENUMBER 6 L0
        LDC 3.1415
        DSTORE 1
      L1
        LINENUMBER 7 L1
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        LDC "pi : 3.14"
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L2
        LINENUMBER 8 L2
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        NEW java/lang/StringBuilder
        DUP
        INVOKESPECIAL java/lang/StringBuilder.<init> ()V
        LDC "pi : "
          INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)
            Ljava/lang/StringBuilder;
        DLOAD 1
        INVOKEVIRTUAL java/lang/StringBuilder.append (D)Ljava/lang/StringBuilder;
        INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L3
        LINENUMBER 9 L3
        RETURN
      L4
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L4 0
        LOCALVARIABLE localPi D L1 L4 1
        MAXSTACK = 4
        MAXLOCALS = 3
    }
    ```

### Trap
A Trap is a mechanism to model exceptional flow.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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
	    LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L5 0
	    LOCALVARIABLE x I L0 L5 1
	    LOCALVARIABLE y I L0 L5 2
	    MAXSTACK = 3
	    MAXLOCALS = 4
    }
    ```

### Stmt
The main piece of Jimple is a Statement (Stmt). [**Stmts**]{formerly known as Units} represent that can be executed by the JVM.


#### Branching Statements
A BranchingStmt's job is to model the flow between Stmts.


##### JGotoStmt
for unconditional flow.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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

##### JIfStmt
for conditional flow depending on boolean Expression (AbstractConditionExpr) so they have two successor Stmt's.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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

##### JSwitchStmt
for conditional flow that behaves like a switch-case. It has #numberOfCaseLabels+1 (for default) successor Stmt's. 

All other Stmts are not manipulating the flow, which means they have a single successor Stmt as long as they are not exiting the flow inside a method.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


##### JReturnStmt & JReturnVoidStmt
They end the execution/flow inside the current method and return (a value) to its caller.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


##### JThrowStmt
Ends the execution inside the current Method if the thrown exception is not caught by a Trap, which redirects the execution to an exceptionhandler.  


=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


##### JInvokeStmt
transfers the control flow to another method until the called method returns.  

=== "Jimple"

    ```jimple
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

        a = virtualinvoke this.<target.exercise1.DemoClass: 
          int increment(int)>(x);
        $stack4 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack4.<java.io.PrintStream: 
          void println(int)>(a);

        a = virtualinvoke this.<target.exercise1.DemoClass: 
          int increment(int)>(a);
        $stack6 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack6.<java.io.PrintStream: 
          void println(int)>(a);

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

=== "Byte Code"

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


##### JAssignStmt
assigns a Value from the right hand-side to the left hand-side.
Left hand-side of an assignment can be a Local referencing a variable (i.e. a Local) or a FieldRef referencing a Field.
Right hand-side of an assignment can be an expression (Expr), a Local, a FieldRef or a Constant.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


##### JIdentityStmt
is semantically like the JAssignStmt and handles assignments of IdentityRef's to make implicit assignments explicit into the StmtGraph.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


#####JEnterMonitorStmt & JExitMonitorStmt
marks synchronized blocks of code from JEnterMonitorStmt to JExitMonitorStmt.  

=== "Jimple"

    ```jimple
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

=== "Byte Code"

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


##### JRetStmt
##### JBreakpointStmt
models a Breakpoint set by a Debugger (usually not relevant for static analyses)


### Immediate
An Immediate has a [**given**]{as in constant or immutable} Type and consists of a Local ("a Variable", "Something that contains a Value") or a Constant ("Something that is a Value").


### Type
VoidType  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
      target.exercise1.DemoClass this;
      this := @this: target.exercise1.DemoClass;
      specialinvoke this.<java.lang.Object: void <init>()>();
      return;
      }

      public void voidMethod()
      {
      java.io.PrintStream $stack1;
      target.exercise1.DemoClass this;
      this := @this: target.exercise1.DemoClass;
      $stack1 = <java.lang.System: java.io.PrintStream out>;
      virtualinvoke $stack1.<java.io.PrintStream: 
          void println(java.lang.String)>("In voidMethod().");
      return;
      }
    }
    /*
      For the SootMethod - <target.exercise1.DemoClass: void voidMethod()>, 
        returnType is instance of VoidType.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public void voidMethod(){
	  System.out.println("In voidMethod().");
      }
	}
    ```

=== "Byte Code"

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
    public voidMethod()V
      L0
        LINENUMBER 5 L0
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        LDC "In voidMethod()."
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
      L1
        LINENUMBER 6 L1
        RETURN
      L2
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L2 0
        MAXSTACK = 2
        MAXLOCALS = 1
    }
    ```


#### PrimaryType
BooleanType, ByteType, CharType, ShortType, IntType, LongType, DoubleType, FloatType  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
          target.exercise1.DemoClass this;
          this := @this: target.exercise1.DemoClass;
          specialinvoke this.<java.lang.Object: void <init>()>();
          return;
      }


      public void display()
      {
          java.io.PrintStream $stack11, $stack13, $stack15, 
            $stack17, $stack19, $stack21, $stack23, $stack25;
          int $stack12, $stack14, $stack16, $stack18;
          long $stack20;
          double $stack22;
          float $stack24;
          target.exercise1.DemoClass this;
          boolean $stack26;

          this := @this: target.exercise1.DemoClass;

          $stack11 = <java.lang.System: java.io.PrintStream out>;

          goto label1;

       label1:
          $stack26 = 0;
          virtualinvoke $stack11.<java.io.PrintStream: 
            void println(boolean)>($stack26);

          $stack13 = <java.lang.System: java.io.PrintStream out>;
          $stack12 = 127 - 1;
          virtualinvoke $stack13.<java.io.PrintStream: 
            void println(int)>($stack12);

          $stack15 = <java.lang.System: java.io.PrintStream out>;
          $stack14 = 97 + 1;
          virtualinvoke $stack15.<java.io.PrintStream: 
            void println(int)>($stack14);

          $stack17 = <java.lang.System: java.io.PrintStream out>;
          $stack16 = 1123 + 1;
          virtualinvoke $stack17.<java.io.PrintStream: 
            void println(int)>($stack16);

          $stack19 = <java.lang.System: java.io.PrintStream out>;
          $stack18 = 123456 + 1;
          virtualinvoke $stack19.<java.io.PrintStream: 
            void println(int)>($stack18);

          $stack21 = <java.lang.System: java.io.PrintStream out>;
          $stack20 = 10L + 1L;
          virtualinvoke $stack21.<java.io.PrintStream: 
            void println(long)>($stack20);

          $stack23 = <java.lang.System: java.io.PrintStream out>;
          $stack22 = 10.1 + 1.0;
          virtualinvoke $stack23.<java.io.PrintStream: 
            void println(double)>($stack22);

          $stack25 = <java.lang.System: java.io.PrintStream out>;
          $stack24 = 10.1F + 1.0F;
          virtualinvoke $stack25.<java.io.PrintStream: 
            void println(float)>($stack24);

          return;
      }
    }
    /*
      The JimpleLocal $stack12, $stack14, $stack16, $stack18 are of IntType. 
      Similarly, $stack20 is of LongType, $stack22 is of DoubleType and so on.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	  public void display(){
	    boolean varBoolean = true;
	    byte varByte = 127;
	    char varChar = 'a';
	    short varShort = 1123;
	    int varInt = 123456;
	    long varLong = 10L;
	    double varDouble = 10.10;
	    float varFloat = 10.10f;

	    System.out.println(!varBoolean);
	    System.out.println(varByte-1);
	    System.out.println(varChar+1);
	    System.out.println(varShort+1);
	    System.out.println(varInt+1);
	    System.out.println(varLong+1);
	    System.out.println(varDouble+1);
	    System.out.println(varFloat+1);

      }
	}
    ```

=== "Byte Code"

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
    public display()V
      L0
		LINENUMBER 5 L0
		ICONST_1
		ISTORE 1
      L1
		LINENUMBER 6 L1
		BIPUSH 127
		ISTORE 2
      L2
		LINENUMBER 7 L2
		BIPUSH 97
		ISTORE 3
      L3
		LINENUMBER 8 L3
		SIPUSH 1123
		ISTORE 4
      L4
		LINENUMBER 9 L4
		LDC 123456
		ISTORE 5
      L5
		LINENUMBER 10 L5
		LDC 10
		LSTORE 6
      L6
		LINENUMBER 11 L6
		LDC 10.1
		DSTORE 8
      L7
		LINENUMBER 12 L7
		LDC 10.1
		FSTORE 10
      L8
		LINENUMBER 14 L8
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 1
		IFNE L9
		ICONST_1
		GOTO L10
      L9
		FRAME FULL [target/exercise1/DemoClass I I I I I J D F] 
          [java/io/PrintStream]
		ICONST_0
      L10
		FRAME FULL [target/exercise1/DemoClass I I I I I J D F] 
          [java/io/PrintStream I]
		INVOKEVIRTUAL java/io/PrintStream.println (Z)V
      L11
		LINENUMBER 15 L11
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 2
		ICONST_1
		ISUB
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L12
		LINENUMBER 16 L12
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 3
		ICONST_1
		IADD
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L13
		LINENUMBER 17 L13
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 4
		ICONST_1
		IADD
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L14
		LINENUMBER 18 L14
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 5
		ICONST_1
		IADD
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L15
		LINENUMBER 19 L15
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		LLOAD 6
		LCONST_1
		LADD
		INVOKEVIRTUAL java/io/PrintStream.println (J)V
      L16
		LINENUMBER 20 L16
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		DLOAD 8
		DCONST_1
		DADD
		INVOKEVIRTUAL java/io/PrintStream.println (D)V
      L17
		LINENUMBER 21 L17
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		FLOAD 10
		FCONST_1
		FADD
		INVOKEVIRTUAL java/io/PrintStream.println (F)V
      L18
		LINENUMBER 23 L18
		RETURN
      L19
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L19 0
		LOCALVARIABLE varBoolean Z L1 L19 1
		LOCALVARIABLE varByte B L2 L19 2
		LOCALVARIABLE varChar C L3 L19 3
		LOCALVARIABLE varShort S L4 L19 4
		LOCALVARIABLE varInt I L5 L19 5
		LOCALVARIABLE varLong J L6 L19 6
		LOCALVARIABLE varDouble D L7 L19 8
		LOCALVARIABLE varFloat F L8 L19 10
		MAXSTACK = 5
		MAXLOCALS = 11
	}
    ```


#### ReferenceType
ClassType, 
ArrayType
NullType  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
    public void <init>()
      {
      target.exercise1.DemoClass this;
      this := @this: target.exercise1.DemoClass;
      specialinvoke this.<java.lang.Object: void <init>()>();
      return;
      }

      public target.exercise1.DemoClass getObject(target.exercise1.DemoClass)
      {
      target.exercise1.DemoClass obj, this;
      this := @this: target.exercise1.DemoClass;
      obj := @parameter0: target.exercise1.DemoClass;
      return obj;
      }

      public void compute(boolean)
      {
      int[] b;
      java.io.PrintStream $stack5, $stack6;
      boolean check;
      target.exercise1.DemoClass this;
      int i;
      null_type $r0;
      java.lang.NullPointerException soot0;
      this := @this: target.exercise1.DemoClass;
      check := @parameter0: boolean;
      b = newarray (int)[5];
      i = 0;

      label1:
        if i >= 5 goto label3;
          if check == 0 goto label2;
          $r0 = (null_type) i;
          soot0 = new java.lang.NullPointerException;
          specialinvoke soot0.<java.lang.NullPointerException: 
            void <init>(java.lang.String)>
              ("This statement would have triggered an Exception: a[i#1] = $r0");
          throw soot0;

      label2:
      b[i] = i;
          i = i + 1;
          goto label1;

      label3:
          $stack5 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack5.<java.io.PrintStream: 
            void println(java.lang.Object)>(b);
          $stack6 = <java.lang.System: java.io.PrintStream out>;
          virtualinvoke $stack6.<java.io.PrintStream: 
            void println(java.lang.Object)>(null);
          return;
      }
    }
    /*
      The JimpleLocal b is of ArrayType,
        and JimpleLocal $r0 is of NullType.
    */
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {
	
	  public DemoClass getObject(DemoClass obj){
	    return obj;
	  }

	  public void compute(boolean check){
	    int a[] = null;
        int b[] = new int[5];
        for (int i = 0; i < 5; i++) {
		  if(check){
		    a[i] = i;
		  }
		    b[i] = i;
        }
        System.out.println(b);
        System.out.println(a);
	  }
	}
    ```

=== "Byte Code"

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
    public getObject(Ltarget/exercise1/DemoClass;)Ltarget/exercise1/DemoClass;
      L0
        LINENUMBER 6 L0
        ALOAD 1
        ARETURN
      L1
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L1 0
        LOCALVARIABLE obj Ltarget/exercise1/DemoClass; L0 L1 1
        MAXSTACK = 1
        MAXLOCALS = 2

    // access flags 0x1
    public compute(Z)V
      L0
        LINENUMBER 10 L0
        ACONST_NULL
        ASTORE 2
      L1
        LINENUMBER 11 L1
        ICONST_5
        NEWARRAY T_INT
        ASTORE 3
      L2
        LINENUMBER 12 L2
        ICONST_0
        ISTORE 4
      L3
        FRAME APPEND [[I [I I]
        ILOAD 4
        ICONST_5
        IF_ICMPGE L4
      L5
        LINENUMBER 13 L5
        ILOAD 1
        IFEQ L6
      L7
        LINENUMBER 14 L7
        ALOAD 2
        ILOAD 4
        ILOAD 4
        IASTORE
      L6
        LINENUMBER 16 L6
        FRAME SAME
        ALOAD 3
        ILOAD 4
        ILOAD 4
        IASTORE
      L8
        LINENUMBER 12 L8
        IINC 4 1
        GOTO L3
      L4
        LINENUMBER 18 L4
        FRAME CHOP 1
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        ALOAD 3
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/Object;)V
      L9
        LINENUMBER 19 L9
        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
        ALOAD 2
        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/Object;)V
      L10
        LINENUMBER 20 L10
        RETURN
      L11
        LOCALVARIABLE i I L3 L4 4
        LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L11 0
        LOCALVARIABLE check Z L0 L11 1
        LOCALVARIABLE a [I L1 L11 2
        LOCALVARIABLE b [I L2 L11 3
        MAXSTACK = 3
        MAXLOCALS = 5
    }
    ```


#### Local
```jimple
$i0 
```
A Local is a variable and its scope is inside its method i.e. no referencing from outside a method.
Values can be assigned to Locals via JIdentityStmt or JAssignStmt.  

=== "Jimple"

    ```jimple
    public class target.exercise1.DemoClass extends java.lang.Object
    {
      public void <init>()
      {
        target.exercise1.DemoClass this;
        this := @this: target.exercise1.DemoClass;
        specialinvoke this.<java.lang.Object: void <init>()>();
        return;
      }

      public void compute()
      {
        java.io.PrintStream $stack2, $stack3;
        target.exercise1.DemoClass this;
        int local#2;

        this := @this: target.exercise1.DemoClass;
        $stack2 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack2.<java.io.PrintStream: void println(int)>(1);

        local#2 = this.<target.exercise1.DemoClass: int global>;
        $stack3 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke $stack3.<java.io.PrintStream: void println(int)>(local#2);
        return;
      }
    }
    /*
      $stack2, this, $stack3, local#2 are all JimpleLocal.

      "this := @this: target.exercise1.DemoClass" is JIdentityStmt

      "$stack2 = <java.lang.System: java.io.PrintStream out>", 
        "local#2 = this.<target.exercise1.DemoClass: int global>", 
          "$stack3 = <java.lang.System: java.io.PrintStream out>" 
            are JAssignStmt

    */  
    ```

=== "Java"

    ```java
	package target.exercise1;

	public class DemoClass {

      private int global;

	  public void compute(){
        int local;
        local = 1;
        System.out.println(local);
        local = this.global;
        System.out.println(local);
      }
	}
    ```

=== "Byte Code"

    ```
	// class version 52.0 (52)
	// access flags 0x21
	public class target/exercise1/DemoClass {

	// compiled from: DemoClass.java

	// access flags 0x2
	private I global

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
	public compute()V
      L0
		LINENUMBER 9 L0
		ICONST_1
		ISTORE 1
      L1
		LINENUMBER 10 L1
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 1
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L2
		LINENUMBER 11 L2
		ALOAD 0
		GETFIELD target/exercise1/DemoClass.global : I
		ISTORE 1
      L3
		LINENUMBER 12 L3
		GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
		ILOAD 1
		INVOKEVIRTUAL java/io/PrintStream.println (I)V
      L4
		LINENUMBER 14 L4
		RETURN
      L5
		LOCALVARIABLE this Ltarget/exercise1/DemoClass; L0 L5 0
		LOCALVARIABLE local I L1 L5 1
		MAXSTACK = 2
		MAXLOCALS = 2
	}
    ```



#### Constant
represents a value itself. don't confuse it with a variable/Local which has a immutable (i.e. final) attribute. 

There exists a constant entity for every Type - that way all value types can have a representation.


### Expr
An expression is a language construct that returns a value. E.g. a binary operation such as addition.


### Ref
#### JArrayRef
```jimple
$arr[1]
```
referencing a position inside an array.

#### JFieldRef (JStaticFieldRef & JInstanceFieldRef)
```jimple
<SomePackage.ExampleClass: fieldname>
// or
$r1.<SomePackage.ExampleClass: fieldname>
```
referencing a Field via its FieldSignature and if necessary (i.e. with JInstanceFieldRef) the corresponding Local instance that points to the object instance.

#### IdentityRef
The IdentityRef makes those implicit special value assignments explicit.

##### JThisRef
```jimple
@this: package.fruit.Banana
```
represents the this pointer of the current class.

##### JCaughtExceptionRef
```jimple
@caughtexception
```
represents the value of the thrown exception (caught by this exceptionhandler).

##### JParameterRef
```jimple
$i0 := @parameter0
$i1 := @parameter1 
```
represents a parameter of a method, identified by its index.

