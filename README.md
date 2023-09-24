<p align="center">
<img width="350px" src="https://github.com/soot-oss/SootUp/blob/develop/docs/SootUpLogo.svg">
</p> 

# SootUp library ![Java CI with Maven](https://github.com/soot-oss/SootUp/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop) [![codecov](https://codecov.io/gh/soot-oss/SootUp/branch/develop/graph/badge.svg?token=ELA7U7IAWD)](https://codecov.io/gh/soot-oss/SootUp) [![javadoc](https://javadoc.io/badge2/org.soot-oss/sootup.core/javadoc.svg)](https://javadoc.io/doc/org.soot-oss/sootup.core)

This is the home of the **SootUp** project.
A complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

## What is SootUp
- Transforms JVM bytecode (and other inputs!) to the intermediate representation Jimple.
- Provides ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- Inter-procedural Data-flow Analysis with the IDE/IFDS framework enabled by [Heros](https://github.com/Sable/heros)
- Applies/Enables simple transformations on retrieving a methods Body (see BodyInterceptor)
- Provides serialization of the Jimple IR.

## Getting started
[Documentation](https://soot-oss.github.io/SootUp/) and usage examples are available on Github pages.
Check the [Javadocs](https://soot-oss.github.io/SootUp/apidocs).
Do you have questions? Feel free to start a [Discussion](https://github.com/soot-oss/SootUp/discussions).


## SootUp improvements 
#### (compared to its predecessor [Soot](https://github.com/soot-oss/soot).)
- [x] New Improved API (without Globals/Singletons)
- [x] Fully-Parallelizable Architecture
- [x] Enables lazyloading of classes (no interleaved loading of used/dependend classes anymore)
- [x] Fail early strategy - input validation while constructing/building objects
- [x] Up-to-Date (i.e. Java8!) Sourcecode Frontend
- [x] Full Java 21 Support for Bytecode
- [x] Multiple Views (Scenes)
- [ ] Full-Diff Support for Classes (across different View instances)
- [x] Immutable Jimple IR Objects and Graphs
- [ ] Incremental Updates of Program Representation

## SootUp Roadmap
See our [Todo list](https://github.com/soot-oss/SootUp/wiki/TODOs).

## Feel free to improve Soot!

### Feedback and Feature Requests
For feedbacks and feature requests, best create appropriate [issues](../../issues).

### Collaboration
You want to collaborate? Please read our [coding guidelines and the contributors notice](../../wiki/contribution-to-SootUp).


## Supporters
The development of SootUp is financed by generous support from the German Research Foundation (DFG) and
the Heinz Nixdorf Institute (HNI).

<table border="0">
<tr>
<td><img src="https://soot-oss.github.io/soot/images/dfg_logo_englisch_blau_en.jpg" width="250" > </td>
<td><img src="https://soot-oss.github.io/soot/images/Heinz_Nixdorf_Institut_Logo_CMYK.jpg" width="250" ></td>
</tr>
</table>

[Become a sponsor!](https://github.com/sponsors/soot-oss)
