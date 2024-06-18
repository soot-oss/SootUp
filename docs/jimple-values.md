# Jimple Values

### Immediate
An Immediate has a [**given**]{as in constant or immutable} Type and consists of a Local ("a Variable", "Something that contains a Value") or a Constant ("Something that is a Value").

#### Local
```jimple
i0 
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

=== "Bytecode"

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
represents an actual value itself like `42` or `"This is a String"`.
Constants are usually assigned to `Local`s or `Ref`s.
There exists a constant entity for every [Primitive Type](jimple-types.md).

### Expr
An expression is a language construct that calculates an operation and returns a value.
E.g. a binary operation `AbstracBinopExpr` such as an addition `a + b`, an `AbstractInvokeExpr` such as `virtualinvoke $stack2.<java.io.PrintStream: void println(int)>(1);` or an `UnaryExpr` such as `!valid`.
And a bunch more!

### Ref
#### JArrayRef
```jimple
$arr[1]
```
referencing a position inside an array.

#### JFieldRef
`JFieldRef`s are referencing a `SootField` via its FieldSignature

- `JStaticFieldRef` like `#!jimple <SomePackage.ExampleClass: fieldname>`
- `JInstanceFieldRef` like `#!jimple r1.<SomePackage.ExampleClass: fieldname>`
  You can see the JInstanceFieldRef has the corresponding Local instance that points to the instance of the object which is holding the field.


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
i0 := @parameter0
i1 := @parameter1 
```
represents a parameter of a method, identified by its index.

