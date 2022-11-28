# SootUp library ![Java CI with Maven](https://github.com/soot-oss/SootUp/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop) ![Coverage](.github/badges/jacoco.svg)

This is the home of the **SootUp** project.
A complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

## What is SootUp
- Transforms JVM bytecode (and other inputs!) to the intermediate representation Jimple.
- ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- Inter-procedural Data-flow Analysis with the IDE/IFDS framework enabled by [Heros](https://github.com/Sable/heros))
- Applys/Enables simple transformations on retrieving a methods Body (see BodyInterceptor)
- Provides serialization of the Jimple IR.

## SootUp improvements 
#### (compared to its predecessor [Soot](https://github.com/soot-oss/soot).)
- [x] New Improved API (without Globals/Singletons)
- [x] Fully-Parallelizable Architecture
- [x] Enables lazyloading of classes (no interleaved loading of used/dependend classes anymore)
- [x] Fail early strategy - input validation while constructing/building objects
- [x] Up-to-Date (i.e. Java8!) Sourcecode Frontend
- [x] Full Java 17 Support for Bytecode
- [x] Multiple Views (Scenes)
- [ ] Full-Diff Support for Classes (across different View instances)
- [x] Immutable Jimple IR Objects and Graphs
- [ ] Incremental Updates of Program Representation

## Getting started
[Documentation](https://soot-oss.github.io/SootUp/) and usage examples are available on Github pages.

## SootUp Roadmap
See our [Todo list](https://github.com/soot-oss/SootUp/wiki/TODOs).

## Feel free to improve Soot!
### Collaboration
You want to collaborate? Please read our [coding guidelines and the contributors notice](../../wiki/contribution-to-SootUp).


### Feedback 
You are using Soot and would like to help us support it in the future?
Please support us by filling out [this little web form](http://TODO/).

Filling out the form helps us in two ways:
By letting us know how we can improve SootUp you can directly help us prioritize newly planned features.
By stating your name and affiliation you help us showcasing Soot(Up)’s large user base. Thanks!


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
