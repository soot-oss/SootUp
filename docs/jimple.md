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
        java.io.PrintStream r1;
            
        r0 := @parameter0: java.lang.String[];
        r1 = <java.lang.System: java.io.PrintStream out>;
        virtualinvoke r1.<java.io.PrintStream: 
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

=== "Bytecode"

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


### Signatures and ClassTypes
Signatures are used to identify Classes,Methods or Fields uniquely/globally.
Sidenote: Locals, do not have a signature, since they are referenced within method boundaries.

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

=== "Bytecode"

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

=== "Bytecode"

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


### Method and its Body
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

=== "Bytecode"

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


### Traps
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
