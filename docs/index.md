# What is FutureSoot?

The purpose of the FutureSoot project is to make [Soot](https://github.com/soot-oss/soot) future-proof. The focus of this project lies on restructuring Soot away from a framework that makes heavy use of singletons, towards a lighter library that can easily be included in other projects. FutureSoot is *not a version update* to Soot, it is instead a *completely new implementation* written from scratch that aims to be a leaner, more extensible equivalent of Soot.

## What is new?

Over the years, Soot has evolved into a powerful framework, which is one of the most widely used tools in the static analysis community. This evolution was guided by the needs of the community and carried out with ad-hoc improvements. As a result, Soot has become a tool that can do many things, but it is heavy and hard to maintain. Below are the features of FutureSoot, which aims to address Soot's shortcomings.

### Library by default

Soot can do many things. It is a library and a stand-alone command-line application. FutureSoot, on the other hand, is designed to be a core library. It assumes that it is embedded in a client application that owns the thread of control. It can be extended with a command-line interface, included in other software projects as a library, or integrated into IDEs with [JimpleLSP](https://github.com/swissiety/Jimplelsp).

### Modular Architecture

FutureSoot has a modular architecture, which enables its clients to include only the necessary functionality to their applications. Below are its modules:

- **core:** contains the core building blocks such as the jimple IR, control flow graphs, and frontend interfaces. The rest of the modules build on the core module.
- **java.core:** contains parts that are essential for analyzing Java code.
- **java.bytecode:** Java bytecode frontend implementation, contains the components that are specific to analyzing java bytecode.
- **java.sourcecode:** Java source code frontend implementation, contains the components that are specific to analyzing java source code.
- **callgraph:** contains implementations of common call graph construction algorithms such as CHA, RTA, VTA, as well as a reimplementation of Spark pointer analysis framework.
- **jimple.parser:** enables reading .jimple files.
- **tests:** contains test code that depends on all of the above modules.

### No More Singletons

Singletons offer a single view of a single program version, which makes it impossible to analyze multiple programs or multiple versions of the same program. FutureSoot does not make use of singletons such the `Scene` class in the old Soot any more. It enables analyzing multple programs simultaneously.

### New Source Code Frontend

Soot's JastAdd-based java frontend is not maintained anymore. In FutureSoot, we use WALA's well-maintained source code frontend, which will not only allow Soot to analyze Java source code, but also JavaScript and Python.

### Immutable by Design

FutureSoot has been designed with the goal of immutability in mind. This makes sharing objects between several entities easier, because there is no need to worry about unintended changes to other entities.

#### Withers instead of Setters

Due to the immutability goal, many classes do not have setters anymore. For example, a `Body` does not have a method `setStmts(List<Stmt> stmts)`. Instead, a method called `withStmts(List<Stmt> stmts)` has been added. This does not modify the original instance, but returns a copy that has different `stmts` than the original instance. This concept of so-called `with`-ers can be found all throughout FutureSoot. A simplified example:

```java
class Body {
  final List<Stmt> stmts;
  final List<Local> locals;

  Body(List<Stmt> stmts, List<Local> locals) {
    this.stmts = stmts;
    this.locals = locals;
  }  

  Body withStmts(List<Stmt> stmts) { return new Body(stmts, this.locals); }
  Body withLocals(List<Local> locals) { return new Body(this.stmts, locals); }
}
```

### Intermediate Representation

Jimple is the only intermediate representation (IR) in FutureSoot. We changed it slightly to be able to accommodate different programming languages in future.

### Is this a drop-in replacement for Soot?

Not really. FutureSoot has a completely new architecture and API, so it is not trivial to update existing projects that build on Soot. We recommend using it for greenfield projects.


<!---
Welcome to FutureSoot User Guide.


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
-->