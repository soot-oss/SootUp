# `sootup.apk.frontend` — Issue TODO (risk vs. reward)

**Scope.** This covers the core APK → Dex → Jimple pipeline: `dexpler/`, `main/`, `instruction/`, `interceptors/`, `Util/` and `tag/`.

**Reference.** The comparisons are against Soot's dexpler (`soot/src/main/java/soot/dexpler`).

**Path conventions.**
- SootUp paths are relative to `src/main/java/sootup/apk/frontend/`.
- Soot paths start with `soot/`, meaning `soot/src/main/java/soot/`.
- `SI/` means `sootup.interceptors/src/main/java/sootup/interceptors/`.

**Buckets.** Items are grouped by **risk** (how bad it is to leave the issue unfixed: wrong Jimple, crashes, silent data loss) and by **reward** (value of fixing it relative to effort). Each item is tagged **Fix:** Easy, Medium or Architectural.

---

## Is android.jar needed?

**What SootUp does today: it never loads android.jar.**
- The platforms directory is used only by `AndroidVersionInfo`, to pick an API level. That level is passed to `Opcodes.forApi(...)` (`dexpler/DexFileProvider.java:129`).
- `getAndroidJarPath` (`main/AndroidVersionInfo.java:127-139`) computes a jar path and then throws it away.
- Nothing adds android.jar to the View.
- The conversion never looks up the class hierarchy. The only View call is `getIdentifierFactory()`.
- The Javadoc says otherwise and should be corrected. `ApkAnalysisInputLocation.java:55-63,73-77` and `main/AndroidVersionInfo.java:62-68` claim the directory is *"required to resolve method calls and class references"*.

**What Soot does: android.jar is mandatory.**
- `soot/Scene.java:632-703` puts android.jar on the classpath and throws if it is missing.
- The class hierarchy is consulted during conversion:
  - the dex-specific `TypeResolver` computes least-common-ancestors from it (`soot/dexpler/DexBody.java:813-998`)
  - `DalvikThrowAnalysis` uses it (`soot/dexpler/DalvikThrowAnalysis.java:329`)
  - invoke-virtual and invoke-interface are repaired with an `isInterface()` check (`soot/dexpler/instructions/MethodInvocationInstruction.java:391,410`)
  - phantom exception classes get `Throwable` as their superclass (`soot/dexpler/DexBody.java:1128-1130`)

**Answer.**
- *Decoding* dex does not need android.jar.
- *Correct Jimple* does. SootUp only gets by without it because it never splits locals or infers types: every register local stays `UnknownType` (see the pipeline table below).
- Once typing is added, android.jar must be in the View as a `SourceType.Library` input location. Today `CallGraphTest` adds it as `JavaClassPathAnalysisInputLocation(path)`, which defaults to `SourceType.Application`.
- Choosing opcodes should not depend on the platforms directory at all (🔴 item 5).

---

## Soot vs SootUp: post-translation pipeline

**SootUp today.** `DexBodyInterceptors.Default` is only `DexNumberTranformer` and `DexNullTransformer`, and **both are no-ops** (🔴 item 1). The Jimple that comes out is exactly what `DexBody.jimplify` produced.

