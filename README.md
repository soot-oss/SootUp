<p align="center">
<img width="350px" src="https://github.com/soot-oss/SootUp/blob/develop/docs/img/SootUpLogo.svg">
</p> 

# SootUp library ![Java CI with Maven](https://github.com/soot-oss/SootUp/workflows/Java%20Tests%20with%20Maven/badge.svg?branch=develop) [![codecov](https://codecov.io/gh/soot-oss/SootUp/branch/develop/graph/badge.svg?token=ELA7U7IAWD)](https://codecov.io/gh/soot-oss/SootUp) [![javadoc](https://javadoc.io/badge2/org.soot-oss/sootup.core/javadoc.svg)](https://javadoc.io/doc/org.soot-oss/sootup.core) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.soot-oss/sootup.core/badge.svg)](https://central.sonatype.com/artifact/org.soot-oss/sootup)

This is the home of the **SootUp** project.
A complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

## What is SootUp
- Transforms JVM bytecode (and other inputs!) to the intermediate representation Jimple.
- Provides ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- Inter-procedural Data-flow Analysis with the IDE/IFDS framework enabled by [Heros](https://github.com/Sable/heros)
- Applies simple transformations on retrieving a methods Body (see `BodyInterceptor`)
- Provides parsing and serialization of the Jimple IR.

## Getting started
[Documentation](https://soot-oss.github.io/SootUp/) and usage examples are available on [Examples](https://github.com/soot-oss/SootUp-Examples).
Check the [Javadocs](https://soot-oss.github.io/SootUp/apidocs).
Do you have questions? Feel free to start a [Discussion](https://github.com/soot-oss/SootUp/discussions).


## SootUp improvements 
#### (compared to its predecessor [Soot](https://github.com/soot-oss/soot).)
- New improved API (without Globals/Singletons)
- Fully-Parallelizable Architecture
- Enables lazy loading of classes (no interleaved loading of used/dependent classes anymore)
- Fail early strategy - input validation while constructing/building objects
- Full Java 21 Support for Bytecode
- Multiple Views (no single static Scene anymore)
- Immutable Jimple IR Objects and Graphs

## Feel free to improve SootUp!

### Contributing
For feedback and feature requests, best create appropriate [issues](../../issues).  
For questions and clarification, please use the [discussions](../../discussions).


## Publications
[The SootUp paper](https://doi.org/10.1007/978-3-031-57246-3_13) explains further details and the design decision behind SootUp.  
[Preprint](/docs/assets/SootUp-paper.pdf) is also available.

If you use SootUp in your research work, feel free to cite it as follows:

```
@InProceedings{10.1007/978-3-031-57246-3_13,
author="Karakaya, Kadiray
and Schott, Stefan
and Klauke, Jonas
and Bodden, Eric
and Schmidt, Markus
and Luo, Linghui
and He, Dongjie",
editor="Finkbeiner, Bernd
and Kov{\'a}cs, Laura",
title="SootUp: A Redesign of the Soot Static Analysis Framework",
booktitle="Tools and Algorithms for the Construction and Analysis of Systems",
year="2024",
publisher="Springer Nature Switzerland",
address="Cham",
pages="229--247",
isbn="978-3-031-57246-3"
}
```

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
