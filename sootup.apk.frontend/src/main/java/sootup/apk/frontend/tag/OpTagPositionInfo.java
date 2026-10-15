package sootup.apk.frontend.tag;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import org.jf.dexlib2.Opcode;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.main.Tag;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * Position info that also records the operand kind of the dex instruction (int, long, float or
 * double), which Jimple itself does not show: {@code add-int} and {@code add-float} both become
 * {@code a + b}. Stmts are immutable and keep their position info when rewritten, so the tag
 * survives interceptors such as LocalSplitter.
 */
public class OpTagPositionInfo extends SimpleStmtPositionInfo {

  @NonNull private final Tag opTag;

  public OpTagPositionInfo(int lineNumber, @NonNull Tag opTag) {
    super(lineNumber);
    this.opTag = opTag;
  }

  @NonNull
  public Tag getOpTag() {
    return opTag;
  }

  /** The operand kind of an arithmetic, compare or cast opcode; for casts the source type. */
  @NonNull
  public static Tag forOpcode(@NonNull Opcode opcode) {
    String name = opcode.name();
    int to = name.indexOf("_TO_");
    String operand = to >= 0 ? name.substring(0, to) : name;
    if (operand.contains("FLOAT")) {
      return new FloatOpTag();
    } else if (operand.contains("DOUBLE")) {
      return new DoubleOpTag();
    } else if (operand.contains("LONG")) {
      return new LongOpTag();
    }
    return new IntOpTag();
  }

  public static boolean isFloatingPointOp(@NonNull Stmt stmt) {
    return stmt.getPositionInfo() instanceof OpTagPositionInfo info
        && (info.opTag instanceof FloatOpTag || info.opTag instanceof DoubleOpTag);
  }
}