| Soot step (in order) | Purpose | SootUp |
|---|---|---|
| `DexTrapStackFixer` | every handler starts with `@caughtexception` | partial, buggy (🔴9) |
| `DexJumpChainShortener` | collapse goto chains | missing (cosmetic) |
| `DexReturnInliner` | duplicate the `return` per incoming goto so locals split | missing |
| `SharedInitializationLocalSplitter` | separate constant defs used at different types | missing |
| `LocalSplitter(DalvikThrowAnalysis)` | one local per def/use web (Dalvik reuses registers) | **missing** — `SI/LocalSplitter` exists and can be reused |
| UCE / DAE / ULE | dead code, dead assignments, unused locals | SootUp prunes unreachable blocks itself; `SI` versions exist |
| `MoveExceptionInstruction.retype` | give the exception local its handler type | dead fields only |
| `DexNumTransformer` | int/long bit patterns → float/double constants | present but no-op, and its binop/cast logic is commented out |
| `DexReturnValuePropagator`, `CopyPropagator` | remove copies | missing / `SI` exists |
| `DexNullThrowTransformer` | `throw 0` → `new NullPointerException` | missing |
| `DexNullTransformer` | `0` → `null` where used as an object | present but no-op and buggy (🔴8) |
| `DexIfTransformer` | `if a == b` where one side is an object → null compare | missing |
| `DexNullArrayRefTransformer`, `DexNullInstanceofTransformer`, `DexNullIfTransformer` + branch folding | null array bases, `0 instanceof T`, `null == 0` | missing |
| `DexFillArrayDataTransformer` | type untyped array payloads through def-use | replaced by a weaker linear scan (🟠8) |
| `LocalSplitter` again | — | missing |
| `handleKnownDexTypes` / `handleAgreegingTypes` / `handleKnownDexArrayTypes` / `handleIncompatibleDexArrayTypes` | seed types from op-kind tags | missing (no op tags attached) |
| **Custom fast `TypeResolver`** | type all locals | **missing** — `SI/TypeAssigner` lacks the dex hooks (🟠1) |
| Post-typing fixups | float/double constants in array stores, boolean constants, `NullType` → Object | missing |
| `checkUnrealizableCasts` | reject primitive→reference casts | missing |
| `DexArrayInitReducer`, `LocalPacker` | inline array inits; pack locals (only after typing) | missing / `SI` exists |
| `FieldStaticnessCorrector` / `MethodStaticnessCorrector` | fix wrong staticness in obfuscated apps | missing |
| `TrapTightener`, `TrapMinimizer` | narrow traps to statements that can throw | missing (`SI/TrapTightener` throws `UnsupportedOperationException`) |
| Aggregators, `ConditionalBranchFolder`, cast/identity eliminators, `NopEliminator`, `DexReturnPacker` | cleanup | mostly missing / some in `SI` |

**Only SootUp does these:**
- builds bodies eagerly when a class is loaded (🔴7)
- prunes unreachable blocks before building the graph
- invents the exception types `Throwable$CatchAll` and `Type$N` (🔴2)

**Target chain once fixed:**
1. DexBody-level trap fixes
2. ReturnInliner
3. SharedInitSplitter
4. LocalSplitter
5. UCE
6. DAE / ULE
7. Num
8. ReturnValueProp
9. CopyProp
10. NullThrow
11. Null
12. If
13. NullArrayRef
14. NullInstanceof
15. NullIf (+ folding)
16. FillArrayData
17. LocalSplitter again
18. DexTypeResolver, then fixups
19. LocalPacker
20. Aggregator
21. CopyProp / DAE / ULE
22. NopEliminator

---

## 🔴 High Risk / High Reward — fix first

- [x] **1. Dex interceptors never change the body.**
  - **Where:** SootUp statements are immutable, and `with*` returns a new object. The results are thrown away in:
    - `interceptors/DexNumberTranformer.java:222,225`
    - `interceptors/DexNullTransformer.java:308,315,325,339,347,357,375-377`
    - `interceptors/AbstractNullTransformer.java:68,80`
  - **Impact:** every "0 → null" or "int bits → float" decision is lost.
  - **Bonus bug:** the `withArgs` at `DexNullTransformer.java:375-377` would replace the *whole* argument list with `[null]` if it were ever applied, and it only handles instance invokes.
  - **Fix: Easy.** Use `builder.getControlFlowGraph().replaceNode(old, new)`. Collect the replacements first, because `DexDefUseAnalysis` snapshots the statement list.
  - **Done:** Rewrites are collected and applied with `replaceNode`; `LocalSplitter` now runs first (`DexBodyInterceptors`), so a register reused as int and object is not nulled wholesale. Tests: `zeroUsedAsObjectBecomesNull`, `reusedRegisterOnlyObjectBecomesNull`, `intBitsUsedAsFloatBecomeAFloatConstant`.

- [x] **2. Made-up exception types in traps.**
  - **Where:** `main/DexBody.java:707-715`.
  - **What happens:**
    - A catch-all becomes `Ljava/lang/Throwable$CatchAll;`.
    - A repeated handler type gets `"$" + n` appended (e.g. `Ljava/io/IOException;$1`), which turns into a class literally named that.
    - Neither class exists, so hierarchy and exception analyses can't resolve them.
  - **Soot:** uses `java.lang.Throwable` (`soot/dexpler/DexBody.java:1746-1748`). SootUp's `MutableBlockControlFlowGraph` already handles overlapping traps of the same type.
  - **Fix: Easy.**
  - **Done:** Catch-all is `java.lang.Throwable`, types keep their names. Test: `catchAllAndRepeatedExceptionTypes`.

