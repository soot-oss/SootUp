# Taint Analysis Tutorial: Step-by-Step Guide

This tutorial will guide you through implementing taint analysis using SootUp's IFDS framework. You'll learn how to track sensitive data flow from sources to sinks in Java programs.

## What You'll Learn

- How to set up a taint analysis framework
- How to define sources, sinks, and flow functions
- How to handle interprocedural taint propagation
- How to detect and prevent information leaks

## Prerequisites

Before starting, ensure you have:

- Java 8 or higher
- Maven or Gradle build system
- SootUp dependencies in your `pom.xml`:

```xml
<dependency>
    <groupId>org.soot-oss</groupId>
    <artifactId>sootup.analysis</artifactId>
    <version>1.3.0</version>
</dependency>
<dependency>
<groupId>ca.mcgill.sable</groupId>
<artifactId>heros</artifactId>
<version>1.2.3</version>
</dependency>
```

## Step 1: Understanding the Problem

Taint analysis tracks the flow of sensitive information through a program. It identifies:

- **Sources**: Where sensitive data originates (e.g., user input, secrets)
- **Sinks**: Where data might be leaked (e.g., network calls, logs)
- **Flow**: How data propagates through assignments and method calls

### Example Scenarios

Let's start with simple test cases to understand different taint scenarios:

