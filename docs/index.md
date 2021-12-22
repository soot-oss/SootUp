# What is FutureSoot?

The purpose of the FutureSoot project is to make [Soot](https://github.com/soot-oss/soot) future-proof. The focus of this project lies on restructuring Soot away from a framework that makes heavy use of singletons, towards a lighter library that can easily be included in other projects. FutureSoot is not a version update to Soot, it is instead a completely new project written from scratch that aims to be a leaner, more extensible equivalent of Soot.

## What is new?

Over the years, Soot has evolved into a powerful framework, which is one of the most widely used tools in the static analysis community. This evolution was guided by the needs of the community and carried out with ad-hoc improvements. As a result, Soot has become a tool that can do many things, but it is heavy and hard to maintain. Below are the features of FutureSoot, which aims to address Soot's shortcomings.

### A Core library

Soot can do many things. It is a library and a stand-alone command-line application. FutureSoot, on the other hand, is designed to be a core library. It can be extended with a command-line interface, included in other software projects as a library, or integrated into IDEs with [JimpleLSP](https://github.com/swissiety/Jimplelsp).

### Modular Architecture

FutureSoot has a modular architecture, which enables its clients to include only the necessary functionality to their applications. Below are its modules:

- **core:** contains the core building blocks such as the jimple IR, control flow graphs, and frontend interfaces. The rest of the modules build on the core module.
- **java.core:** contains parts that are essential for analyzing Java code.
- **java.bytecode:** Java bytecode frontend implementation, contains the components that are specific to analyzing java bytecode.
- **java.sourcecode:** Java source code frontend implementation, contains the components that are specific to analyzing java source code.
- **callgraph:** contains implementations of common call graph construction algorithms such as CHA, RTA, VTA, as well as a reimplementation of Spark pointer analysis framework.
- **jimple.parser:** enables reading .jimple files.
- **tests:** contains test code that depends on all of the above modules.