- [x] **3. `cmpl-float` and `cmpl-double` are translated as `cmp`.**
  - **Where:** `instruction/CmpInstruction.java:55,59` call `Jimple.newCmpExpr` instead of `newCmplExpr`.
  - **Impact:** the NaN result (-1 for cmpl vs +1 for cmpg) is lost.
  - **Soot:** correct (`soot/dexpler/instructions/CmpInstruction.java:72-81`).
  - **Fix: Easy.** Add a test.
  - **Done:** Test: `floatingPointComparisons`.

- [x] **4. Dex file priority is inverted.**
  - **Where:** the prioritizer returns `+1` for `classes.dex` (`dexpler/DexFileProvider.java:217-234`). Soot returns `-1`. The list is sorted in reverse, and `DexLibWrapper.initialize` does last-put-wins (`dexpler/DexLibWrapper.java:77-86`).
  - **What wins today:**
    - `classes2.dex` and later override `classes.dex`
    - `classes10` beats `classes2` (string comparison)
    - any non-`classes*` dex (e.g. an `assets/` payload) beats everything
  - **Impact:** with duplicate classes (multidex main-dex duplicates, shading, packers) SootUp analyses a version the runtime never loads.
  - **Fix: Easy.** Port Soot's comparator (`soot/dexpler/DexFileProvider.java:66-95`), which uses a numeric `classesN` compare.
  - **Done:** Soot's ordering ported. Tests: `DexFileProviderTest`.

- [x] **5. The opcode set depends on which platform folders exist.**
  - **Where:** `Opcodes.forApi(api)` in `dexpler/DexFileProvider.java:129`. The API is clamped to the newest `android-N` folder (`main/AndroidVersionInfo.java:167-180`).
  - **Impact:** with only `android-19` present (as in the tests), the dex 038/039 opcodes 0xfa–0xff are decoded as ODEX quick/volatile opcodes:
    - the instruction stream gets misaligned
    - 0xff (`const-method-handle`/`const-method-type` range) silently becomes a NOP
  - **Note:** a *missing* platforms directory avoids the clamp, so it currently decodes better than a partial one.
  - **Fix: Easy.** Pass `null` opcodes to `DexFileFactory.loadDexContainer` so dexlib2 uses each dex header's version. Keep an explicit API override only for `.odex`.
  - **Done:** `null` opcodes for dex/apk, API level only for `.odex`. Test: `opcodesFollowTheDexVersion`.

- [x] **6. Annotation values are wrong.**
  - **Where:** `dexpler/DexClassSource.java:197-208`.
  - **Problems:**
    - It stores `convertAnnotationValue(element.getValue().getValueType())`, i.e. the encoded-value *type code* (an int), so `@Foo(x="bar")` becomes `x = 23`.
    - `EncodedValue` is imported from `org.jf.dexlib2.dexbacked.raw`, so the `instanceof` in `convertAnnotationValue` never matches.
    - One `paramMap` is shared by every annotation of a class. Each usage gets the union of all elements, same-named elements overwrite each other, and `AnnotationUsage.hashCode` changes as the map grows.
    - Dalvik system annotations (`dalvik.annotation.InnerClass`, `EnclosingClass`, `EnclosingMethod`, `MemberClasses`, `Signature`, `AnnotationDefault`, `Throws`) leak out as user annotations. Soot consumes them into tags and filters them out (`soot/dexpler/DexAnnotation.java:119-129,915-926`).
  - **Fix: Easy–Medium.**
    - Build a fresh map per annotation.
    - Convert every encoded-value kind, following `AsmUtil.createAnnotationUsage` (`sootup.java.bytecode.frontend/.../AsmUtil.java:271-332`): constants, enum, class, nested annotations, and arrays as `List`. `ConstantUtil.fromObject` can't take dexlib2 value objects directly.
    - Filter out `dalvik.annotation.*`.
    - Delete the dead `DexUtil.createAnnotationUsage` (`Util/DexUtil.java:98-111`).
  - **Done:** `DexUtil.createAnnotationUsage` converts all value kinds, fresh map per annotation, SYSTEM annotations skipped. Test: `DexAnnotationTest`.

