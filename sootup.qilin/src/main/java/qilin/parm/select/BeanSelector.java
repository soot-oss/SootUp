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

package qilin.parm.select;

import java.util.Map;
import qilin.core.context.Context;
import qilin.core.context.ContextElement;
import qilin.core.context.ContextElements;
import qilin.core.pag.AllocNode;
import qilin.core.pag.FieldValNode;
import qilin.core.pag.LocalVarNode;
import qilin.core.pag.PAG;
import sootup.core.model.SootMethod;

public class BeanSelector extends ContextSelector {
  private final PAG pag;
  private final Map<Object, Map<Object, Map<Object, Object>>> beanNexContextMap;
  // currently, we only support k = 2 and hk = 1;
  // we will generalize Bean in future.
  private final int k = 2;
  private final int hk = 1;

  public BeanSelector(PAG pag, Map<Object, Map<Object, Map<Object, Object>>> beanNexContextMap) {
    this.pag = pag;
    this.beanNexContextMap = beanNexContextMap;
  }

  @Override
  public Context select(SootMethod m, Context context) {
    return contextTailor(context, k);
  }

  @Override
  public Context select(LocalVarNode lvn, Context context) {
    return contextTailor(context, k);
  }

  @Override
  public Context select(FieldValNode fvn, Context context) {
    return contextTailor(context, k);
  }

  @Override
  public Context select(AllocNode heap, Context context) {
    assert context instanceof ContextElements;
    ContextElements ctxElems = (ContextElements) context;
    int s = ctxElems.size();
    if (s > 1) {
      ContextElement[] cxtAllocs = ctxElems.getElements();
      AllocNode allocator = (AllocNode) cxtAllocs[0];
      if (beanNexContextMap.containsKey(heap.getNewExpr())) {
        Map<Object, Map<Object, Object>> mMap1 = beanNexContextMap.get(heap.getNewExpr());
        if (mMap1.containsKey(allocator.getNewExpr())) {
          Map<Object, Object> mMap2 = mMap1.get(allocator.getNewExpr());
          AllocNode allocAllocNode = (AllocNode) cxtAllocs[1];
          if (allocAllocNode != null && mMap2.containsKey(allocAllocNode.getNewExpr())) {
            Object newContextNode = mMap2.get(allocAllocNode.getNewExpr());
            AllocNode newContextAllocNode = pag.getAllocNode(newContextNode);
            ContextElement[] array = new ContextElement[s];
            System.arraycopy(cxtAllocs, 0, array, 0, s);
            array[0] = newContextAllocNode;
            context = new ContextElements(array, s);
          }
        }
      }
    }
    return contextTailor(context, hk);
  }
}
