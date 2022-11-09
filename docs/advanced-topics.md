# Advanced Topics

As a user of the SootUp framework, you can omit these topics which mostly explain how some of the concepts work internally.

## Body Interceptors

!!! info "Soot Equivalent"

    [BodyTransformer](https://github.com/soot-oss/soot/blob/develop/src/main/java/soot/BodyTransformer.java)


Almost in all use-cases you can simply *ignore* body interceptors. They are applied to each `Body` *by default* to create their rather normalized or leaner versions, e.g. 
by eliminating unreachable code (`UnreachableCodeEliminator`), standardizing names of locals (`LocalNameStandardizer`), or removing empty switch statements (`EmptySwitchEliminator`) etc. 

Below, we show how these body interceptors work for the users who are interested in their internal workings.

### LocalSplitter

LocalSplitter is a<code>BodyInterceptor</code>that attempts to identify and separate uses of a local variable (as definition) that are independent of each other by renaming local variables.

Example 1: 

![LocalSplitter Example_1](./figures/LocalSplitter%20Example_1.png)

As shown in the example above, the local variable<code>l1</code>is defined twice. It can be split up into two new local variables: <code>l1#1</code> and <code>l1#2</code> because the both definitions are independent of each other. 



Look for foldable navigation and tabs for showing old vs new


Example 2:

![LocalSplitter Example_2](./figures/LocalSplitter%20Example_2.png)

In the second example, the local variable<code>l2</code>is defined thrice. But it cannot be split up into three new local variables as in the first example, because its definitions in the if-branches are not independent of each other. Therefore, it can only be split up into two local variables as shown in the figure.



### LocalPacker

LocalPacker is a<code>BodyInterceptor</code>that attempts to minimize the number of local variables which are used in body by reusing them, when it is possible. It corresponds to the inverse body transformation of LocalSplitter. Note: Every local variable's type should be assigned before running LocalPacker.

Example:

![LocalPacker Example](./figures/LocalPacker%20Example.png)

In the given example above, the local variables<code>l1</code>,<code>l3</code>are summarized to be one local variable<code>l1</code>, because they have the same type without interference with each other. Likewise, the local variables<code>l2</code>,<code>l4</code>and<code>l5</code>are summarized to be another local variable<code>l2</code>. Although the local variable<code>l0</code>doesn't interfere any other local variables, it cannot be summed up with other local variables because of its distinctive type.



### TrapTightener

TrapTightener is a<code>BodyInterceptor</code>that shrinks the protected area covered by each Trap in a Body. 

Example:

![TrapTightener Example](./figures/TrapTightener%20Example.png)

We assume in the example above that only the<code>Stmt</code>:<code>l2 := 2</code>might throw an exception caught by the<code>Trap</code>which is labeled with<code>label3</code>. In the jimple body before running the TrapTightener, the protected area covered by the Trap contains three<code>Stmts</code>:<code>l1 := 1; l2 := 2; l2 := 3</code>. But an exception could only arise at the<code>Stmt</code>:<code>l2 := 2</code>. After the implementation of TrapTightener, we will get a contractible protected area which contains only the<code>Stmt</code>that might throw an exception, namely the<code>Stmt</code>:<code>l2 := 2</code>.



### EmptySwitchEliminator

EmptySwitchEliminator is a<code>BodyInterceptor</code>that removes empty switch statements which contain only the default case.

Example: 

![EmptySwitchEliminator Example](./figures/EmptySwitchEliminator%20Example.png)

As shown in the example above, the switch statement in the jimple body always takes the default action. After running EmptySwitchEliminator, the switch statement is replaced with a<code>GotoStmt</code>to the default case.



### UnreachableCodeEliminator

UnreachableCodeEliminator is a<code>BodyInterceptor</code>that removes all unreachable statements.

Example: 

![UnreachableCodeEliminator Example](./figures/UnreachableCodeEliminator%20Example.png)

Obviously, the code segment<code>l2 = 2; l3 = 3;</code>is unreachable. It will be removed after running the UreachableCodeEliminator.



### CopyPropagator

CopyPropagator is a<code>BodyInterceptor</code>that supports the global copy propagation and constant propagation. 

Example for global copy propagation:

![UnreachableCodeEliminator Example](./figures/CopyPropagator%20Example_1.png)

Consider a code segment in the following form: 

```
a = b;
...
c = use(a); // a, b, c are local variables
```

According to the copy propagation's definition, the statement<code>c = use(a)</code>can be replaced with<code>c = use(b)</code>iff both conditions are met: 

* <code>a</code>is defined only one time on all the paths from<code>a = b</code>to<code>c = use(a)</code>.
* There are no definitions of<code>b</code>on any path from<code>a = b</code>to<code>c = use(a)</code>.

In the example for global copy propagation, the first used<code>l1</code>is replaced with<code>l0</code>, but the second used<code>l1</code>cannot be replaced with<code>l3</code>, because the second condition is not satisfied.

Example for constant propagation:

![CopyPropagator Example_1](figures/CopyPropagator%20Example_2.png)

Constant propagation is similar to copy propagation. Consider a code segment in the following form:

```
a = const;
...
b = use(a); // a, b are local variables, const is a constant
```

After perfoming the constant propagation, the statement<code>b = use(a)</code>can be replaced with<code>b = use(const)</code>iff<code>a</code>is not redefined on any of the paths from<code>a = const</code>to<code>b = use(a)</code>.

Therefore, the first used<code>l1</code>in the second example can be replaced with the constant<code>1</code>, but the second used<code>l1</code>cannot be replaced with the constant<code>2</code>, because<code>l1</code>is redefined on the path from<code>l1 = 2</code>to<code>l4 = use(l1)</code>.  However, it can be replaced with local variable<code>l2</code>, because the both conditions of copy propagation are met. 

### LocalNameStandardizer

LocalNameStandardizer is a<code>BodyInterceptor</code>that assigns a generic name to each local variable. Firstly, it will sort the local variables' order alphabetically by the string representation of their type. If there are two local variables with the same type, then the LocalNameStandardizer will use the sequence of their occurrence in jimple body to determine their order.  Each assigned name consists of two parts:

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


### StaticSingleAssignmentFormer

StaticSingleAssignmentFormer is a<code>BodyInterceptor</code>that transforms jimple body into SSA form, so that each local variable is assigned exactly once and defined before its first use.

Example:

![SSA Example_1](./figures/SSA%20Example_1.png)

![SSA Example_2](./figures/SSA%20Example_2.png)

In the given example, the StaticSingleAssignmentFormer assigns each<code>IdentityStmt</code>and<code>AssignStmt</code>to a new local variable . And each use uses the local variable which is most recently defined. Sometimes, it is impossible to determine the most recently defined local variable for a use in a join block. In this case, the StaticSingleAssignmentFormer will insert a<code>PhiStmt</code>in the front of the join block to merge all most recently defined local variables and assign them a new local variable. 

## Tools

#### LocalLivenessAnalyser

LocalLivenessAnalyser is used for querying for the list of live local variables before and after a given <code>Stmt</code>.

Example:

![LocalLiveness Example](./figures/LocalLiveness%20Example.png)

The live local variables before and after each <code>Stmt</code> will be calculated after generating an instance of LocalLivenessAnalyser as shown the example above. They can be queried by using the methods <code>getLiveLocalsBeforeStmt</code> and <code>getLiveLocalsAfterStmt</code>.

#### DominanceFinder

DomianceFinder is used for querying for the immediate dominator and dominance frontiers for a given basic block.

Example:  ![DominanceFinder Example](figures/DominanceFinder%20Example.png)

After generating an instance of DominanceFinder for a <code>BlockGraph</code>, we will get the immediate dominator and dominance frontiers for each basic block. The both properties can be queried by using the methods<code>getImmediateDominator</code>and<code>getDominanceFrontiers</code>.
