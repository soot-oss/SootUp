## Release Announcement

We would like to announce [Soot](https://github.com/soot-oss/soot)’s successor, SootUp. 

Over more than 20 years, Soot has become one of the world’s most popular academic tool for Java and Android analysis and instrumentation. We thank all of you who have used and contributed to it over the years! It was your feedback and your contributions that helped it grow into such a versatile tool!

However, over the years, the requirements for Soot have changed a lot, and given its original architecture, it was no longer quite up to the task. Soot was originally developed for ahead-of-time code transformation, particularly optimization, which has become very uncommon in Java. Current use cases center much more around powerful program analyses and program-understanding tasks.

Today we are officially releasing SootUp, a new version of Soot with a completely overhauled architecture. With SootUp, we aim to keep the best things about Soot, yet overcome a lot of its drawbacks. We very much restructured Soot, particularly abolishing the heavy use of singletons. Soot now has a concept of views; each view corresponds to some version or variant of an analyzed program, and multiple views can be kept in memory at the same time. This sets the foundation, for instance, for differential or incremental program analyses.

SootUp is a library that can easily be included in other projects, leaving those projects in control. For those who intend to use it as a framework, with inversion of control, SootUp provides additional templates that help you and novices to get started more easily. The Jimple IR has been slightly simplified, and has been made immutable by default such that internally SootUp can make use of caching where desired. Where required, Jimple transformations are still allowed, but in a systematic manner, for instance assuring that analyses depending on the transformed code are notified about its changes.

Below is an overview of what’s new. 
 
* Library by default, framework as an option
* Modular Architecture, no more singletons
* New source code frontend
* Immutable Jimple IR
* Greatly increased testability and test coverage

SootUp is not a drop-in replacement for Soot! Due to its completely new architecture and API it is essentially an almost complete rewrite. For a while, Soot and SootUp will coexist, as many existing tools depend on Soot, yet our maintenance efforts will henceforth be focused on SootUp, not Soot, and on extending SootUp with those capabilities that people still find missing. For now, we recommend using SootUp for greenfield projects.

For more details, check out
* The SootUp home page: https://soot-oss.github.io/SootUp/, and 
* The SootUp repository: https://github.com/soot-oss/SootUp/

We are very much looking forward to your feedback and feature requests. To this end, best create appropriate issues in the repository.

This major upgrade of Soot was made possible by generous competitive funding by the DFG, within the project “Future-proofing the Soot Framework for Program Analysis
and Transformation (FutureSoot)”. It was funded in the DFG’s program on Research Software Sustainability.
