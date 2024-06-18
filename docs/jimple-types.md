# Jimple Types
represents primary types i.e. non-reference types and non-void

### PrimaryType

- `BooleanType`
- `ByteType`
- `CharType`
- `ShortType`
- `IntType`
- `LongType`
- `DoubleType`
- `FloatType`

=== "Jimple"

    ```jimple hl_lines="14-21"
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


### ReferenceType
- `(Java)ClassType` - represents the type of a Class.
- `ArrayType` - represents an array.
- `NullType` - assignable to one of the other ReferenceTypes.

=== "Jimple"

    ```jimple hl_lines="21 24 26"
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
      null_type r0;
      java.lang.NullPointerException soot0;
      this := @this: target.exercise1.DemoClass;
      check := @parameter0: boolean;
      b = newarray (int)[5];
      i = 0;

      label1:
        if i >= 5 goto label3;
          if check == 0 goto label2;
          r0 = (null_type) i;
          soot0 = new java.lang.NullPointerException;
          specialinvoke soot0.<java.lang.NullPointerException: 
            void <init>(java.lang.String)>
              ("This statement would have triggered an Exception: a[i#1] = r0");
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
      The Local b is of ArrayType,
        and Local r0 is of NullType.
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


### VoidType
Used as a possible return type of a method.

=== "Jimple"

    ```jimple hl_lines="11"
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

