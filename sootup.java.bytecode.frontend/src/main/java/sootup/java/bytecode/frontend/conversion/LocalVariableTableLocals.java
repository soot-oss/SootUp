/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2026 Markus Schmidt and others
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

package sootup.java.bytecode.frontend.conversion;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ISTORE;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.*;
import sootup.core.model.LocalVariableInfo;
import sootup.core.model.LocalVariableScope;
import sootup.java.core.jimple.basic.JavaLocal;

/**
 * Creates one Local per LocalVariableTable variable instead of one Local per slot, so variables of
 * disjoint scopes that reuse a slot keep their own names (and types).
 *
 * <p>This is only done for slots whose accesses are all covered by the LocalVariableTable. If a
 * single access of a slot is not covered, the slot falls back to one Local for the whole method
 * (see {@link #resolve}), since splitting it by (incomplete) debug information could separate a
 * definition from its uses.
 *
 * <p>Everything is computed once, on construction: the entry every slot access resolves to, and a
 * collision-free name per variable. The first variable (by scope start) with a name keeps it,
 * further ones are numbered {@code name_1}, {@code name_2}, ... skipping names the table uses. So
 * lookups during conversion are map accesses, and names do not depend on conversion order.
 */
class LocalVariableTableLocals {

  /**
   * Identifies a source variable in the LocalVariableTable. A variable may consist of multiple
   * entries (e.g. {@code int x; if (c) x = 1; else x = 2;}), which share slot, name and descriptor.
   */
  private record LvtVariable(int index, String name, String desc) {
    LvtVariable(LocalVariableNode lvn) {
      this(lvn.index, lvn.name, lvn.desc);
    }
  }

  @Nullable private final List<LocalVariableNode> localVariables;
  @NonNull private final InsnList instructions;

  /** Position of an instruction in {@link #instructions}. */
  @NonNull private final ToIntFunction<AbstractInsnNode> insnIndex;

  /** LocalVariableTable entries grouped by slot. */
  @NonNull private final Map<Integer, List<LocalVariableNode>> entriesBySlot = new HashMap<>();

  /** Entry each access (load/store/iinc/ret) of a splittable slot resolves to. */
  @NonNull private final Map<AbstractInsnNode, LocalVariableNode> entryOfInsn = new HashMap<>();

  /** Entry of each splittable this/parameter slot at method entry. */
  @NonNull private final Map<Integer, LocalVariableNode> entryOfPreamble = new HashMap<>();

  /** Collision-free name of each variable of a splittable slot. */
  @NonNull private final Map<LvtVariable, String> names = new HashMap<>();

  @NonNull private final Map<LvtVariable, JavaLocal> locals = new LinkedHashMap<>();

  /**
   * @param preambleSlots slots of this/parameters, defined at method entry
   */
  LocalVariableTableLocals(
      @Nullable List<LocalVariableNode> localVariables,
      @NonNull InsnList instructions,
      @NonNull ToIntFunction<AbstractInsnNode> insnIndex,
      @NonNull Collection<Integer> preambleSlots) {
    this.localVariables = localVariables;
    this.instructions = instructions;
    this.insnIndex = insnIndex;
    if (localVariables == null || localVariables.isEmpty()) {
      return;
    }
    for (LocalVariableNode lvn : localVariables) {
      entriesBySlot.computeIfAbsent(lvn.index, s -> new ArrayList<>()).add(lvn);
    }
    Set<Integer> splittable = resolveAccesses(preambleSlots);
    assignNames(localVariables, splittable);
  }

  /**
   * Returns the LVT entry {@code atInsn} (an access of a slot) resolves to if its slot is
   * splittable, otherwise {@code null}.
   */
  @Nullable LocalVariableNode resolve(@NonNull AbstractInsnNode atInsn) {
    return entryOfInsn.get(atInsn);
  }

  /**
   * Returns the LVT entry of this/parameter slot {@code idx} at method entry if the slot is
   * splittable, otherwise {@code null}.
   */
  @Nullable LocalVariableNode resolvePreamble(int idx) {
    return entryOfPreamble.get(idx);
  }