- [x] **7. Bodies are built eagerly, and one bad method breaks the whole class.**
  - **Where:** `DexClassSource.resolveMethods` → `DexMethod.makeSootMethod` → `DexBody.makeSootMethod` → `DexMethodSource.makeSootMethod`, which calls `resolveBody` immediately. That runs jimplify, builds the CFG and runs every interceptor for *every* method as soon as the class's methods are requested (`dexpler/DexMethodSource.java:76-98`).
  - **Impact:**
    - Any exception makes the whole class's method set unresolvable, e.g. the unhandled-opcode `IllegalArgumentException` at `instruction/InstructionFactory.java:312` or the malformed-dex checks in `DexBody.java:134,165-171`.
    - Memory and time scale with the whole APK.
  - **Soot:** installs a lazy `MethodSource` and turns invalid bytecode into a throwing body (`soot/dexpler/DexMethod.java:103-131`).
  - **Fix: Medium.** Make the body source lazy, and catch failures per method so one bad method leaves only that body unresolved.
  - **Done:** `DexMethodSource.resolveBody` converts on first `getBody()` and wraps failures in a per-method `ResolveException`. Test: `brokenMethodDoesNotBreakItsClass`.

- [x] **8. Logic bugs in `DexNullTransformer` (on top of item 1).**
  - **Wrong method checked:** `:119` uses `MethodModifier.isStatic(builder.getModifiers())`, the *enclosing* method, where it should check the *callee*. Soot uses `e.getMethodRef().isStatic()`. Inside static methods, `l.foo()` is never treated as an object use.
  - **Unnecessary crash:** `:190-191` throws `RuntimeException("NOT POSSIBLE StringConstant or NewExpr")`. Soot treats that case as an object use (`soot/dexpler/DexNullTransformer.java:244-246`).
  - **Tag branches dropped:** the `ObjectOpTag` branches are gone (`:72-76,171-177`). Array stores and reads of unknown type now stop the search with "not an object".
  - **Fix: Easy.**
  - **Done:** Callee check and the `NOT POSSIBLE` throw fixed. Still open: the `ObjectOpTag` branches need op tags (🟠2).

- [x] **9. Trap and handler construction.**
  - **Where:** `main/DexBody.java:680-764`.
  - **Problems:**
    - The end-of-body nop is commented out (`:696-704`). A try block that reaches the last instruction loses it (the end is exclusive), and a single-instruction try there is dropped by `beginStmt != endStmt`.
    - A handler that doesn't start with `move-exception` (d8/R8 omit it when the exception is ignored) gets no `@caughtexception`. Soot's `DexTrapStackFixer` always adds one.
    - A nop at the trap end inserts a *second* `@caughtexception` next to the move-exception (`:729-741`).
    - `insertBefore` silently does nothing at index 0 (`:674`).
    - There is no `canThrow` narrowing of the trap range. Soot does it at `soot/dexpler/DexBody.java:1702-1727`.
    - `DexBodyEdgeCaseTest.java:455-464` currently **asserts** the double `@caughtexception` output, which `JimpleTrapValidator` rejects.
  - **Fix: Medium.** Port `DexTrapStackFixer` and the trap narrowing, then fix the test.
  - **Done:** Handlers without move-exception get a `caught; goto` stub, ranges reaching the end of the code end before the last non-throwing Stmt, no double `@caughtexception`. Still open: `canThrow` narrowing (needs throw analysis, 🟠3). Tests: `trapReachingTheEndOfTheCode`, `handlerWithoutMoveException`, trap validation.

- [x] **10. `AndroidVersionInfo` fails silently.**
  - **Problems:**
    - The manifest-parse `catch (Exception e) {}` is empty (`main/AndroidVersionInfo.java:120-124`). It also swallows `NumberFormatException` for codename SDK values.
    - The throws for a missing jar or platforms directory are commented out (`:134-138,148-152,277-284`).
    - `apk.endsWith(".apk")` is `Path.endsWith`, which compares path elements and is always false (`:161`). The case-sensitive `contains(".apk")` fallback also matches directory names like `x.apk_out/`.
  - **Fix: Easy.** Log the exception or throw, and compare lowercased file names.
  - **Done:** Parse failures and non-numeric SDK values are logged, missing jar/platforms dir warn, `.apk` detection fixed; Javadoc no longer claims android.jar is loaded.

## 🟠 High Risk / Lower Reward — architectural, tackle deliberately

