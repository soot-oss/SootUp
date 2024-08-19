# Body Interceptors
BodyInterceptors are applied to each `Body` *now by default, if not overridden in the used AnalysisInputLocations*.
The BodyInterceptors exist to to improve and normalize the raw Jimple that was generated in an earlier step.
The "raw" generated Jimple from the Bytecodefrontend needs a lot improvements - deficits of raw Jimple are:

- Java Variables with that are compiled to the same Local index, but from different scopes inside the method are mapped to the same Local. The Localsplitter takes care of splitting these Locals that are semantically different, into two seperate Local instances. 
- The Conversion from a stack-machine to a register-machine creates leftover assignments - handled/inlined/removed by the Aggregator, CopyPropagator. They inline unnecessary Assignments.
- As the previous BodyTransformers could optimize code that leads to unused assignments etc - The DeadAssignmentEliminator keeps the StmtGraph clean from unused/dead Assignments.
- The Locals we get from the Java bytecode are typically untyped. Therefore we have to augment the Local types which is done by the TypeAssigner.
- t.b.c.

Optimizations (method scope)

- ConditionalBranchFolder: removes tautologic ifs that are always true/false - if we can determine it in the scope of the method.
- EmptySwitchEliminator: removes switches that are not really switching
- ConstantPropagatorAndFolder: calculates constant values before runtime
- CastAndReturnInliner: Removes merging flows to a single return
- UnreachableCodeEliminator: speaks for itself.
- TrapTightener

Standardize Jimple appearance

- LocalNameStandardizer: numbers Locals with the scheme: type-initial + number of type occurence 