<!--codeinclude-->
[Basic taint propagation](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:BasicTaint
<!--/codeinclude-->

In this example:
- **Source**: `String a = "SECRET"` (sensitive data)
- **Flow**: `a` → `b` → `c` (taint propagation)
- **Sink**: `sc.sink(c)` (potential leak)

## Step 2: Setting Up the Analysis Infrastructure

### 2.1 Create the Analysis Runner

First, create a class to handle the analysis setup:

<!--codeinclude-->
[Analysis runner setup](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:AnalysisRunner
<!--/codeinclude-->

### 2.2 Initialize SootUp View

The analysis requires a SootUp view to access the program's intermediate representation:

<!--codeinclude-->
[Execute static analysis method](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:executeStaticAnalysis
<!--/codeinclude-->

**Key components:**
- **JavaView**: Provides access to classes and methods
- **AnalysisInputLocation**: Specifies where to find bytecode
- **Entry method**: Starting point for analysis

## Step 3: Implementing the Taint Analysis Problem

### 3.1 Create the IFDS Problem Class

The core analysis logic extends `DefaultJimpleIFDSTabulationProblem`:

<!--codeinclude-->
[Taint analysis problem class](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:TaintAnalysisProblem
<!--/codeinclude-->

### 3.2 Define Initial Seeds

The analysis starts from the entry method:

```java
@Override
public Map<Stmt, Set<Object>> initialSeeds() {
    return DefaultSeeds.make(
            Collections.singleton(entryMethod.getBody().getStmtGraph().getStartingStmt()),
            zeroValue());
}
```

**Zero value**: Represents "no taint" - the bottom element of the analysis lattice.

## Step 4: Implementing Flow Functions

Flow functions define how taint propagates through different statement types. The IFDS framework uses four types:

![IFDS Flow Functions](../img/ifds-flow-diagram.png)

### 4.1 Normal Flow Function

Handles regular statements within a method:

<!--codeinclude-->
[Normal flow function](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:getNormalFlow
<!--/codeinclude-->

**Logic breakdown:**
1. **Source detection**: If `rightOp` is `"SECRET"`, generate taint for `leftOp`
2. **Taint propagation**: Transfer taint from `rightOp` to `leftOp` in assignments
3. **Identity**: Preserve existing taint for other statements

**Key flow function types:**
- `Gen<>(leftOp, zeroValue())`: Generate new taint fact
- `Transfer<>(leftOp, rightOp)`: Transfer taint between variables
- `Identity.v()`: Preserve existing facts

### 4.2 Call Flow Function

Maps arguments to parameters when entering methods:

<!--codeinclude-->
[Call flow function](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:getCallFlow
<!--/codeinclude-->

**Process:**
1. Extract method call arguments
2. Map first argument to first parameter
3. Use `Transfer` to propagate taint across the call boundary

### 4.3 Return Flow Function

Maps return values back to call sites:

<!--codeinclude-->
[Return flow function](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:getReturnFlow
<!--/codeinclude-->

**Handles two cases:**
1. **Tainted return value**: Transfer taint to assignment target
2. **Source in return**: Generate taint if returning `"SECRET"`

### 4.4 Call-to-Return Flow Function

Preserves facts that don't flow through the called method:

<!--codeinclude-->
[Call-to-return flow function](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:getCallToReturnFlow
<!--/codeinclude-->

## Step 5: Running the Analysis

### 5.1 Execute the Solver

Create and run the IFDS solver:

```java
JimpleBasedInterproceduralCFG icfg = 
    new JimpleBasedInterproceduralCFG((View) view, entryMethodSignature, false, false);
TaintAnalysisProblem problem = new TaintAnalysisProblem(icfg, entryMethod);
JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solver = 
    new JimpleIFDSSolver(problem);
solver.solve(entryMethod.getDeclaringClassType().getClassName());
```

### 5.2 Analyze Results

Check for tainted data at sink statements:

<!--codeinclude-->
[Check leak method](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:checkLeak
<!--/codeinclude-->

## Step 6: Testing Different Scenarios

### 6.1 Basic Taint Propagation

**Test case:**
<!--codeinclude-->
[Basic taint test](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:BasicTaint
<!--/codeinclude-->

**Expected result**: Leak detected - taint flows from `"SECRET"` to sink

### 6.2 Taint Sanitization

**Test case:**
<!--codeinclude-->
[Sanitized taint test](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:BasicTaintSanitized
<!--/codeinclude-->

**Expected result**: No leak - taint removed by `b = "..."` assignment

### 6.3 Interprocedural Propagation

**Test case:**
<!--codeinclude-->
[Function propagates taint](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:FunctionPropagatesTaint
<!--/codeinclude-->

**Expected result**: Leak detected - taint flows through method call

### 6.4 Return Value Sources

**Test case:**
<!--codeinclude-->
[Function returns taint](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:FunctionReturnsTaint
<!--/codeinclude-->

**Expected result**: Leak detected - taint originates from return value

## Step 7: Running All Tests

Execute the complete test suite:

<!--codeinclude-->
[Test taint analysis](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:testTaintAnalysis
<!--/codeinclude-->

You can also run it as a standalone program:

<!--codeinclude-->
[Main method](../sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java) block:main
<!--/codeinclude-->

## Step 8: Understanding the Results

### Analysis Output

For each test case, the analysis will:

1. **Build the interprocedural control flow graph**
2. **Propagate taint facts through the program**
3. **Check if tainted data reaches sink methods**
4. **Report potential information leaks**

### Interpreting Results

- **Leak detected**: Sensitive data can flow to a sink
- **No leak**: Either no sensitive data exists or it's properly sanitized
- **Analysis failed**: Usually indicates setup or configuration issues

## Step 9: Extending the Analysis

### 9.1 Custom Sources and Sinks

Modify the flow functions to recognize domain-specific sources and sinks:

```java
// Custom source detection
if (methodName.equals("getUserInput")) {
    return new Gen<>(leftOp, zeroValue());
}

// Custom sink detection  
if (methodName.equals("sendToServer")) {
    // Check for tainted arguments
}
```

### 9.2 Advanced Sanitization

Model complex sanitization patterns:

```java
if (methodName.equals("validate") || methodName.equals("sanitize")) {
    return KillAll.v(); // Remove all taint
}
```

### 9.3 Field-Sensitive Analysis

Extend to track taint through object fields:

```java
// Handle field assignments
if (leftOp instanceof FieldRef) {
    FieldRef fieldRef = (FieldRef) leftOp;
    // Track taint in object fields
}
```

## Conclusion

You've successfully implemented a taint analysis using SootUp's IFDS framework! 