# Jimple
What is Jimple? Jimple is the intermediate representation [**IR**]{A data structure which represents (source) code} of Soot.
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
            virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>("Hello world!");
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

## Jimple Grammar Structure
Jimple mimics the JVMs class file structure.
Therefore it is object oriented.
A Single Class (or Interface) per file.
Three-Address-Code which means there are no nested expressions.
(nested expressions can be modeled via Locals that store intermediate calculation results.)


### Class (or Interface)
A class consists of Fields and Methods.
It is referenced by its ClassType.


### Field
A Field is a piece of memory which can store a value that is accessible according to its visibility modifier.
It is referenced by its FieldSignature.


### Method and the Body
The interesting part is a method. A method is a "piece of code" that can be executed.
It is referenced by its MethodSignature and contains a [**StmtGraph**]{a control flow graph} that models the sequence of single instructions/statements (Stmts).


### Signatures
Everything that we can reference across a method (e.g. a Class, Interface, Method or Field) - which is basically every item//TODO:wording that is not a Local - has a Signature.


### Trap
A Trap is a mechanism to model exceptional flow.


### Stmt
The main piece of Jimple is a Statement (Stmt). [**Stmts**]{formerly known as Units} represent that can be executed by the JVM.


#### Branching Statements
A BranchingStmt's job is to model the flow between Stmts.


##### JGotoStmt
for unconditional flow

##### JIfStmt
for conditional flow depending on boolean Expression (AbstractConditionExpr) so they have two successor Stmt's

##### JSwitchStmt
for conditional flow that behaves like a switch-case. It has #numberOfCaseLabels+1 (for default) successor Stmt's. 

All other Stmts are not manipulating the flow, which means they have a single successor Stmt as long as they are not exiting the flow inside a method.


##### JReturnStmt & JReturnVoidStmt
They end the execution/flow inside the current method and return (a value) to its caller.


##### JThrowStmt
Ends the execution inside the current Method if the thrown exception is not caught by a Trap, which redirects the execution to an exceptionhandler 


##### JInvokeStmt
transfers the control flow to another method until the called method returns. 


##### JAssignStmt
assigns a Value from the right handside to the left handside.
On the left handside can occure a Local referencing a variable (i.e. a Local) or a FieldRef referencing a Field.
On the right handside can be an expression (Expr), a Local, a FieldRef or a Constant.
##### JIdentityStmt
is semantically like the JAssignStmt and handles assignments of IdentityRef's to make implicit assignments explicit into the StmtGraph. 

#####JEnterMonitorStmt & JExitMonitorStmt
marks synchronized blocks of code from JEnterMonitorStmt to JExitMonitorStmt.


##### JRetStmt
##### JBreakpointStmt
models a Breakpoint set by a Debugger (usually not relevant for static analyses)


### Immediate
An Immediate has a [**given**]{as in constant or immutable} Type and consists of a Local ("a Variable", "Sth that contains a Value") or a Constant ("Sth that is a Value").


### Type
VoidType
#### PrimaryType
BooleanType, ByteType, CharType, ShortType, IntType, LongType, DoubleType, FloatType

#### ReferenceType
ClassType, 
ArrayType
NullType

#### Local
```jimple
$i0 
```
[Whats this?](https://www.youtube.com/watch?v=_FLhO7ZnKHs&t=54s)
A Local is a variable and its scope is inside its method i.e. no referencing from outside a method.
Values can be assigned to Locals via JIdentityStmt or JAssignStmt.


#### Constant
represents a value itself. don't confuse it with a variable/Local which has a immutable (i.e. final) attribute. 

There exists a constant entity for every Type - that way all value types can have a representation.

### Expr
// TODO


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