- [x] **1. No local splitting and no type inference.**
  - **Where:** `main/DexBody.java:529-610` creates one `UnknownType` local per Dalvik register for the whole method, plus one shared `$stack` local that receives every invoke and filled-new-array result.
  - **Impact:** nothing splits or types these locals. Reused registers (a String and an int loop counter in `v0`) collapse into one local, and parameter or `this` registers can be reassigned.
  - **What can be reused:** `SI/LocalSplitter` works on untyped locals.
  - **What can't:** `SI/TypeAssigner` can't simply be dropped in. It lacks Soot's dex overrides (`soot/dexpler/DexBody.java:813-998`):
    - int↔float and long↔double ambiguity handling
    - promoting any int constant to boolean
    - null LCA: `SI/typeresolving/BytecodeHierarchy.java:154-156` returns Top → `Object`, producing `(Object) 0` casts
    - definite and maybe constraints, weak object types
    - never removing casts that were in the original code
  - **Fix: Architectural.**
    - Introduce a `DexTypeResolver`, either by refactoring `SI` `TypeResolver` into overridable hooks or by porting Soot's.
    - Run it after splitting and the null/number passes.
    - Require android.jar in the View (see the android.jar section).
    - Check whether this can run during eager class loading (🔴7).
  - **Done:**
    - `LocalSplitter` runs; `DexSharedInitializationLocalSplitter` gives each use of a shared constant (and each repeated call argument) its own local; unused locals are removed; the null transformer resolves array element types via `findArrayType`.
    - Measured with `TypeAssigner` appended (not yet in the default chain), android.jar in the view: all locals get a type; invalid primitive<->reference casts dropped from 747 in 164 bodies to 117 in 34 (FlowSensitivity1) and from 198 in 43 to 31 in 9 (LocationLeak1).
    - Arithmetic, cast, negation and compare instructions now record their operand kind in `tag/OpTagPositionInfo` (a position info subclass, so it survives Stmt rewrites), and `DexNumberTranformer` uses it: invalid casts are down to 1 (FlowSensitivity1) and 0 (LocationLeak1).
    - The last one comes from exceptional edges of statements that cannot throw (needs throw analysis, 🟠3).
    - `TypeAssigner` is now part of `DexBodyInterceptors.Default`: every local of the sample APKs gets a type, with one invalid cast left in FlowSensitivity1 and none in LocationLeak1. Conversion costs roughly 50% more time. Types are best with the android.jar in the view (without it, unresolvable supertypes fall back to Object), but conversion does not fail without it.

- [ ] **2. Missing dex-specific passes and op-kind information.**
  - **Tags never attached:** Soot tags statements while translating: Int/Long/Float/Double op tags on binop, cast, unop and cmp; Object/Byte/Char/Short/Boolean/IntOrFloat/LongOrDouble tags on aget; ObjectOpTag on aput-object and filled-new-array. SootUp attaches none. The `tag/*OpTag` classes exist, but SootUp `Stmt`s can't carry tags. As a result, the number transformer's binop and cast branches are commented out (`interceptors/DexNumberTranformer.java:166-173`). `const/high16 v0,0x3f80; add-float …` stays `1065353216` instead of `1.0F`.
  - **Passes missing:** DexIfTransformer, DexNullThrowTransformer, DexNullArrayRefTransformer, DexNullInstanceofTransformer, DexNullIfTransformer, DexReturnInliner, SharedInitializationLocalSplitter, the post-typing constant and boolean fixups, and checkUnrealizableCasts.
  - **Ordering:** these passes (and the existing null/number ones) assume local splitting has already run. Before splitting, one piece of evidence decides every def of a register across the whole method, and the result depends on `HashSet<Stmt>` iteration order.
  - **Fix: Architectural.** Either carry a `Stmt → op-kind` side table through `DexMethodSource`, or run the tag-dependent passes inside `DexBody` on `stmtList` before the graph is built.
  - **Partly done:** Int/Long/Float/Double op tags are attached via `OpTagPositionInfo` and used by `DexNumberTranformer`. Still open: aget/aput element kind tags (ObjectOpTag etc.) and the other Soot passes listed above.

- [ ] **3. No throw analysis.**
  - **Impact:** traps aren't narrowed to statements that can throw, `SI/TrapTightener.java:57-60` throws `UnsupportedOperationException`, and TrapMinimizer can't be ported. Handlers receive flow from `const` and `move` statements that can't throw, so splitting and dataflow are less precise.
  - **Soot:** uses `DalvikThrowAnalysis` (`soot/dexpler/DalvikThrowAnalysis.java:160-341`).
  - **Fix: Architectural.** Needs a ThrowAnalysis API in SootUp.

