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

import qilin.util.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PTAPattern {
    private ContextKind ctxKind;
    private Approach approach;
    private int k, hk;

    public PTAPattern(String ptacmd) {
        parsePTACommand(ptacmd);
    }

    private Pattern getSupportPTAPattern() {
        String approachPtn = "(" + String.join("|", Approach.approachAliases()) + ")";
        String ctxLenPtn = "(\\d*)";
        String ctxKindPtn = "(" + String.join("|", ContextKind.contextAliases()) + ")";
        String heapLenPtn = "(\\+?(\\d*)h(eap)?)?";
        String regexPattern = "^(" + approachPtn + "-)?" + ctxLenPtn + ctxKindPtn + heapLenPtn + "$";
        return Pattern.compile(regexPattern);
    }

    private void parsePTACommand(String ptacmd) {
        Pattern pattern = getSupportPTAPattern();
        Matcher matcher = pattern.matcher(ptacmd);
        if (!matcher.find()) {
            throw new RuntimeException("Unsupported PTA: " + ptacmd + " !");
        }

        String approachString = matcher.group(2);
        String kString = matcher.group(3);
        String typeString = matcher.group(4);
        String hkString = matcher.group(6);

        approach = Approach.toApproach(approachString);
        k = kString.equals("") ? 1 : Integer.parseInt(kString);
        ctxKind = ContextKind.toCtxKind(typeString);
        hk = hkString == null ? -1 : hkString.equals("") ? 1 : Integer.parseInt(hkString);

        if (k == 0) {
            ctxKind = ContextKind.INSENS;
        }
        if (hk == -1) {
            hk = k - 1;
        }
        validateApproachCompatibility();
        validateContextLength();
    }

    private void validateApproachCompatibility() {
        switch (ctxKind) {
            case HYBOBJ:
                if (approach != Approach.DATADRIVEN && approach != Approach.NONE && approach != Approach.TUNNELING) {
                    throw new RuntimeException("Approach <" + approach.toString() + " > is currently not designed for hybrid sensitivity");
                }
                break;
            case HYBTYPE:
                if (approach != Approach.NONE) {
                    throw new RuntimeException("Approach <" + approach.toString() + " > is currently not designed for hybrid sensitivity");
                }
                break;
            case TYPE: {
                if (approach != Approach.NONE && approach != Approach.TUNNELING) {
                    throw new RuntimeException("Approach <" + approach.toString() + "> is currently not designed for type sensitivity.");
                }
            }
            break;
            case CALLSITE: {
                if (approach != Approach.NONE && approach != Approach.ZIPPER && approach != Approach.MAHJONG
                        && approach != Approach.DATADRIVEN && approach != Approach.TUNNELING
                        && approach != Approach.SELECTX
                ) {
                    throw new RuntimeException("Approach <" + approach.toString() + "> is currently not designed for call-site sensitivity.");
                }
            }
            break;
            case OBJECT: {
                if (approach != Approach.EAGLE && approach != Approach.ZIPPER && approach != Approach.TURNER &&
                        approach != Approach.DATADRIVEN && approach != Approach.TUNNELING
                        && approach != Approach.MERCURIAL && approach != Approach.MAHJONG && approach != Approach.BEAN
                        && approach != Approach.NONE) {
                    throw new RuntimeException("Approach <" + approach.toString() + "> is currently not designed for object sensitivity.");
                }
            }
            break;
            case INSENS: {
                if (approach != Approach.SPARK && approach != Approach.NONE) {
                    throw new RuntimeException("Approach <" + approach.toString() + "> is currently not designed for context insensitive pointer analysis.");
                }
            }
            default:
                break;
        }
    }

    private void validateContextLength() {
        if (ctxKind == ContextKind.INSENS) {
            return;
        }
        if (hk > k) {
            throw new RuntimeException("Heap context depth cannot exceed method context depth!");
        }
        if (hk < k - 1 && (ctxKind == ContextKind.OBJECT || ctxKind == ContextKind.TYPE)) {
            throw new RuntimeException("Heap context depth can only be k or k-1 for object/type-sensitive analysis!");
        }
    }

    public ContextKind getContextKind() {
        return ctxKind;
    }

    public Approach getApproach() {
        return approach;
    }

    public int getContextDepth() {
        return k;
    }

    public int getHeapContextDepth() {
        return hk;
    }

    @Override
    public String toString() {
        if (ctxKind == ContextKind.INSENS) {
            return "insensitive";
        }
        StringBuilder builder = new StringBuilder();
        if (!approach.toString().isEmpty()) {
            builder.append(approach);
            builder.append('-');
        }
        builder.append(k);
        builder.append(ctxKind.toString());
        builder.append('+');
        builder.append(hk);
        builder.append("heap");
        return builder.toString();
    }

    public enum Approach {
        // for insens
        NONE,
        SPARK,
        //
        BEAN,
        ZIPPER,
        EAGLE,
        TURNER,
        DATADRIVEN,
        TUNNELING,
        MERCURIAL,
        MAHJONG,
        SELECTX;

        static final Map<String, Approach> approaches = new HashMap<>();

        static {
            Util.add(approaches, BEAN, "bean", "B");
            Util.add(approaches, ZIPPER, "zipper", "Z");
            Util.add(approaches, EAGLE, "eagle", "E");
            Util.add(approaches, TURNER, "turner", "T");
            Util.add(approaches, MERCURIAL, "mercurial", "hg");
            Util.add(approaches, MAHJONG, "mahjong", "M");
            Util.add(approaches, DATADRIVEN, "datadriven", "D");
            Util.add(approaches, TUNNELING, "tunneling", "t");
            Util.add(approaches, SELECTX, "selectx", "s");
            Util.add(approaches, SPARK, "insens", "ci");
        }

        public static Collection<String> approachAliases() {
            return approaches.keySet();
        }

        public static Approach toApproach(String name) {
            return approaches.getOrDefault(name, NONE);
        }

        @Override
        public String toString() {
            return switch (this) {
                case DATADRIVEN -> "data-driven";
                case TUNNELING -> "tunneling";
                case BEAN -> "bean";
                case ZIPPER -> "zipper";
                case EAGLE -> "eagle";
                case TURNER -> "turner";
                case MERCURIAL -> "mercurial";
                case MAHJONG -> "mahjong";
                case SELECTX -> "selectx";
                case SPARK -> "spark";
                default -> "";
            };
        }
    }
}
