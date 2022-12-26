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

package qilin.pta.toolkits.turner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DFA {
    public enum State {
        S, FLOW, IFLOW, E, ERROR;
    }

    public enum TranCond {
        PARAM, IPARAM, ASSIGN, IASSIGN, LOAD, ILOAD, STORE, ISTORE, NEW, INEW, CSLIKELY;
    }

    private static final Map<State, Map<TranCond, State>> transitionFunc = new HashMap<>();

    static {
        Map<TranCond, State> mS = transitionFunc.computeIfAbsent(State.S, k -> new HashMap<>());
        mS.put(TranCond.PARAM, State.FLOW);

        Map<TranCond, State> mFlow = transitionFunc.computeIfAbsent(State.FLOW, k -> new HashMap<>());
        mFlow.put(TranCond.ASSIGN, State.FLOW);
        mFlow.put(TranCond.LOAD, State.FLOW);
        mFlow.put(TranCond.STORE, State.IFLOW);
        mFlow.put(TranCond.ISTORE, State.IFLOW);
        mFlow.put(TranCond.NEW, State.FLOW);

        Map<TranCond, State> mIFlow = transitionFunc.computeIfAbsent(State.IFLOW, k -> new HashMap<>());
        mIFlow.put(TranCond.IASSIGN, State.IFLOW);
        mIFlow.put(TranCond.ILOAD, State.IFLOW);
        mIFlow.put(TranCond.IPARAM, State.E);
        mIFlow.put(TranCond.INEW, State.IFLOW);
        mIFlow.put(TranCond.CSLIKELY, State.FLOW);
    }

    public static State nextState(State curr, TranCond tranCond) {
        return transitionFunc.getOrDefault(curr, Collections.emptyMap()).getOrDefault(tranCond, State.ERROR);
    }
}