- [x] **4. JVM-wide static singletons.**
  - **Where:**
    - `DexFileProvider.getInstance()` (`dexpler/DexFileProvider.java:40-49,72,84`): lazy, unsynchronized, with a static `dexMap` and a mutable `api_version` field
    - `DexResolver.getInstance()` (`dexpler/DexResolver.java:30-49`)
    - the static `DexUtil.androidVersionInfo` (`Util/DexUtil.java:39,155-161`), overwritten by every `ApkAnalysisInputLocation` constructor
  - **Impact:**
    - every APK's dex bytes stay in memory forever
    - re-analysing a changed file at the same path returns stale classes
    - the API level used belongs to whichever location was constructed last
    - under concurrency, one APK can be decoded with another's opcodes and cached permanently
  - **Soot:** scopes these to `G.v()` with a reset.
  - **Fix: Medium.** Make them instance state of `ApkAnalysisInputLocation`.
  - **Done:** `DexResolver` and the static `DexUtil.androidVersionInfo` are gone, `DexFileProvider` keeps no state, and each `ApkAnalysisInputLocation` owns its `DexLibWrapper`. Test: `rewrittenDexFileIsNotStale`.

- [ ] **5. dexlib2 2.5.2 only reads dex versions 035–039.**
  - **Impact:** dex entries with version 040+ inside an APK are silently skipped (`ZipDexContainer.isDex` catches `UnsupportedFile`), so their classes vanish without an error.
  - **Fix: Medium.** Upgrade to `com.android.tools.smali:smali-dexlib2` 3.x; the package rename touches every import.

- [ ] **6. Unsupported opcodes.**
  - **What SootUp can't handle:** `INVOKE_POLYMORPHIC(_RANGE)`, `INVOKE_CUSTOM(_RANGE)`, `CONST_METHOD_HANDLE`, `CONST_METHOD_TYPE`, and the ODEX quick/volatile/execute-inline families all throw at `instruction/InstructionFactory.java:312`.
  - **invoke-polymorphic — Easy.** Port `soot/dexpler/instructions/InvokePolymorphicInstruction.java:73-101`, using the signature from the proto reference.
  - **invoke-custom — Medium.** Port `soot/dexpler/instructions/InvokeCustomInstruction.java:82-217`. `Jimple.newDynamicInvokeExpr`, `JavaJimple.newMethodHandle`/`newMethodType` and `AsmMethodSource.java:1270-1299` are templates.
  - **const-method-handle / const-method-type — Easy.** Soot doesn't handle these either.
  - **ODEX — skip.** It needs de-odexing with a boot classpath; rely on 🔴7's per-method isolation instead.

- [ ] **7. Static field initial values are dropped.**
  - **Impact:** D8/R8 move static initial values (URLs, keys, flags) into dex `static_values` and delete the `<clinit>` puts, so analyses can't see these constants.
  - **Soot:** keeps them via `DexField.addConstantTag` (`soot/dexpler/DexField.java:65-104`).
  - **SootUp:** reads nothing (`dexpler/DexClassSource.java:220-241`), and `JavaSootField` has no constant-value API.
  - **Fix: Medium.** Needs a core API.

- [ ] **8. `fill-array-data`.**
  - **Where:** `instruction/FillArrayDataInstruction.java`.
  - **Problems:**
    - The linear backward scan only recognises `new-array` and moves (`:98-138`). An array that comes from a parameter, field, invoke or another branch loses its **whole payload**, with only a warning (`:71-73,135-138`).
    - Every element re-copies and reverses the instruction list (`:71,95`), which is quadratic for large static tables.
    - Nothing checks whether the register was overwritten in between.
  - **Soot:** uses untyped constants plus def-use analysis (`soot/dexpler/DexFillArrayDataTransformer.java:128-168`).
  - **Fix: Medium.**

- [ ] **9. Multidex is wired but untested.**
  - **Gap:** all three sample APKs have a single `classes.dex`, and 🔴4 shows the ordering is already wrong.
  - **Fix: Medium.** Add a multidex fixture with a duplicated class and a `classes10.dex`.

## 🟢 Low Risk / High Reward — quick wins

