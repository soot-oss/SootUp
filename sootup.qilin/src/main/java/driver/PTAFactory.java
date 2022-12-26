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

package driver;

import qilin.core.PTA;
import qilin.parm.ctxcons.*;
import qilin.pta.PTAConfig;
import qilin.pta.tools.*;

public class PTAFactory {
    public static PTA createPTA(PTAPattern ptaPattern) {
        switch (ptaPattern.getContextKind()) {
            case HYBOBJ: {
                switch (ptaPattern.getApproach()) {
                    case DATADRIVEN -> {
                        // data-driven hybrid-2obj, Sehun Jeong oopsla'17
                        CtxConstructor ctxCons = new HybObjCtxConstructor();
                        return new DataDrivenPTA(ctxCons);
                    }
                    case TUNNELING -> {
                        CtxConstructor ctxCons = new HybObjCtxConstructor();
                        return new TunnelingPTA(ctxCons, ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                    default -> {
                        // static method using callsite as context, Yannis pldi'13
                        return new HybridObjectSensPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                }
            }
            case OBJECT: {
                switch (ptaPattern.getApproach()) {
                    case EAGLE -> {
                        // k-obj pointer analysis with Eagle pre-analysis, Jingbo OOPSLA'19
                        assert ptaPattern.getContextDepth() == ptaPattern.getHeapContextDepth() + 1;
                        BasePTA eagle = new EaglePTA(ptaPattern.getContextDepth());
                        if (PTAConfig.v().getPtaConfig().ctxDebloating) {
                            return new DebloatedPTA(eagle);
                        } else {
                            return eagle;
                        }
                    }
                    case BEAN -> {
                        CtxConstructor ctxCons = new ObjCtxConstructor();
                        return new BeanPTA(ctxCons);
                    }
                    case TURNER -> {
                        return new TurnerPTA(ptaPattern.getContextDepth());
                    }
                    case ZIPPER -> {
                        CtxConstructor ctxCons = new ObjCtxConstructor();
                        BasePTA zipperPTA = new ZipperPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth(), ctxCons);
                        if (PTAConfig.v().getPtaConfig().ctxDebloating) {
                            return new DebloatedPTA(zipperPTA);
                        } else {
                            return zipperPTA;
                        }
                    }
                    case MAHJONG -> {
                        CtxConstructor ctxCons = new ObjCtxConstructor();
                        BasePTA mahjongPTA = new MahjongPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth(), ctxCons);
                        if (PTAConfig.v().getPtaConfig().ctxDebloating) {
                            return new DebloatedPTA(mahjongPTA);
                        } else {
                            return mahjongPTA;
                        }
                    }
                    case DATADRIVEN -> {
                        CtxConstructor ctxCons = new ObjCtxConstructor();
                        return new DataDrivenPTA(ctxCons);
                    }
                    case TUNNELING -> {
                        CtxConstructor ctxCons = new ObjCtxConstructor();
                        return new TunnelingPTA(ctxCons, ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                    default -> {
                        BasePTA kobj = new ObjectSensPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());

                        if (PTAConfig.v().getPtaConfig().ctxDebloating) {
                            return new DebloatedPTA(kobj);
                        } else {
                            // normal object-sensitive pointer analysis, Milanova TOSEM'05
                            return kobj;
                        }
                    }
                }
            }
            case TYPE: {
                switch (ptaPattern.getApproach()) {
                    case DATADRIVEN -> {
                        CtxConstructor ctxCons = new TypeCtxConstructor();
                        return new DataDrivenPTA(ctxCons);
                    }
                    case TUNNELING -> {
                        CtxConstructor ctxCons = new TypeCtxConstructor();
                        return new TunnelingPTA(ctxCons, ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                    default -> {
                        // normal type-sensitive pointer analysis, Yannis popl'11
                        return new TypeSensPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                }
            }
            case CALLSITE: {
                switch (ptaPattern.getApproach()) {
                    case ZIPPER -> {
                        CtxConstructor ctxCons = new CallsiteCtxConstructor();
                        return new ZipperPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth(), ctxCons);
                    }
                    case MAHJONG -> {
                        CtxConstructor ctxCons = new CallsiteCtxConstructor();
                        return new MahjongPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth(), ctxCons);
                    }
                    case DATADRIVEN -> {
                        CtxConstructor ctxCons = new CallsiteCtxConstructor();
                        return new DataDrivenPTA(ctxCons);
                    }
                    case TUNNELING -> {
                        CtxConstructor ctxCons = new CallsiteCtxConstructor();
                        return new TunnelingPTA(ctxCons, ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                    case SELECTX -> {
                        return new SelectxPTA(ptaPattern.getContextDepth());
                    }
                    default -> {
                        // CallSite Sensitive
                        return new CallSiteSensPTA(ptaPattern.getContextDepth(), ptaPattern.getHeapContextDepth());
                    }
                }
            }
            case INSENS:
            default:
                return new Spark();
        }
    }
}
