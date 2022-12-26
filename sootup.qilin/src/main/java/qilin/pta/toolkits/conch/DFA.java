/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.pta.toolkits.conch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * This class defines two DFA used for leak analysis and depOnParam analysis introduced in our paper respectively.
 * */
public class DFA {
    public enum State {
        O, F, B, E, ERROR;
    }

    public enum TranCond {
        PARAM, I_PARAM, RETURN, I_RETURN, THROW, I_THROW, ASSIGN, I_ASSIGN,
        LOAD, I_LOAD,
        STORE, I_STORE, NEW, I_NEW,
        INTER_STORE, INTER_ASSIGN, I_INTER_LOAD;
    }

    private static final Map<State, Map<TranCond, State>> transitionFunc = new HashMap<>();

    /*
     * This is another automaton for PFG.
     * */
    private static final Map<State, Map<TranCond, State>> transitionFunc2 = new HashMap<>();

    static {
        // The first DFA introduced in Figure 13 for leak analysis.
        Map<TranCond, State> mO = transitionFunc.computeIfAbsent(State.O, k -> new HashMap<>());
        mO.put(TranCond.NEW, State.F);

        Map<TranCond, State> mF = transitionFunc.computeIfAbsent(State.F, k -> new HashMap<>());
        mF.put(TranCond.ASSIGN, State.F);
        mF.put(TranCond.STORE, State.B);
        mF.put(TranCond.INTER_STORE, State.B);
        mF.put(TranCond.INTER_ASSIGN, State.F);
        mF.put(TranCond.RETURN, State.E);
        mF.put(TranCond.THROW, State.E);

        Map<TranCond, State> mB = transitionFunc.computeIfAbsent(State.B, k -> new HashMap<>());
        mB.put(TranCond.I_ASSIGN, State.B);
        mB.put(TranCond.I_LOAD, State.B);
        mB.put(TranCond.I_PARAM, State.E);
        mB.put(TranCond.I_INTER_LOAD, State.B);
        mB.put(TranCond.I_NEW, State.O);

        // The second DFA introduced in Figure 15 for depOnParma analysis.
        Map<TranCond, State> mF2 = transitionFunc2.computeIfAbsent(State.F, k -> new HashMap<>());
        mF2.put(TranCond.NEW, State.F);
        mF2.put(TranCond.ASSIGN, State.F);
        mF2.put(TranCond.LOAD, State.F);
        mF2.put(TranCond.RETURN, State.E);
        mF2.put(TranCond.INTER_ASSIGN, State.F);
    }

    public static State nextState(State curr, TranCond tranCond) {
        return transitionFunc.getOrDefault(curr, Collections.emptyMap()).getOrDefault(tranCond, State.ERROR);
    }

    public static State nextState2(TranCond tranCond) {
        return transitionFunc2.get(State.F).getOrDefault(tranCond, State.ERROR);
    }
}