!!! info "Soot Equivalent"
    [BodyTransformer](https://github.com/soot-oss/soot/blob/develop/src/main/java/soot/BodyTransformer.java)


## LocalSplitter

LocalSplitter is a`BodyInterceptor`that attempts to identify and separate uses of a local variable (as definition) that are independent of each other by renaming local variables.


Example 1: 

![LocalSplitter Example_1](assets/figures/LocalSplitter%20Example_1.png)

As shown in the example above, the local variable`l1`is defined twice. It can be split up into two new local variables: `l1#1` and `l1#2` because the both definitions are independent of each other. 



Look for foldable navigation and tabs for showing old vs new


Example 2:

![LocalSplitter Example_2](assets/figures/LocalSplitter%20Example_2.png)

In the second example, the local variable`l2`is defined thrice. But it cannot be split up into three new local variables as in the first example, because its definitions in the if-branches are not independent of each other. Therefore, it can only be split up into two local variables as shown in the figure.



## LocalPacker

LocalPacker is a`BodyInterceptor`that attempts to minimize the number of local variables which are used in body by reusing them, when it is possible. It corresponds to the inverse body transformation of LocalSplitter. Note: Every local variable's type should be assigned before running LocalPacker.

Example:

![LocalPacker Example](assets/figures/LocalPacker%20Example.png)

In the given example above, the local variables`l1`,`l3`are summarized to be one local variable`l1`, because they have the same type without interference with each other. Likewise, the local variables`l2`,`l4`and`l5`are summarized to be another local variable`l2`. Although the local variable`l0`doesn't interfere any other local variables, it cannot be summed up with other local variables because of its distinctive type.



## TrapTightener
**WIP - currently not available!**

TrapTightener is a`BodyInterceptor`that shrinks the protected area covered by each Trap in a Body. 

Example:

![TrapTightener Example](assets/figures/TrapTightener%20Example.png)

We assume in the example above that only the`Stmt`:`l2 := 2`might throw an exception caught by the`Trap`which is labeled with`label3`. In the jimple body before running the TrapTightener, the protected area covered by the Trap contains three`Stmts`:`l1 := 1; l2 := 2; l2 := 3`. But an exception could only arise at the`Stmt`:`l2 := 2`. After the implementation of TrapTightener, we will get a contractible protected area which contains only the`Stmt`that might throw an exception, namely the`Stmt`:`l2 := 2`.



## EmptySwitchEliminator

EmptySwitchEliminator is a`BodyInterceptor`that removes empty switch statements which contain only the default case.

Example: 

![EmptySwitchEliminator Example](assets/figures/EmptySwitchEliminator%20Example.png)

As shown in the example above, the switch statement in the jimple body always takes the default action. After running EmptySwitchEliminator, the switch statement is replaced with a`GotoStmt`to the default case.



## UnreachableCodeEliminator

UnreachableCodeEliminator is a`BodyInterceptor`that removes all unreachable statements.

Example: 

![UnreachableCodeEliminator Example](assets/figures/UnreachableCodeEliminator%20Example.png)

Obviously, the code segment`l2 = 2; l3 = 3;`is unreachable. It will be removed after running the UreachableCodeEliminator.



## CopyPropagator

CopyPropagator is a `BodyInterceptor` that supports copy propagation and constant propagation.

!!! abstract "CopyPropagator"

    "Definition 3[Copy Propagation]: The use of a variable y in the statement z=x+y occurring at a point
    p can be replaced by a variable w if every path from the entry node to point p contains the same
    definition y=w, for the variable y, and after the definition prior to reaching p, there is no redefinition to
    the variable y and no redefinition to the variable w."

    [Sreekala, S. K. and Vineeth Kumar Paleri. “Copy Propagation subsumes Constant Propagation.” ArXiv abs/2207.03894 (2022): n. pag.](https://arxiv.org/pdf/2207.03894)



Example for global copy propagation:

![UnreachableCodeEliminator Example](assets/figures/CopyPropagator%20Example_1.png)

Consider a code segment in the following form: 

```
a = b;
...
c = use(a); // a, b, c are local variables
```

According to the copy propagation's definition, the statement`c = use(a)`can be replaced with`c = use(b)`iff both conditions are met: 

* `a`is defined only one time on all the paths from`a = b`to`c = use(a)`.
* There are no definitions of`b`on any path from`a = b`to`c = use(a)`.

In the example for global copy propagation, the first used`l1`is replaced with`l0`, but the second used`l1`cannot be replaced with`l3`, because the second condition is not satisfied.

Example for constant propagation:

![CopyPropagator Example_1](assets/figures/CopyPropagator%20Example_2.png)

Constant propagation is similar to copy propagation. Consider a code segment in the following form:

```
a = const;
...
b = use(a); // a, b are local variables, const is a constant
```

After perfoming the constant propagation, the statement`b = use(a)`can be replaced with`b = use(const)`iff`a`is not redefined on any of the paths from`a = const`to`b = use(a)`.

Therefore, the first used`l1`in the second example can be replaced with the constant`1`, but the second used`l1`cannot be replaced with the constant`2`, because`l1`is redefined on the path from`l1 = 2`to`l4 = use(l1)`.  However, it can be replaced with local variable`l2`, because the both conditions of copy propagation are met. 

## LocalNameStandardizer

LocalNameStandardizer is a`BodyInterceptor`that assigns a generic name to each local variable. Firstly, it will sort the local variables' order alphabetically by the string representation of their type. If there are two local variables with the same type, then the LocalNameStandardizer will use the sequence of their occurrence in jimple body to determine their order.  Each assigned name consists of two parts:

* A letter to imply the local variable's type
* A digit to imply the local variable's order

The following table shows the letter corresponding to each type:

| Type of Local Variable | Letter |
| :---------------: | :---------: |
|        boolean    |      z      |
|          byte     |      b      |
|         short     |      s      |
|          int      |      i      |
|          long     |      l      |
|         float     |      f      |
|         double    |      d      |
| char | c |
| null | n |
| unknown | e |
| reference | r |


## StaticSingleAssignmentFormer

StaticSingleAssignmentFormer is a`BodyInterceptor`that transforms jimple body into SSA form, so that each local variable is assigned exactly once and defined before its first use.

Example:

![SSA Example_1](assets/figures/SSA%20Example_1.png)

![SSA Example_2](assets/figures/SSA%20Example_2.png)

In the given example, the StaticSingleAssignmentFormer assigns each`IdentityStmt`and`AssignStmt`to a new local variable . And each use uses the local variable which is most recently defined. Sometimes, it is impossible to determine the most recently defined local variable for a use in a join block. In this case, the StaticSingleAssignmentFormer will insert a`PhiStmt`in the front of the join block to merge all most recently defined local variables and assign them a new local variable.