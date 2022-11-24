# SootUp core library ![Java CI with Maven](https://github.com/soot-oss/SootUp/workflows/Java%20CI%20with%20Maven/badge.svg?branch=develop) ![Coverage](.github/badges/jacoco.svg)

![Logo](logo.png)

This is the home of the **SootUp** project.
A complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

## What is SootUp
- SootUp framework transforms JVM bytecode to the intermediate representation Jimple.
- ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- IDE/IFDS solving (enabled by [Heros](https://github.com/Sable/heros))
- Applys/Enables simple transformations on retrieving a methods Body (BodyInterceptor)
- Provides serialization of the Jimple IR.

## SootUp provides
#### comparing to it predecessor [Soot](https://github.com/soot-oss/soot).
- [x] New Improved API (without Globals/Singletons)
- [x] Fully-Parallelizable Architecture
- [x] Enables lazyloading of classes (no interleaved loading of used/dependend classes anymore)
- [x] Fail early strategy - input Validation constructing objects
- [x] Multiple Views (Scenes)
- [x] Up-to-Date (i.e. Java8!) Sourcecode Frontend
- [x] Full Java 17 Support for Bytecode
- [ ] Full-Diff Support for Classes (across different View instances)
- [ ] Incremental Updates of Program Representation 

## Getting started
[Documentation](https://soot-oss.github.io/SootUp/) and usage Examples are available on Github pages.

## SootUp Roadmap
- Big todo list: https://github.com/soot-oss/SootUp/wiki/TODOs
- Project Management: We use Zenhub to manage the project. Issues are created according to the big todo list.

## Contribution Guidelines 

- [Coding guidelines and contributors notice!](../../wiki/contribution-to-SootUp)



## Feel free to improve Soot!
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
