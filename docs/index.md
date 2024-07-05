# What's SootUp?
SootUp is a complete overhaul of the good, old static analysis framework [Soot](https://github.com/soot-oss/soot).

- Transforms JVM bytecode to the intermediate representation Jimple.
- Provides ClassHierarchy generation
- CallGraph generation with different algorithms/precisions
- Inter-procedural data-flow analysis with the IDE/IFDS framework enabled by [Heros](https://github.com/Sable/heros)
- Applies simple transformations on retrieving a methods Body (see BodyInterceptor)
- Provides serialization of the Jimple IR.

!!! important

    SootUp is *not a version update* to Soot, it is a *completely new implementation* written from scratch that aims to be a leaner, modernized and developer friendly successor of Soot.
    It is not a Drop-In Replacement! The new architecture and API, renders it not trivial to update existing projects that were built on soot.
    Therefore we recommend using SootUp for greenfield projects. We hope improved type safety and streamlined mechanisms will aide you implementing and debugging your analysis tool.
    Unfortunately not every feature has been ported - If you miss something feel free to [contribute](https://github.com/soot-oss/SootUp/pulls) a feature you miss from good old Soot.


## Why SootUp?
Over the 20+ years, SootUps predecessor Soot has evolved into a powerful framework, which is one of the most widely used tools in the static analysis community. 
This evolution was guided by the needs of the community and carried out with ad-hoc improvements.
As a result, Soot has become a tool that can do a multitude of things, but it is heavy and hard to maintain and comprehend.
So there was the need to clean up the codebase e.g. improve the software architecture,
remove legacy datastructures that weren't in the Java Runtime at the time of Soots creation,
enforce validation to have a sane state,
removing the necessity of arcane knowledge, document it more and more - to make Soot future prove.
So we introduced [Design changes in SootUp](whatsnew.md), which aim to address Soot's shortcomings.
The goal is a lighter library that can easily be understood and maintained to be included in other projects.


## Supporters
The development of SootUp is financed by generous support from the German Research Foundation (DFG) and
the Heinz Nixdorf Institute (HNI).

<table>
<tr>
<td><img src="https://soot-oss.github.io/soot/images/dfg_logo_englisch_blau_en.jpg" width="250" > </td>
<td> </td>
<td><img src="https://soot-oss.github.io/soot/images/Heinz_Nixdorf_Institut_Logo_CMYK.jpg" width="250" ></td>
</tr>
</table>

[Become a sponsor!](https://github.com/sponsors/soot-oss)

