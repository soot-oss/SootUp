package sootup.core.typehierarchy;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.base.Suppliers;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Stores class relations as dense integer ids plus two encodings tailored to the two different
 * shapes the Java type hierarchy actually has:
 *
 * <ul>
 *   <li><b>class-extends-class</b> is single inheritance, i.e. a forest. Each class gets a
 *       contiguous <code>[rangeStart, rangeEnd)</code> position range into a single L2R pre-order
 *       DFS array ({@link RawAdjacency}/{@link TypeHierarchyEncoding#classHierarchyOrder}), so "is
 *       B a subtype of A" is an O(1) range-containment test and "all subtypes of A" is an O(k)
 *       array slice - no traversal needed. This mirrors phasar's {@code DIBasedTypeHierarchy}
 *       encoding for (single-inheritance) C++ class hierarchies.
 *   <li><b>interface-extends-interface</b> and <b>class-implements-interface</b> form a general DAG
 *       (multiple supertypes), which a single contiguous range cannot encode. Interfaces get their
 *       own dense sub-numbering and a bitset per interface holding all transitively extended
 *       interfaces (self-inclusive), computed once via bottom-up DP over the interface DAG. Subtype
 *       tests against an interface are then O(1) bit tests instead of a graph walk.
 * </ul>
 *
 * <p>Documentation about how to use it is available <a
 * href="https://soot-oss.github.io/SootUp/latest/typehierarchy/">here</a>.
 *
 * @author Christian Brüggemann
 */
public class ViewTypeHierarchy implements MutableTypeHierarchy {

  private static final Logger logger = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final View view;

  /**
   * Cheap, incrementally-mutated-in-place adjacency (dense ids + direct edges only). Built once,
   * memoized, then mutated directly by {@link #addType(SootClass)} - unlike {@link
   * #typeHierarcBacking}, it never needs to be rebuilt wholesale.
   */
  private final Supplier<RawAdjacency> lazyRaw;

  /**
   * The expensive derived encoding (pre-order ranges for the class tree, transitive-closure bitsets
   * for the interface DAG). {@code null} means "dirty, rebuild from {@link #lazyRaw} on next
   * access". Invalidated wholesale by {@link #addType(SootClass)}, since a structural insertion can
   * shift every subsequent pre-order position - mutation is rare relative to reads, so a full
   * rebuild on the first read after a mutation is cheap in practice.
   */
  private ViewTypeHierarchy.@Nullable TypeHierarchyEncoding typeHierarcBacking;

  private final ClassType objectClassType;

  /**
   * Caches the result of {@link #subtypeClassesOf(ClassType)}, i.e. {@link #subtypesOf(ClassType)}
   * already resolved to {@link SootClass} via the {@link View} - a real lookup cost, unlike {@link
   * #subtypesOf(ClassType)} itself, which is a cheap array slice and so is computed fresh on every
   * call. Invalidated wholesale by {@link #addType(SootClass)}.
   */
  private final Map<ClassType, List<? extends SootClass>> subtypeClassesCache = new HashMap<>();

  private final Map<SymmetricKey, Set<ClassType>> lcaCache = new HashMap<>();

  private final Set<ClassType> reportedUnresolvableTypes =
      Collections.synchronizedSet(new HashSet<>());

  /**
   * Bumped every time {@link #addType(SootClass)} actually mutates the hierarchy. Exposed via
   * {@link #getModificationCount()} so external caches derived from hierarchy queries can detect
   * staleness.
   */
  private long modificationCount = 0;

  /** to allow caching use Typehierarchy.fromView() to get/create the Typehierarchy. */
  public ViewTypeHierarchy(@NonNull View view) {
    this.view = view;
    lazyRaw = Suppliers.memoize(() -> buildRawAdjacency(view));
    objectClassType = view.getIdentifierFactory().getClassType("java.lang.Object");
  }

  @Override
  public long getModificationCount() {
    return modificationCount;
  }

  /**
   * Looks up the dense id for the given type. If the type was not reached during the initial scan
   * (e.g. because it is only referenced from a method signature, cast, or catch clause rather than
   * declared as a super type), this attempts to resolve and add it to the hierarchy on demand via
   * the {@link View}. Returns {@code -1} only if the type cannot be resolved at all, e.g. because
   * it is an optional/vendor-provided type that is absent from the classpath.
   */
  private int resolveId(@NonNull ClassType type) {
    RawAdjacency raw = lazyRaw.get();
    Integer id = raw.idOf.get(type);
    if (id != null) {
      return id;
    }
    Optional<? extends SootClass> sootClass = view.getClass(type);
    if (sootClass.isEmpty()) {
      if (reportedUnresolvableTypes.add(type)) {
        logger.warn(
            "Could not find '{}' in the type hierarchy - add the jar/classpath entry defining it to analyze it.",
            type);
      }
      return -1;
    }
    addType(sootClass.get());
    Integer resolved = raw.idOf.get(type);
    return resolved == null ? -1 : resolved;
  }

  private TypeHierarchyEncoding getTypeHierarcBacking() {
    TypeHierarchyEncoding d = typeHierarcBacking;
    if (d == null) {
      d = buildDerived(lazyRaw.get());
      typeHierarcBacking = d;
    }
    return d;
  }

  @NonNull
  @Override
  public Stream<ClassType> implementersOf(@NonNull ClassType interfaceType) {
    int id = resolveId(interfaceType);
    if (id == -1) {
      return Stream.empty();
    }
    if (!lazyRaw.get().isInterface.get(id)) {
      throw new IllegalArgumentException("'" + interfaceType + "' is not an interface.");
    }
    return subtypesOf(interfaceType);
  }

  @NonNull
  @Override
  public Stream<ClassType> subclassesOf(@NonNull ClassType classType) {
    int id = resolveId(classType);
    if (id == -1) {
      return Stream.empty();
    }
    if (lazyRaw.get().isInterface.get(id)) {
      throw new IllegalArgumentException("'" + classType + "' is not a class.");
    }
    return subtypesOf(classType);
  }

  @NonNull
  @Override
  public Stream<ClassType> subinterfacesOf(@NonNull ClassType interfaceType) {
    int id = resolveId(interfaceType);
    if (id == -1) {
      return Stream.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    // Mirrors the pre-existing (undocumented) behavior of silently returning an empty stream
    // rather than throwing when given a class type.
    if (!raw.isInterface.get(id)) {
      return Stream.empty();
    }
    TypeHierarchyEncoding d = getTypeHierarcBacking();
    int dense = d.denseOfInterfaceId[id];
    return Arrays.stream(d.descendants[dense])
        .filter(x -> x != dense)
        .mapToObj(x -> raw.classTypeOf.get(d.interfaceIdOfDense[x]));
  }

  @NonNull
  @Override
  public Stream<ClassType> subtypesOf(@NonNull ClassType type) {
    int id = resolveId(type);
    if (id == -1) {
      return Stream.empty();
    }

    RawAdjacency raw = lazyRaw.get();
    TypeHierarchyEncoding d = getTypeHierarcBacking();
    return raw.isInterface.get(id) ? interfaceSubtypesOf(id, raw, d) : classSubtypesOf(id, raw, d);
  }

  @NonNull
  @Override
  public Stream<? extends SootClass> subtypeClassesOf(@NonNull ClassType type) {
    List<? extends SootClass> cached = subtypeClassesCache.get(type);
    if (cached != null) {
      return cached.stream();
    }

    List<ClassType> subtypes = subtypesOf(type).toList();
    List<SootClass> computed = new ArrayList<>(subtypes.size());
    for (ClassType classType : subtypes) {
      view.getClass(classType).ifPresent(computed::add);
    }
    subtypeClassesCache.put(type, computed);
    return computed.stream();
  }

  @NonNull
  @Override
  public Stream<ClassType> directSubtypesOf(@NonNull ClassType type) {
    int id = resolveId(type);
    if (id == -1) {
      return Stream.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    if (raw.isInterface.get(id)) {
      return Stream.concat(
          raw.directSubinterfaces.get(id).stream().mapToObj(raw.classTypeOf::get),
          raw.directImplementers.get(id).stream().mapToObj(raw.classTypeOf::get));
    }
    return raw.directSubclasses.get(id).stream().mapToObj(raw.classTypeOf::get);
  }

  @Override
  public Stream<ClassType> directlyImplementedInterfacesOf(@NonNull ClassType classType) {
    int id = resolveId(classType);
    if (id == -1) {
      return Stream.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    if (raw.isInterface.get(id)) {
      throw new IllegalArgumentException(classType + " is not a class.");
    }
    return raw.directImplementedInterfaces.get(id).stream().mapToObj(raw.classTypeOf::get);
  }

  @NonNull
  @Override
  public Stream<ClassType> directlyExtendedInterfacesOf(@NonNull ClassType interfaceType) {
    int id = resolveId(interfaceType);
    if (id == -1) {
      return Stream.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    if (!raw.isInterface.get(id)) {
      throw new IllegalArgumentException(interfaceType + " is not an interface.");
    }
    return raw.directExtendedInterfaces.get(id).stream().mapToObj(raw.classTypeOf::get);
  }

  @Override
  public boolean contains(ClassType type) {
    return resolveId(type) != -1;
  }

  /**
   * This algorithm is an adaptation of
   * https://www.baeldung.com/cs/lowest-common-ancestor-acyclic-graph split across the two
   * differently-encoded parts of the hierarchy: the (proper) class-chain ancestors of {@code a} and
   * {@code b} are compared directly via the parent-array, while their interface ancestors are
   * compared via bitset intersection.
   */
  @Override
  public Collection<ClassType> getLowestCommonAncestors(ClassType a, ClassType b) {
    SymmetricKey pair = new SymmetricKey(a, b);
    Set<ClassType> cached = lcaCache.get(pair);
    if (cached != null) {
      return cached;
    }

    int idA = resolveId(a);
    int idB = resolveId(b);
    RawAdjacency raw = lazyRaw.get();
    TypeHierarchyEncoding d = getTypeHierarcBacking();

    Set<ClassType> lcas;
    if (idA == -1 || idB == -1) {
      lcas = Collections.singleton(objectClassType);
      lcaCache.put(pair, lcas);
      return lcas;
    }

    Ancestors ancA = computeAncestors(idA, raw, d);
    Ancestors ancB = computeAncestors(idB, raw, d);

    if (ancA.isEmpty() || ancB.isEmpty()) {
      lcas = Collections.singleton(objectClassType);
      lcaCache.put(pair, lcas);
      return lcas;
    }

    lcas = new HashSet<>();

    // Class-side candidate: the nearest common element of both (proper-ancestor) class chains.
    // Since each chain is a single root-terminated path, the first element of ancA's chain that
    // also appears in ancB's chain is necessarily the nearest one.
    int classPartLca = -1;
    if (ancA.classChain.size() > 0 && ancB.classChain.size() > 0) {
      Set<Integer> bChainSet = new HashSet<>();
      for (int i = 0; i < ancB.classChain.size(); i++) {
        bChainSet.add(ancB.classChain.get(i));
      }
      for (int i = 0; i < ancA.classChain.size(); i++) {
        int candidate = ancA.classChain.get(i);
        if (bChainSet.contains(candidate)) {
          classPartLca = candidate;
          break;
        }
      }
    }

    long[] commonInterfaceBits = ancA.interfaceBits.clone();
    andInto(commonInterfaceBits, ancB.interfaceBits);

    if (classPartLca != -1) {
      lcas.add(raw.classTypeOf.get(classPartLca));
      // Drop any interface candidate that classPartLca itself transitively implements - the
      // class always dominates an interface it implements.
      long[] lcaOwnInterfaceBits = new long[commonInterfaceBits.length];
      int cur = classPartLca;
      while (cur != -1) {
        unionImplementedInterfaces(cur, raw, d, lcaOwnInterfaceBits);
        cur = raw.superClassId.get(cur);
      }
      for (int w = 0; w < commonInterfaceBits.length; w++) {
        commonInterfaceBits[w] &= ~lcaOwnInterfaceBits[w];
      }
    }

    List<Integer> candidateDense = new ArrayList<>();
    forEachSetBit(commonInterfaceBits, candidateDense::add);
    for (int i = 0; i < candidateDense.size(); i++) {
      int candidate = candidateDense.get(i);
      boolean dominated = false;
      for (int j = 0; j < candidateDense.size(); j++) {
        if (i == j) {
          continue;
        }
        int other = candidateDense.get(j);
        // `other` dominates `candidate` iff `other` transitively extends `candidate`.
        if (testBit(d.ancestorsBitset[other], candidate)) {
          dominated = true;
          break;
        }
      }
      if (!dominated) {
        lcas.add(raw.classTypeOf.get(d.interfaceIdOfDense[candidate]));
      }
    }

    if (lcas.isEmpty()) {
      lcas = Collections.singleton(objectClassType);
    }
    lcaCache.put(pair, lcas);
    return lcas;
  }

  @NonNull
  @Override
  public Stream<ClassType> implementedInterfacesOf(@NonNull ClassType type) {
    int id = resolveId(type);
    if (id == -1) {
      return Stream.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    TypeHierarchyEncoding d = getTypeHierarcBacking();

    if (!raw.isInterface.get(id)) {
      // We ascend from the class through its superclasses to java.lang.Object, unioning together
      // the (self-inclusive, transitive) interfaces each one directly implements.
      Set<ClassType> result = new LinkedHashSet<>();
      int cur = id;
      while (cur != -1) {
        IntArrayList impls = raw.directImplementedInterfaces.get(cur);
        for (int i = 0; i < impls.size(); i++) {
          addInterfaceAndAncestors(impls.get(i), raw, d, result);
        }
        cur = raw.superClassId.get(cur);
      }
      return result.stream();
    } else {
      int dense = d.denseOfInterfaceId[id];
      long[] bits = d.ancestorsBitset[dense];
      List<ClassType> result = new ArrayList<>();
      forEachSetBit(
          bits,
          j -> {
            if (j != dense) {
              result.add(raw.classTypeOf.get(d.interfaceIdOfDense[j]));
            }
          });
      return result.stream();
    }
  }

  @NonNull
  @Override
  public Optional<ClassType> superClassOf(@NonNull ClassType classType) {
    int id = resolveId(classType);
    if (id == -1) {
      return Optional.empty();
    }
    if (objectClassType.equals(classType)) {
      return Optional.empty();
    }
    RawAdjacency raw = lazyRaw.get();
    int superId = raw.superClassId.get(id);
    if (superId != -1) {
      return Optional.of(raw.classTypeOf.get(superId));
    }
    if (raw.isInterface.get(id)) {
      return Optional.of(objectClassType);
    }
    return Optional.empty();
  }

  @Override
  public boolean isInterface(@NonNull ClassType type) {
    int id = resolveId(type);
    return id != -1 && lazyRaw.get().isInterface.get(id);
  }

  public boolean isClass(@NonNull ClassType type) {
    int id = resolveId(type);
    return id != -1 && !lazyRaw.get().isInterface.get(id);
  }

  /**
   * O(1) class-class range containment / O(1) interface-interface bitset test / O(chain length)
   * class-subtype-of-interface chain walk - overrides the linear default in {@link TypeHierarchy}.
   */
  @Override
  public boolean isClassSubtype(
      @NonNull ClassType supertypeCt, @NonNull ClassType potentialSubtypeCt) {
    final String jlObject = "java.lang.Object";
    if (supertypeCt.getFullyQualifiedName().equals(jlObject)) {
      return !potentialSubtypeCt.getFullyQualifiedName().equals(jlObject);
    }

    int superId = resolveId(supertypeCt);
    int subId = resolveId(potentialSubtypeCt);
    if (superId == -1 || subId == -1 || superId == subId) {
      return false;
    }

    RawAdjacency raw = lazyRaw.get();
    TypeHierarchyEncoding d = getTypeHierarcBacking();
    boolean superIsInterface = raw.isInterface.get(superId);

    if (!superIsInterface) {
      // supertype is a class: an interface can never be a subtype of a (non-Object) class.
      if (raw.isInterface.get(subId)) {
        return false;
      }
      if (d.rangeStart[superId] == -1 || d.rangeStart[subId] == -1) {
        return false;
      }
      return d.rangeStart[superId] <= d.rangeStart[subId]
          && d.rangeStart[subId] < d.rangeEnd[superId];
    }

    int superDense = d.denseOfInterfaceId[superId];
    if (raw.isInterface.get(subId)) {
      int subDense = d.denseOfInterfaceId[subId];
      return testBit(d.ancestorsBitset[subDense], superDense);
    }

    // supertype is an interface, subtype is a class: walk the class's chain (self + superclasses)
    // and test each ancestor's directly-implemented interfaces' transitive closure.
    int cur = subId;
    while (cur != -1) {
      IntArrayList impls = raw.directImplementedInterfaces.get(cur);
      for (int i = 0; i < impls.size(); i++) {
        int implDense = d.denseOfInterfaceId[impls.get(i)];
        if (implDense == superDense || testBit(d.ancestorsBitset[implDense], superDense)) {
          return true;
        }
      }
      cur = raw.superClassId.get(cur);
    }
    return false;
  }

  @Override
  public void addType(@NonNull SootClass sootClass) {
    RawAdjacency raw = lazyRaw.get();
    boolean changed = addSootClassToGraph(sootClass, raw);
    if (!changed) {
      return;
    }
    typeHierarcBacking = null;
    subtypeClassesCache.clear();
    lcaCache.clear();
    modificationCount++;
  }

  private static Stream<ClassType> classSubtypesOf(
      int id, RawAdjacency raw, TypeHierarchyEncoding d) {
    int start = d.rangeStart[id];
    int end = d.rangeEnd[id];
    if (start == -1 || start + 1 >= end) {
      return Stream.empty();
    }
    return Arrays.stream(d.classHierarchyOrder, start + 1, end).mapToObj(raw.classTypeOf::get);
  }

  private static Stream<ClassType> interfaceSubtypesOf(
      int id, RawAdjacency raw, TypeHierarchyEncoding d) {
    int dense = d.denseOfInterfaceId[id];
    int[] subDense = d.descendants[dense];

    Stream<ClassType> subInterfaces =
        Arrays.stream(subDense)
            .filter(x -> x != dense)
            .mapToObj(x -> raw.classTypeOf.get(d.interfaceIdOfDense[x]));

    List<int[]> intervals = new ArrayList<>();
    collectImplementerIntervals(id, raw, d, intervals);
    for (int x : subDense) {
      if (x == dense) {
        continue;
      }
      collectImplementerIntervals(d.interfaceIdOfDense[x], raw, d, intervals);
    }
    Stream<ClassType> classes = mergeIntervalsToClassTypes(intervals, raw, d);

    return Stream.concat(subInterfaces, classes);
  }

  private static void collectImplementerIntervals(
      int interfaceId, RawAdjacency raw, TypeHierarchyEncoding d, List<int[]> intervals) {
    IntArrayList implementers = raw.directImplementers.get(interfaceId);
    for (int i = 0; i < implementers.size(); i++) {
      int classId = implementers.get(i);
      int start = d.rangeStart[classId];
      int end = d.rangeEnd[classId];
      if (start != -1) {
        intervals.add(new int[] {start, end});
      }
    }
  }

  private static Stream<ClassType> mergeIntervalsToClassTypes(
      List<int[]> intervals, RawAdjacency raw, TypeHierarchyEncoding d) {
    if (intervals.isEmpty()) {
      return Stream.empty();
    }
    intervals.sort(Comparator.comparingInt(iv -> iv[0]));
    List<int[]> merged = new ArrayList<>();
    int[] current = intervals.get(0).clone();
    for (int i = 1; i < intervals.size(); i++) {
      int[] next = intervals.get(i);
      if (next[0] <= current[1]) {
        current[1] = Math.max(current[1], next[1]);
      } else {
        merged.add(current);
        current = next.clone();
      }
    }
    merged.add(current);

    return merged.stream()
        .flatMapToInt(iv -> Arrays.stream(d.classHierarchyOrder, iv[0], iv[1]))
        .mapToObj(raw.classTypeOf::get);
  }

  private static void unionImplementedInterfaces(
      int classId, RawAdjacency raw, TypeHierarchyEncoding d, long[] out) {
    IntArrayList impls = raw.directImplementedInterfaces.get(classId);
    for (int i = 0; i < impls.size(); i++) {
      int dense = d.denseOfInterfaceId[impls.get(i)];
      orInto(out, d.ancestorsBitset[dense]);
    }
  }

  private static void addInterfaceAndAncestors(
      int implId, RawAdjacency raw, TypeHierarchyEncoding d, Set<ClassType> out) {
    out.add(raw.classTypeOf.get(implId));
    int dense = d.denseOfInterfaceId[implId];
    forEachSetBit(
        d.ancestorsBitset[dense],
        j -> {
          if (j != dense) {
            out.add(raw.classTypeOf.get(d.interfaceIdOfDense[j]));
          }
        });
  }

  /**
   * The proper (self-exclusive) ancestors of a single type, split into the class-chain part (empty
   * for interfaces) and a bitset of all interface ancestors (transitively implemented/extended).
   */
  private static final class Ancestors {
    final IntArrayList classChain;
    final long[] interfaceBits;

    Ancestors(IntArrayList classChain, long[] interfaceBits) {
      this.classChain = classChain;
      this.interfaceBits = interfaceBits;
    }

    boolean isEmpty() {
      if (classChain.size() > 0) {
        return false;
      }
      for (long word : interfaceBits) {
        if (word != 0) {
          return false;
        }
      }
      return true;
    }
  }

  private static Ancestors computeAncestors(int id, RawAdjacency raw, TypeHierarchyEncoding d) {
    IntArrayList classChain = new IntArrayList();
    long[] interfaceBits = new long[wordsFor(d.interfaceCount)];

    if (!raw.isInterface.get(id)) {
      unionImplementedInterfaces(id, raw, d, interfaceBits);
      int cur = raw.superClassId.get(id);
      while (cur != -1) {
        classChain.add(cur);
        unionImplementedInterfaces(cur, raw, d, interfaceBits);
        cur = raw.superClassId.get(cur);
      }
    } else {
      int dense = d.denseOfInterfaceId[id];
      orInto(interfaceBits, d.ancestorsBitset[dense]);
      clearBit(interfaceBits, dense);
    }
    return new Ancestors(classChain, interfaceBits);
  }

  private static RawAdjacency buildRawAdjacency(@NonNull View view) {
    RawAdjacency raw = new RawAdjacency();
    view.getClasses().forEach(sootClass -> addSootClassToGraph(sootClass, raw));
    return raw;
  }

  /** Returns {@code true} if this call actually added new structure (edges) to {@code raw}. */
  private static boolean addSootClassToGraph(SootClass sootClass, RawAdjacency raw) {
    ClassType type = sootClass.getType();
    boolean isInterface = sootClass.isInterface();
    int id = raw.getOrCreateId(type, isInterface);
    if (raw.scanned.get(id)) {
      return false;
    }
    raw.scanned.set(id);

    if (isInterface) {
      for (ClassType extendedInterface : sootClass.getInterfaces()) {
        int extendedId = raw.getOrCreateId(extendedInterface, true);
        raw.directExtendedInterfaces.get(id).add(extendedId);
        raw.directSubinterfaces.get(extendedId).add(id);
      }
    } else {
      for (ClassType implementedInterface : sootClass.getInterfaces()) {
        int ifaceId = raw.getOrCreateId(implementedInterface, true);
        raw.directImplementedInterfaces.get(id).add(ifaceId);
        raw.directImplementers.get(ifaceId).add(id);
      }
      Optional<? extends ClassType> superClass = sootClass.getSuperclass();
      if (superClass.isPresent()) {
        int superId = raw.getOrCreateId(superClass.get(), false);
        raw.superClassId.set(id, superId);
        raw.directSubclasses.get(superId).add(id);
      }
    }
    return true;
  }

  private static TypeHierarchyEncoding buildDerived(RawAdjacency raw) {
    int total = raw.size();

    // --- class tree: iterative L2R pre-order DFS -> contiguous [rangeStart,rangeEnd) per class id
    int[] rangeStart = new int[total];
    int[] rangeEnd = new int[total];
    Arrays.fill(rangeStart, -1);
    int classCount = 0;
    for (int i = 0; i < total; i++) {
      if (!raw.isInterface.get(i)) {
        classCount++;
      }
    }
    int[] order = new int[classCount];
    boolean[] visited = new boolean[total];
    int[] stackId = new int[classCount];
    int[] stackChildIdx = new int[classCount];
    int pos = 0;

    for (int root = 0; root < total; root++) {
      if (raw.isInterface.get(root) || visited[root] || raw.superClassId.get(root) != -1) {
        continue;
      }
      int sp = 0;
      stackId[0] = root;
      stackChildIdx[0] = 0;
      visited[root] = true;
      rangeStart[root] = pos;
      order[pos++] = root;

      while (sp >= 0) {
        int curId = stackId[sp];
        IntArrayList children = raw.directSubclasses.get(curId);
        int ci = stackChildIdx[sp];
        if (ci < children.size()) {
          stackChildIdx[sp] = ci + 1;
          int child = children.get(ci);
          if (!visited[child]) {
            visited[child] = true;
            rangeStart[child] = pos;
            order[pos++] = child;
            sp++;
            stackId[sp] = child;
            stackChildIdx[sp] = 0;
          }
        } else {
          rangeEnd[curId] = pos;
          sp--;
        }
      }
    }

    // --- interface DAG: dense sub-numbering + bottom-up transitive-closure bitsets
    int interfaceCount = 0;
    int[] denseOfInterfaceId = new int[total];
    Arrays.fill(denseOfInterfaceId, -1);
    int[] interfaceIdOfDenseTmp = new int[total];
    for (int i = 0; i < total; i++) {
      if (raw.isInterface.get(i)) {
        denseOfInterfaceId[i] = interfaceCount;
        interfaceIdOfDenseTmp[interfaceCount] = i;
        interfaceCount++;
      }
    }
    int[] interfaceIdOfDense = Arrays.copyOf(interfaceIdOfDenseTmp, interfaceCount);

    int words = wordsFor(interfaceCount);
    long[][] ancestorsBitset = new long[interfaceCount][words];
    byte[] state = new byte[interfaceCount];
    for (int dense = 0; dense < interfaceCount; dense++) {
      computeInterfaceAncestors(
          dense, raw, denseOfInterfaceId, interfaceIdOfDense, ancestorsBitset, state);
    }

    List<IntArrayList> descBuild = new ArrayList<>(interfaceCount);
    for (int i = 0; i < interfaceCount; i++) {
      descBuild.add(new IntArrayList());
    }
    for (int dense = 0; dense < interfaceCount; dense++) {
      int finalDense = dense;
      forEachSetBit(ancestorsBitset[dense], j -> descBuild.get(j).add(finalDense));
    }
    int[][] descendants = new int[interfaceCount][];
    for (int i = 0; i < interfaceCount; i++) {
      descendants[i] = descBuild.get(i).toArray();
    }

    return new TypeHierarchyEncoding(
        rangeStart,
        rangeEnd,
        order,
        interfaceCount,
        denseOfInterfaceId,
        interfaceIdOfDense,
        ancestorsBitset,
        descendants);
  }

  /**
   * Cycle-guarded (state: 0 unvisited, 1 in-progress, 2 done) - real interface DAGs are acyclic.
   */
  private static void computeInterfaceAncestors(
      int dense,
      RawAdjacency raw,
      int[] denseOfInterfaceId,
      int[] interfaceIdOfDense,
      long[][] ancestorsBitset,
      byte[] state) {
    if (state[dense] != 0) {
      return;
    }
    state[dense] = 1;
    long[] bits = ancestorsBitset[dense];
    setBit(bits, dense);
    int id = interfaceIdOfDense[dense];
    IntArrayList exts = raw.directExtendedInterfaces.get(id);
    for (int i = 0; i < exts.size(); i++) {
      int targetId = exts.get(i);
      int targetDense = denseOfInterfaceId[targetId];
      if (targetDense < 0) {
        continue;
      }
      computeInterfaceAncestors(
          targetDense, raw, denseOfInterfaceId, interfaceIdOfDense, ancestorsBitset, state);
      orInto(bits, ancestorsBitset[targetDense]);
    }
    state[dense] = 2;
  }

  private static int wordsFor(int bitCount) {
    return (bitCount + 63) >> 6;
  }

  private static void setBit(long[] bits, int idx) {
    bits[idx >> 6] |= 1L << (idx & 63);
  }

  private static void clearBit(long[] bits, int idx) {
    bits[idx >> 6] &= ~(1L << (idx & 63));
  }

  private static boolean testBit(long[] bits, int idx) {
    return (bits[idx >> 6] & (1L << (idx & 63))) != 0;
  }

  private static void orInto(long[] dst, long[] src) {
    for (int i = 0; i < dst.length; i++) {
      dst[i] |= src[i];
    }
  }

  private static void andInto(long[] dst, long[] src) {
    for (int i = 0; i < dst.length; i++) {
      dst[i] &= src[i];
    }
  }

  private static void forEachSetBit(long[] bits, IntConsumer consumer) {
    for (int w = 0; w < bits.length; w++) {
      long word = bits[w];
      while (word != 0) {
        int b = Long.numberOfTrailingZeros(word);
        consumer.accept((w << 6) + b);
        word &= word - 1;
      }
    }
  }

  /** Minimal growable primitive-int array - avoids boxing overhead for adjacency lists. */
  static final class IntArrayList {
    private static final int[] EMPTY = new int[0];
    private int[] data = EMPTY;
    private int size = 0;

    void add(int value) {
      if (size == data.length) {
        data = Arrays.copyOf(data, Math.max(4, data.length * 2));
      }
      data[size++] = value;
    }

    int get(int index) {
      return data[index];
    }

    void set(int index, int value) {
      data[index] = value;
    }

    int size() {
      return size;
    }

    IntStream stream() {
      return Arrays.stream(data, 0, size);
    }

    int[] toArray() {
      return Arrays.copyOf(data, size);
    }
  }

  /** Dense ids + direct-edge adjacency only. Cheap to mutate incrementally in {@link #addType}. */
  static final class RawAdjacency {
    /** Assigns each {@link ClassType} the dense id used to index every array/list below. */
    final Map<ClassType, Integer> idOf = new HashMap<>();

    /** Reverse of {@link #idOf}: dense id -&gt; {@link ClassType}. */
    final List<ClassType> classTypeOf = new ArrayList<>();

    /** Bit {@code id} set iff that id denotes an interface rather than a class. */
    final BitSet isInterface = new BitSet();

    /**
     * Bit {@code id} set once that id's own declaration (its {@code extends}/{@code implements}
     * edges) has been recorded, as opposed to merely having a placeholder id allocated because it
     * was referenced as someone else's supertype. Guards {@link #addSootClassToGraph} against
     * adding duplicate edges if the same {@link SootClass} is passed to {@link #addType} more than
     * once.
     */
    final BitSet scanned = new BitSet();

    // class tree (single inheritance) - classes only
    /** Direct superclass id per class id, or {@code -1} if none (root, or unresolved). */
    final IntArrayList superClassId = new IntArrayList();

    /** Direct subclass ids per class id; reverse of {@link #superClassId}. */
    final List<IntArrayList> directSubclasses = new ArrayList<>();

    /** Interface ids a class id directly {@code implements} (not transitively). */
    final List<IntArrayList> directImplementedInterfaces = new ArrayList<>();

    // interface DAG - interfaces only
    /** Interface ids an interface id directly {@code extends} (not transitively). */
    final List<IntArrayList> directExtendedInterfaces = new ArrayList<>();

    /**
     * Interface ids directly {@code extend}-ing an interface id; reverse of {@link
     * #directExtendedInterfaces}.
     */
    final List<IntArrayList> directSubinterfaces = new ArrayList<>();

    /**
     * Class ids directly {@code implements}-ing an interface id; reverse of {@link
     * #directImplementedInterfaces}.
     */
    final List<IntArrayList> directImplementers = new ArrayList<>();

    int getOrCreateId(ClassType type, boolean asInterface) {
      Integer existing = idOf.get(type);
      if (existing != null) {
        return existing;
      }
      int id = classTypeOf.size();
      classTypeOf.add(type);
      idOf.put(type, id);
      if (asInterface) {
        isInterface.set(id);
      }
      superClassId.add(-1);
      directSubclasses.add(new IntArrayList());
      directImplementedInterfaces.add(new IntArrayList());
      directExtendedInterfaces.add(new IntArrayList());
      directSubinterfaces.add(new IntArrayList());
      directImplementers.add(new IntArrayList());
      return id;
    }

    int size() {
      return classTypeOf.size();
    }
  }

  /** The expensive, lazily-(re)built encoding derived from a {@link RawAdjacency} snapshot. */
  static final class TypeHierarchyEncoding {
    /**
     * Per class id, its position in {@link #classHierarchyOrder} - or {@code -1} if the id was
     * never reached by the class-tree DFS (e.g. it sits on a cyclic/dangling chain that never
     * bottoms out at a root; not expected for real bytecode, handled defensively).
     */
    final int[] rangeStart;

    /**
     * Per class id, the exclusive end of its subtree's range: {@code [rangeStart[id],
     * rangeEnd[id])} covers the id itself plus every transitive subclass, contiguously, so "is X a
     * subtype of Y" reduces to one range-containment check on {@code rangeStart}.
     */
    final int[] rangeEnd;

    /**
     * All class ids in L2R pre-order DFS order, the array {@link #rangeStart}/{@link #rangeEnd}
     * index into. Slicing {@code [rangeStart[id]+1, rangeEnd[id])} out of this array yields exactly
     * the transitive subclasses of {@code id}, in one pass, without traversal.
     */
    final int[] classHierarchyOrder;

    /**
     * Number of interface ids in the hierarchy - the size of the interfaces' own dense id space,
     * kept separate from the (larger) class+interface id space in {@link RawAdjacency}.
     */
    final int interfaceCount;

    /**
     * Per (global) interface id, its dense 0..{@link #interfaceCount}-1 sub-id, or {@code -1} if
     * that id is a class rather than an interface. Indexes {@link #ancestorsBitset} and {@link
     * #descendants}.
     */
    final int[] denseOfInterfaceId;

    /** Reverse of {@link #denseOfInterfaceId}: dense sub-id -&gt; global interface id. */
    final int[] interfaceIdOfDense;

    /**
     * Per dense interface id, a bitset (indexed by dense id, {@link #interfaceCount} bits wide) of
     * every interface it transitively {@code extends}, self-inclusive. Computed once via bottom-up
     * DP over the interface DAG; makes "does interface X extend interface Y" an O(1) bit test
     * instead of a graph walk.
     */
    final long[][] ancestorsBitset;

    /**
     * Per dense interface id, the sorted array of dense ids of every interface that transitively
     * {@code extends} it, self-inclusive - the transpose of {@link #ancestorsBitset}, materialized
     * as arrays (not bitsets) since these are only ever enumerated, never bit-tested.
     */
    final int[][] descendants;

    TypeHierarchyEncoding(
        int[] rangeStart,
        int[] rangeEnd,
        int[] classHierarchyOrder,
        int interfaceCount,
        int[] denseOfInterfaceId,
        int[] interfaceIdOfDense,
        long[][] ancestorsBitset,
        int[][] descendants) {
      this.rangeStart = rangeStart;
      this.rangeEnd = rangeEnd;
      this.classHierarchyOrder = classHierarchyOrder;
      this.interfaceCount = interfaceCount;
      this.denseOfInterfaceId = denseOfInterfaceId;
      this.interfaceIdOfDense = interfaceIdOfDense;
      this.ancestorsBitset = ancestorsBitset;
      this.descendants = descendants;
    }
  }

  static class SymmetricKey extends ImmutablePair<ClassType, ClassType> {
    public SymmetricKey(ClassType left, ClassType right) {
      super(left, right);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> other = (Map.Entry) obj;
      return (Objects.equals(this.getKey(), other.getKey())
              && Objects.equals(this.getValue(), other.getValue()))
          || (Objects.equals(this.getKey(), other.getValue())
              && Objects.equals(this.getValue(), other.getKey()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(getKey()) + Objects.hash(getValue());
    }
  }
}