- [x] **Method `throws` and method annotations are always empty.**
  - **Where:** `dexpler/DexMethodSource.java:101-102` pass `Collections.emptyList()` and `emptySet()`.
  - **Soot:** reads `dalvik.annotation.Throws` (`soot/dexpler/DexMethod.java:148-173`) and handles method annotations.
  - **Fix: Easy** (after 🔴6).
  - **Done:** `DexMethodSource` reads `dalvik.annotation.Throws` and the method annotations. Test: `methodAndFieldMetadataIsConverted`.

- [ ] **Field and parameter annotations are dropped.**
  - **Where:** field annotations at `dexpler/DexClassSource.java:237` (`Collections.emptySet() // TODO`). Parameter annotations and names are never read.
  - **Soot:** `soot/dexpler/DexAnnotation.java:336-443`.
  - **Fix: Easy–Medium.**
  - **Partly done:** field annotations are converted. Parameter annotations are still dropped; `JavaSootMethod` has no model for them.

- [ ] **`JavaSootMethod.getDefaultValue()` throws a NullPointerException.**
  - **Why:** the method is wrapped in `OverridingBodySource` with a null delegate, so the `UnsupportedOperationException("TODO")` in `DexMethodSource.java:111-113` is never even reached.
  - **Soot:** redistributes `AnnotationDefault` to methods.
  - **Fix: Medium.**
  - **Partly done:** no longer throws (returns empty since the lazy body source); the `AnnotationDefault` values are still not read.

- [ ] **`resolveOuterClass()` always returns `Optional.empty()`.**
  - **Where:** `dexpler/DexClassSource.java:150-152`.
  - **Fix: Easy.** Use `EnclosingMethod` for parity with the ASM frontend, or also `EnclosingClass`/`InnerClass` for Soot parity (`soot/dexpler/DexAnnotation.java:518-671`).

- [ ] **Invoke-virtual vs invoke-interface isn't repaired.**
  - **Where:** the check is commented out in `instruction/MethodInvocationInstruction.java:105-110,127-132`. Obfuscated apps can carry the wrong invoke kind.
  - **Soot:** swaps the kind based on `isInterface()`.
  - **Fix: Medium.** Add a hierarchy-based interceptor that acts only when the class resolves.

- [ ] **Every class is `SourceType.Application`.**
  - **Where:** `ApkAnalysisInputLocation.java:133-137`.
  - **Impact:** bundled androidx, kotlin, okhttp and GMS are treated as app code, which inflates app-only analyses.
  - **Soot:** default exclusions downgrade `java.*`, `javax.*` and similar to library (`soot/Scene.java:184-212`).
  - **Fix: Medium.** Add a constructor parameter and a package-based classifier.

- [ ] **Dex entry handling.**
  - **Name collisions:** dex entries are keyed by basename (`dexpler/DexFileProvider.java:208-210`), so `assets/x/classes.dex` collides with the root `classes.dex` and one is silently dropped.
  - **Silent empty inputs:** `.jar`/`.zip` inputs return no sources at all (`:179-182`).
  - **Fix: Easy.**

- [x] **Misleading android.jar documentation and test setup.**
  - Fix the Javadoc described in the android.jar section.
  - Tests should add android.jar with `SourceType.Library`.
  - **Fix: Easy.**
  - **Done:** `AndroidVersionInfo.androidJarInputLocation()` builds it as `SourceType.Library` in one call, using the already-resolved API version; `CallGraphTest` uses it now instead of hand-building the path with the wrong SourceType.
  - Measured the effect on type precision (android.jar in the view vs. not, `TypeAssigner` in the chain): 1.3-4.4% of locals get a more specific type instead of `java.lang.Object` across the sample APKs, mostly caught-exception locals (`IllegalStateException`, `PackageManager$NameNotFoundException`, ...) and locals typed from a field/method whose own type needs the framework hierarchy to resolve (e.g. `javax.crypto.SecretKey`). Invalid casts are unaffected either way, and catch-block locals are still under-typed even with the jar in ~85-90% of cases (the throw-analysis gap, 🟠3), so this is worth doing but isn't a soundness fix on its own.

- [ ] **`CallGraphTest` seeds RTA with the whole view.**
  - **Where:** `src/test/.../CallGraphTest.java:87-88,122-124,156-158` use `view.getClasses()`, which includes android.jar, instead of only the app classes.
  - **Fix: Easy.** Recompute the expected counts.