  /** The collision-free name of the variable of {@code lvn}, an entry {@link #resolve}d before. */
  @NonNull String nameOf(@NonNull LocalVariableNode lvn) {
    return names.get(new LvtVariable(lvn));
  }

  /**
   * Returns the Local of the variable of {@code lvn}; it is created once via {@code factory}, which
   * is passed the variable's name.
   */
  @NonNull JavaLocal getOrCreate(
      @NonNull LocalVariableNode lvn, @NonNull Function<String, JavaLocal> factory) {
    LvtVariable variable = new LvtVariable(lvn);
    JavaLocal local = locals.get(variable);
    if (local == null) {
      local = factory.apply(names.get(variable));
      locals.put(variable, local);
    }
    return local;
  }

  /** Registers the Local of a this/parameter slot as the Local of its LVT variable {@code lvn}. */
  void registerPreambleLocal(@NonNull LocalVariableNode lvn, @NonNull JavaLocal local) {
    locals.put(new LvtVariable(lvn), local);
  }

  /** Every name {@link #nameOf} can return, to be kept free of other Locals. */
  @NonNull Collection<String> reservedNames() {
    return names.values();
  }

  @NonNull Collection<JavaLocal> getLocals() {
    return locals.values();
  }

  /**
   * Captures scope membership in original bytecode order, before CFG layout and optimization.
   * Scopes cover [start, end): the start label is included and the end label is excluded.
   */
  @NonNull Map<AbstractInsnNode, LocalVariableScope> createScopes() {
    if (localVariables == null || localVariables.isEmpty()) {
      return Collections.emptyMap();
    }

    // `starts` tracks the indices in localVariables whose start label is this node
    Map<AbstractInsnNode, List<Integer>> starts = new IdentityHashMap<>();
    // `ends` tracks the indices in localVariables whose end label is this node (exclusive)
    Map<AbstractInsnNode, List<Integer>> ends = new IdentityHashMap<>();
    List<LocalVariableInfo> variables = new ArrayList<>(localVariables.size());
    for (int varIdx = 0; varIdx < localVariables.size(); varIdx++) {
      LocalVariableNode node = localVariables.get(varIdx);
      variables.add(new LocalVariableInfo(node.name, node.index, node.desc));

      // get the instruction indices of the start and end labels
      int startInsnIdx = insnIndex.applyAsInt(node.start);
      int endInsnIdx = insnIndex.applyAsInt(node.end);

      // add only valid ranges (ignore empty, reversed, or missing-label ranges)
      if (startInsnIdx >= 0 && endInsnIdx > startInsnIdx) {
        starts.computeIfAbsent(node.start, key -> new ArrayList<>()).add(varIdx);
        ends.computeIfAbsent(node.end, key -> new ArrayList<>()).add(varIdx);
      }
    }

    Map<AbstractInsnNode, LocalVariableScope> result = new IdentityHashMap<>();

    // when iterating over instructions, will keep pairs of variable indices and LocalVariableInfo
    // records
    // that are currently active at iterated instruction.
    Map<Integer, LocalVariableInfo> activeVariables = new TreeMap<>();

    // caches already computed scopes by list of variables (order sensitive)
    Map<List<LocalVariableInfo>, LocalVariableScope> sharedScopes = new HashMap<>();
    LocalVariableScope activeScope = LocalVariableScope.empty();
    for (AbstractInsnNode insn : instructions) {
      List<Integer> endedVarIdxs = ends.get(insn);
      List<Integer> startedVarIdxs = starts.get(insn);

      // add variables that start at this instruction and remove variables that end at this
      // instruction
      if (startedVarIdxs != null) {
        startedVarIdxs.forEach(index -> activeVariables.put(index, variables.get(index)));
      }
      if (endedVarIdxs != null) {
        endedVarIdxs.forEach(activeVariables::remove);
      }

      // update active scope if any variable started or ended at this instruction
      if (endedVarIdxs != null || startedVarIdxs != null) {
        activeScope =
            sharedScopes.computeIfAbsent(
                List.copyOf(activeVariables.values()), LocalVariableScope::of);
      }

      result.put(insn, activeScope);
    }
    return result;
  }

