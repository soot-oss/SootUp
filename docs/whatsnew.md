# What's new in SootUp?

### Library by default

While Soot is a library and a stand-alone command-line application. SootUp, on the other hand, is designed to be a core library.
It assumes that it is embedded in a client application that owns the thread of control.
It can be extended with a command-line interface, included in other software projects as a library, or integrated into IDEs with [JimpleLSP](https://github.com/swissiety/Jimplelsp).

### Modular Architecture

SootUp has a modular architecture, which enables its clients to include only the necessary functionality to their applications.

- [core module](https://github.com/soot-oss/SootUp/tree/develop/sootup.core) contains the core building blocks such as the jimple IR, control flow graphs, and frontend interfaces. The rest of the modules build on the core module.
- [java.core module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.core) contains parts that are essential for analyzing Java code.
- [java.bytecode module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.bytecode) contains the functionality that is necessary for taking as input java bytecode.
- [java.sourcecode module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.sourcecode) contains the functionality that is necessary for taking as input java source code.
- [callgraph module](https://github.com/soot-oss/SootUp/tree/develop/sootup.callgraph) contains implementations of common call graph construction algorithms such as **CHA**, **RTA**. A reimplementation of **Spark** pointer analysis framework is in progress.
- [jimple.parser module](https://github.com/soot-oss/SootUp/tree/develop/sootup.jimple.parser) contains the functionalty that is necessary for taking as input .jimple files.
- [analysis module](https://github.com/soot-oss/SootUp/tree/develop/sootup.analysis) enables performing interprocedural dataflow analyses.

### No More Singletons

Singletons offer a single view of a single program version, which makes it impossible to analyze multiple programs or multiple versions of the same program.
SootUp does not make use of singletons such the `Scene` class in the old Soot any more. It enables analyzing multple programs simultaneously.

### New Source Code Frontend

Soot's JastAdd-based java frontend is not maintained anymore.
In SootUp, we use WALA's well-maintained source code frontend, which will not only allow Soot to analyze Java source code, but also JavaScript and Python.

### Immutable by Design

SootUp has been designed with the goal of immutability in mind.
This makes sharing objects between several entities easier, because there is no need to worry about unintended changes to other entities.

#### Withers instead of Setters

Due to the goal of immutability, many classes do not have setters anymore.
For example, a `Body` does not have a method `setStmts(List<Stmt> stmts)`.
Instead, a method called `withStmts(List<Stmt> stmts)` has been added.
This does not modify the original instance, but returns a copy of the Body but with the provided `stmts` in its instance.
This concept of so-called `with`-ers can be found all throughout SootUp.

!!! example "A simplified example"

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

[Jimple](jimple.md) is the only intermediate representation (IR) in SootUp. We modified it slightly to be able to accommodate different programming languages in the future.

