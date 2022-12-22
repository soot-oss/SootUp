# What is SootUp?

The purpose of the SootUp project is to make [Soot](https://github.com/soot-oss/soot) future-proof. The focus of this project lies on restructuring Soot away from a framework that makes heavy use of singletons, towards a lighter library that can easily be included in other projects.




!!! important

    SootUp is *not a version update* to Soot, it is instead a *completely new implementation* written from scratch that aims to be a leaner, more extensible equivalent of soot.


## What is new?

Over the years, Soot has evolved into a powerful framework, which is one of the most widely used tools in the static analysis community. This evolution was guided by the needs of the community and carried out with ad-hoc improvements. As a result, Soot has become a tool that can do many things, but it is heavy and hard to maintain. Below are the features of SootUp, which aims to address Soot's shortcomings.

### Library by default

Soot can do many things. It is a library and a stand-alone command-line application. SootUp, on the other hand, is designed to be a core library. It assumes that it is embedded in a client application that owns the thread of control. It can be extended with a command-line interface, included in other software projects as a library, or integrated into IDEs with [JimpleLSP](https://github.com/swissiety/Jimplelsp).

### Modular Architecture

SootUp has a modular architecture, which enables its clients to include only the necessary functionality to their applications. Below are its modules:

??? info "core"

    [core module](https://github.com/soot-oss/SootUp/tree/develop/sootup.core) contains the core building blocks such as the jimple IR, control flow graphs, and frontend interfaces. The rest of the modules build on the core module.
    
??? info "java.core"
    
    [java.core module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.core) contains parts that are essential for analyzing Java code.

??? info "java.bytecode"

    [java.bytecode module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.bytecode) contains the functionality that is necessary for taking as input java bytecode.
    
??? info "java.sourcecode"

    [java.sourcecode module](https://github.com/soot-oss/SootUp/tree/develop/sootup.java.sourcecode) contains the functionality that is necessary for taking as input java source code.
    
??? info "callgraph"
 
    [callgraph module](https://github.com/soot-oss/SootUp/tree/develop/sootup.callgraph) contains implementations of common call graph construction algorithms such as **CHA**, **RTA**. A reimplementation of **Spark** pointer analysis framework is in progress.
  
??? info "jimple.parser"
    
    [jimple.parser module](https://github.com/soot-oss/SootUp/tree/develop/sootup.jimple.parser) contains the functionalty that is necessary for taking as input .jimple files.

??? info "analysis" 

    [analysis module](https://github.com/soot-oss/SootUp/tree/develop/sootup.analysis) enables performing interprocedural dataflow analyses.

### No More Singletons

Singletons offer a single view of a single program version, which makes it impossible to analyze multiple programs or multiple versions of the same program. SootUp does not make use of singletons such the `Scene` class in the old Soot any more. It enables analyzing multple programs simultaneously.

### New Source Code Frontend

Soot's JastAdd-based java frontend is not maintained anymore. In SootUp, we use WALA's well-maintained source code frontend, which will not only allow Soot to analyze Java source code, but also JavaScript and Python.

### Immutable by Design

SootUp has been designed with the goal of immutability in mind. This makes sharing objects between several entities easier, because there is no need to worry about unintended changes to other entities.

#### Withers instead of Setters

Due to the immutability goal, many classes do not have setters anymore. For example, a `Body` does not have a method `setStmts(List<Stmt> stmts)`. Instead, a method called `withStmts(List<Stmt> stmts)` has been added. This does not modify the original instance, but returns a copy that has different `stmts` than the original instance. This concept of so-called `with`-ers can be found all throughout SootUp. 

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

Jimple is the only intermediate representation (IR) in SootUp. We changed it slightly to be able to accommodate different programming languages in future.

### Is this a drop-in replacement for Soot?

Not really. SootUp has a completely new architecture and API, so it is not trivial to update existing projects that build on soot. We recommend using it for greenfield projects.


## Supporters
The development of SootUp is financed by generous support from the German Research Foundation (DFG),
the Heinz Nixdorf Institute (HNI).

<table border="0">
<tr>
<td><img src="https://soot-oss.github.io/soot/images/dfg_logo_englisch_blau_en.jpg" width="250" > </td>
<td><img src="https://soot-oss.github.io/soot/images/Heinz_Nixdorf_Institut_Logo_CMYK.jpg" width="250" ></td>
</tr>
</table>

[Become a sponsor!](https://github.com/sponsors/soot-oss)