- [x] **`resolveSuperclass()` NPE.**
  - **Where:** `dexpler/DexClassSource.java:137` calls `isEmpty()` on the `@Nullable` result of `ClassDef.getSuperclass()`. It crashes for `java.lang.Object` in framework or core-lib dex.
  - **Soot:** null-checks it (`soot/dexpler/DexClassLoader.java:82`).
  - **Fix: Easy.**
  - **Done:** null-checked (with the singleton removal).

- [ ] **Every `DexClassSource` from one APK compares equal.**
  - **Why:** `JavaSootClassSource.equals/hashCode` use only (input location, source path), and all classes share the APK path.
  - **Fix: Easy.** Override to include the class signature.

- [ ] **Abstract and native methods run interceptors on empty graphs.**
  - **Where:** `dexpler/DexMethod.java:57-77`.
  - **Fix: Easy.** Skip them.

- [ ] **`DexBacked*` casts crash on immutable dexlib2 instructions.**
  - **Where:** `instruction/FilledNewArrayInstruction.java:45` casts to `DexBackedInstruction35c`, and `instruction/SwitchInstruction.java:76-81,107-112` only handle `DexBacked*Payload` (it relies on an `assert`).
  - **Fix: Easy.** Use the interfaces, as Soot does.

- [ ] **The move-exception local is never retyped.**
  - **Where:** `realType` and `stmtToRetype` in `instruction/MoveExceptionInstruction.java` are unused.
  - **Soot:** retypes it after splitting (`soot/dexpler/DexBody.java:746-748,1758`).
  - **Fix: Easy.**

- [ ] **Dead code to remove.**
  - `FieldInstruction.getSootFieldRef` builds `JInstanceFieldRef(null, …)` (`instruction/FieldInstruction.java:60-63`).
  - `DexFileProvider` has a hard-coded `multiple_dex = true` and an unreachable single-dex branch that tells users about a nonexistent `-process-multiple-dex` option (`:127,154-169`).
  - ~~The class-modifier map in `ApkAnalysisInputLocation.java:100-111` is computed but never read.~~ Removed.
  - ~~The `classInformation == null` branches in `DexClassSource` are unreachable, because the provider already returns empty.~~ Removed.
  - `DexUtil.getClassTypeFromClassName` prints to stdout and rethrows a bare `RuntimeException` with no cause (`Util/DexUtil.java:145-150`).
  - **Fix: Easy.**

- [ ] **Correction to the previous TODO: don't delete `tag/*OpTag`.**
  - They look unused because the op-kind mechanism they belong to was never wired up (🟠2). Keep them, or replace them with a side table, when porting that.

## ⚪ Low Risk / Low Reward — backlog

- [ ] `sootup.apk.parser` and `sootup.apk.backend` are dead directories at the repo root (only stale `target/` output, not in the root `pom.xml`). The module also has no README or `package-info.java`. `Sootup DexToJimple Flow.pdf` isn't linked from anywhere.
- [ ] Positions: `LinePosition(-1)` is attached before the first debug line entry, where "no position" is more correct. No bytecode offsets are recorded (Soot has `BytecodeOffsetTag`).
- [ ] The packed-switch default target is registered twice (`instruction/SwitchInstruction.java:108-109`). It's harmless, because `DexBody.convertMultimap` keeps the first entry.
- [ ] 2-byte `fill-array-data` elements go through `shortValue()`, so `char` values ≥ 0x8000 become negative (`FillArrayDataInstruction.java:151-153`). Soot has the same bug.
- [ ] No `FieldStaticnessCorrector`/`MethodStaticnessCorrector`, so an `sget` on an instance field stays static.
- [ ] Dex `DECLARED_SYNCHRONIZED` (0x20000) isn't mapped to synchronized. Soot behaves the same.
- [ ] Cosmetic Soot passes are missing: `DexJumpChainShortener`, `DexReturnPacker`, `ConstantCastEliminator`, `IdentityCastEliminator`, `IdentityOperationEliminator`, `ArrayWriteAggregator`.
- [ ] `DexUtil.toSootType` maps an invalid descriptor to `UnknownType` (`Util/DexUtil.java:84-85`). Soot throws.

## Already fixed

- Operand order in field and array stores (`Aput`/`Iput`/`Sput`/`FieldInstruction.getAssignStmt`) used to be swapped (PR `fix-operand-order-in-field-and-array-store-instructions2`). Keep this in mind when touching the instruction classes: register and operand mix-ups have happened here before.
