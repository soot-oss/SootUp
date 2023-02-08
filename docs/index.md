# What's SootUp?
SootUp is a complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

- Transforms JVM bytecode (and other inputs!) to the intermediate representation Jimple.
- Provides ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- Inter-procedural Data-flow Analysis with the IDE/IFDS framework enabled by [Heros](https://github.com/Sable/heros)
- Applies/Enables simple transformations on retrieving a methods Body (see BodyInterceptor)
- Provides serialization of the Jimple IR.

!!! important

    SootUp is *not a version update* to Soot, it is instead a *completely new implementation* written from scratch that aims to be a leaner, more extensible equivalent of soot.


## Why SootUp? 
#### [Click here to read our announcement on the first release of SootUp](announce.md)

Over the 20+ years, SootUps predecessor Soot has evolved into a powerful framework, which is one of the most widely used tools in the static analysis community. 
This evolution was guided by the needs of the community and carried out with ad-hoc improvements.
As a result, Soot has become a tool that can do a multitude of things, but it is heavy and hard to maintain and comprehend.
So there was the need to clean up the codebase e.g. improve the software architecture,
remove legacy datastructures that weren't in the Java Runtime at the time of Soots creation,
enforce validation to have a sane state,
removing the necessity of arcane knowledge, document it more and more - to make Soot future prove.
So we introduced [Design changes in SootUp](whatsnew.md), which aim to address Soot's shortcomings.
The goal is a lighter library that can easily be understood and maintained to be included in other projects.

### Is this a drop-in replacement for Soot?
Not really. SootUp has a completely new architecture and API, so it is not trivial to update existing projects that build on soot. We recommend using it for greenfield projects.


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
