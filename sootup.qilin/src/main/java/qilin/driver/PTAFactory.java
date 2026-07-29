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

package qilin.driver;

import qilin.core.PTA;
import qilin.core.PTAScene;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.parm.ctxcons.*;
import qilin.pta.tools.*;
import sootup.core.views.View;

public class PTAFactory {
  public static PTA createPTA(
          PTAConfigPattern ptaConfigPattern, View view, String mainClassSig, PointerAnalysisConfig config) {
    PTAScene scene = new PTAScene(view, mainClassSig, config);
    switch (ptaConfigPattern.getContextKind()) {
      case HYBOBJ:
        {
            return switch (ptaConfigPattern.getApproach()) {
                case DATADRIVEN -> {
                    // data-driven hybrid-2obj, Sehun Jeong oopsla'17
                    CtxConstructor ctxCons = new HybObjCtxConstructor();
                    yield new DataDrivenPTA(scene, ctxCons);
                }
                case TUNNELING -> {
                    CtxConstructor ctxCons = new HybObjCtxConstructor();
                    yield new TunnelingPTA(
                            scene, ctxCons, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth());
                }
                default ->
                    // static method using callsite as context, Yannis pldi'13
                        new CoreVariantPTA(
                                scene,
                                ContextSensitivity.hybridObjectSensitive(
                                        ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth()));
            };
        }
      case OBJECT:
        {
          switch (ptaConfigPattern.getApproach()) {
            case EAGLE:
              {
                // k-obj pointer analysis with Eagle pre-analysis, Jingbo OOPSLA'19
                assert ptaConfigPattern.getContextDepth() == ptaConfigPattern.getHeapContextDepth() + 1;
                BasePTA eagle = new EaglePTA(scene, ptaConfigPattern.getContextDepth());
                if (config.isCtxDebloating()) {
                  return new DebloatedPTA(eagle, config.getDebloatApproach());
                } else {
                  return eagle;
                }
              }
            case BEAN:
              {
                CtxConstructor ctxCons = new ObjCtxConstructor();
                return new BeanPTA(scene, ctxCons);
              }
            case TURNER:
              {
                return new TurnerPTA(scene, ptaConfigPattern.getContextDepth());
              }
            case ZIPPER:
              {
                CtxConstructor ctxCons = new ObjCtxConstructor();
                BasePTA zipperPTA =
                    new ZipperPTA(
                        scene,
                        ptaConfigPattern.getContextDepth(),
                        ptaConfigPattern.getHeapContextDepth(),
                        ctxCons);
                if (config.isCtxDebloating()) {
                  return new DebloatedPTA(zipperPTA, config.getDebloatApproach());
                } else {
                  return zipperPTA;
                }
              }
            case MAHJONG:
              {
                CtxConstructor ctxCons = new ObjCtxConstructor();
                BasePTA mahjongPTA =
                    new MahjongPTA(
                        scene,
                        ptaConfigPattern.getContextDepth(),
                        ptaConfigPattern.getHeapContextDepth(),
                        ctxCons);
                if (config.isCtxDebloating()) {
                  return new DebloatedPTA(mahjongPTA, config.getDebloatApproach());
                } else {
                  return mahjongPTA;
                }
              }
            case DATADRIVEN:
              {
                CtxConstructor ctxCons = new ObjCtxConstructor();
                return new DataDrivenPTA(scene, ctxCons);
              }
            case TUNNELING:
              {
                CtxConstructor ctxCons = new ObjCtxConstructor();
                return new TunnelingPTA(
                    scene, ctxCons, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth());
              }
            default:
              {
                BasePTA kobj =
                    new CoreVariantPTA(
                        scene,
                        ContextSensitivity.objectSensitive(
                            ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth()));

                if (config.isCtxDebloating()) {
                  return new DebloatedPTA(kobj, config.getDebloatApproach());
                } else {
                  // normal object-sensitive pointer analysis, Milanova TOSEM'05
                  return kobj;
                }
              }
          }
        }
      case TYPE:
        {
            return switch (ptaConfigPattern.getApproach()) {
                case DATADRIVEN -> {
                    CtxConstructor ctxCons = new TypeCtxConstructor();
                    yield new DataDrivenPTA(scene, ctxCons);
                }
                case TUNNELING -> {
                    CtxConstructor ctxCons = new TypeCtxConstructor();
                    yield new TunnelingPTA(
                            scene, ctxCons, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth());
                }
                default ->
                    // normal type-sensitive pointer analysis, Yannis popl'11
                        new CoreVariantPTA(
                                scene,
                                ContextSensitivity.typeSensitive(
                                        ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth()));
            };
        }
      case CALLSITE:
        {
            return switch (ptaConfigPattern.getApproach()) {
                case ZIPPER -> {
                    CtxConstructor ctxCons = new CallsiteCtxConstructor();
                    yield new ZipperPTA(
                            scene, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth(), ctxCons);
                }
                case MAHJONG -> {
                    CtxConstructor ctxCons = new CallsiteCtxConstructor();
                    yield new MahjongPTA(
                            scene, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth(), ctxCons);
                }
                case DATADRIVEN -> {
                    CtxConstructor ctxCons = new CallsiteCtxConstructor();
                    yield new DataDrivenPTA(scene, ctxCons);
                }
                case TUNNELING -> {
                    CtxConstructor ctxCons = new CallsiteCtxConstructor();
                    yield new TunnelingPTA(
                            scene, ctxCons, ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth());
                }
                case SELECTX -> new SelectxPTA(scene, ptaConfigPattern.getContextDepth());
                default ->
                    // CallSite Sensitive
                        new CoreVariantPTA(
                                scene,
                                ContextSensitivity.callSite(
                                        ptaConfigPattern.getContextDepth(), ptaConfigPattern.getHeapContextDepth()));
            };
        }
      case INSENS:
      default:
        return new CoreVariantPTA(scene, ContextSensitivity.insensitive());
    }
  }
}