  /**
   * Resolves every access of every slot, and returns the slots for which all of them are covered by
   * an entry — for this/parameter slots including the definition at method entry. Only the
   * resolutions of those slots are kept.
   */
  @NonNull
  private Set<Integer> resolveAccesses(@NonNull Collection<Integer> preambleSlots) {
    Set<Integer> splittable = new HashSet<>(entriesBySlot.keySet());
    Map<AbstractInsnNode, LocalVariableNode> resolved = new HashMap<>();
    for (AbstractInsnNode insn = instructions.getFirst(); insn != null; insn = insn.getNext()) {
      int slot;
      if (insn instanceof VarInsnNode) {
        slot = ((VarInsnNode) insn).var;
      } else if (insn instanceof IincInsnNode) {
        slot = ((IincInsnNode) insn).var;
      } else {
        continue;
      }
      if (!splittable.contains(slot)) {
        continue;
      }
      LocalVariableNode lvn = findEntry(slot, insn);
      if (lvn == null) {
        splittable.remove(slot);
      } else {
        resolved.put(insn, lvn);
      }
    }
    for (int slot : preambleSlots) {
      if (!splittable.contains(slot)) {
        continue;
      }
      LocalVariableNode lvn = findEntry(slot, null);
      if (lvn == null) {
        splittable.remove(slot);
      } else {
        entryOfPreamble.put(slot, lvn);
      }
    }
    resolved.forEach(
        (insn, lvn) -> {
          if (splittable.contains(lvn.index)) {
            entryOfInsn.put(insn, lvn);
          }
        });
    return splittable;
  }

  /**
   * Names the variables of the splittable slots. Variables are visited in the order their scopes
   * start, so the first one with a name keeps it and later ones — in another slot or of another
   * type — are numbered, skipping any name the table itself uses.
   */
  private void assignNames(
      @NonNull List<LocalVariableNode> localVariables, @NonNull Set<Integer> splittable) {
    List<LocalVariableNode> ordered = new ArrayList<>();
    for (LocalVariableNode lvn : localVariables) {
      if (splittable.contains(lvn.index)) {
        ordered.add(lvn);
      }
    }
    ordered.sort(
        Comparator.<LocalVariableNode>comparingInt(lvn -> insnIndex.applyAsInt(lvn.start))
            .thenComparingInt(lvn -> lvn.index));

    Set<String> taken = new HashSet<>();
    for (LocalVariableNode lvn : localVariables) {
      taken.add(lvn.name);
    }
    Set<String> claimed = new HashSet<>();
    for (LocalVariableNode lvn : ordered) {
      LvtVariable variable = new LvtVariable(lvn);
      if (names.containsKey(variable)) {
        continue;
      }
      String name = lvn.name;
      if (!claimed.add(name)) {
        for (int i = 1; ; i++) {
          String candidate = lvn.name + "_" + i;
          if (taken.add(candidate)) {
            name = candidate;
            break;
          }
        }
      }
      names.put(variable, name);
    }
  }

  @Nullable
  private LocalVariableNode findEntry(int idx, @Nullable AbstractInsnNode atInsn) {
    List<LocalVariableNode> entries = entriesBySlot.get(idx);
    if (entries == null) {
      return null;
    }
    int op = atInsn == null ? -1 : atInsn.getOpcode();
    if (atInsn == null || (op >= ISTORE && op <= ASTORE)) {
      // the scope of a variable starts directly *after* its defining store
      AbstractInsnNode n = atInsn == null ? instructions.getFirst() : atInsn.getNext();
      for (; n != null && n.getOpcode() < 0; n = n.getNext()) {
        if (n instanceof LabelNode) {
          for (LocalVariableNode lvn : entries) {
            if (lvn.start == n) {
              return lvn;
            }
          }
        }
      }
      if (atInsn == null) {
        if (n == null) {
          return null;
        }
        atInsn = n;
      }
    }
    int insnIdx = insnIndex.applyAsInt(atInsn);
    for (LocalVariableNode lvn : entries) {
      if (insnIdx >= insnIndex.applyAsInt(lvn.start) && insnIdx < insnIndex.applyAsInt(lvn.end)) {
        return lvn;
      }
    }
    return null;
  }
}
