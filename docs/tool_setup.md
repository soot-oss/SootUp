# From Prototype to a shareable and usable analysis Tool

## The problem

Most SootUp analyses start the same way: a hardcoded path in a `main` method, no way to
change the target without recompiling, a README that says *"edit line 3"*.
That is fine for a prototype, but turns into friction the moment someone else wants to use your work.

This proposes is CLI option parsing layer. Rather than designing one from scratch.
This page walks through `SootUpConfiguration` you can copy it directly.

The flag names deliberately mirror the `java` executable, so users who already know and can copy &amp; paste their execution options e.g. from their IDE.

```
java -cp myapp.jar:libs/* com.example.Main
```

can immediately guess how to invoke a SootUp tool built on this pattern.

---

## Dependency

=== "Maven"
    ```xml
    <dependency>
        <groupId>commons-cli</groupId>
        <artifactId>commons-cli</artifactId>
        <version>1.11.0</version>
    </dependency>
    ```

=== "Gradle"
    ```groovy
    implementation("commons-cli:commons-cli:1.11.0")
    ```

---

## Defining the flags

The options below cover the most common scenarios: classpath, module path, an executable
JAR, and a module-style entry point. Every flag that `java` accepts for specifying
*where to find code* has a direct equivalent here:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/toolsetup/SootUpConfiguration.java:options"
```

**Why mirror the `java` flags?**  
Muscle memory. Anyone who has ever run `javac`, `java`, or `jar` already knows that
`-cp` / `--classpath` / `--class-path` all mean the same thing. Inventing new names
(`--input`, `--target`, `--binary`) adds a cognitive hurdle that pays nothing back.

---

## Constructing the View

Each flag maps to exactly one SootUp API call. Classpath entries become
`JavaClassPathAnalysisInputLocation`s; module paths become
`JavaModulePathAnalysisInputLocation`s. The JRE is added automatically as
`SourceType.Library` so that `java.lang.*` and friends always resolve:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/toolsetup/SootUpConfiguration.java:classpath-handling"
```

The entry point is resolved from three sources in order of precedence: `--jar` (reads
`Main-Class` from the manifest), `--module`, or the first positional argument (the main
class name). The rest of the positional arguments are forwarded as `arguments` to
simulate passing `String[]` to `main`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/toolsetup/SootUpConfiguration.java:entrypoint"
```

---

## Using it in your tool

```java
public static void main(String[] args) throws Exception {
    SootUpConfiguration config = new SootUpConfiguration(args);

    JavaView view          = config.getView();
    MethodSignature entry  = config.getEntrypoint();
    List<String> mainArgs  = config.getArguments();   // remaining positional args

    // Everything from here is identical to any other SootUp analysis.
    view.getClasses().forEach(c -> System.out.println(c.getName()));
}
```

Compared to a hardcoded prototype, only the top three lines change. All analysis code
below them stays the same.

---


## Supported invocation patterns

| What you want | Command-line |
|---|---|
| Analyse a directory of `.class` files | `mytool --cp target/classes com.example.Main` |
| Analyse an executable JAR | `mytool --jar myapp.jar` |
| Classpath with multiple entries | `mytool --cp "app.jar:libs/*" com.example.Main` |
| Module path | `mytool -p mods -m com.example/com.example.Main` |
| All three aliases are equivalent | `--cp` = `--classpath` = `--class-path` |
