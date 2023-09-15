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

package qilin.pta.tools;

import qilin.pta.toolkits.selectx.Selectx;

import java.util.Map;

public class SelectxPTA extends PartialCallSiteSensPTA {

    public SelectxPTA(int ctxLen) {
        super(ctxLen);
        System.out.println("selectx ... ");
    }

    //=========context selector=============

    @Override
    protected Map<Object, Integer> calculatingNode2Length() {
        System.out.print("Construct transPAG...");
        long time = System.currentTimeMillis();

        prePAG = prePTA.getPag();
        Selectx selectx = new Selectx(prePTA);

        System.out.println((System.currentTimeMillis() - time) / 1000 + "s");

        System.out.println("Propagate..");
        return selectx.process();
    }

}
