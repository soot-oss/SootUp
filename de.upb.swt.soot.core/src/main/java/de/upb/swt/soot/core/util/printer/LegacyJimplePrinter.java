package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JSwitchStmt;
import de.upb.swt.soot.core.model.Body;

/** StmtPrinter implementation for normal (full) Jimple for OldSoot */
/*
 *  List of differences between old and current Jimple:
 * - tableswitch and lookupswitch got merged into switch
 * - now imports are possible
 *
 * */

public class LegacyJimplePrinter extends NormalStmtPrinter {

  public LegacyJimplePrinter(Body b) {
    super(b);
  }

  @Override
  public void stmt(Stmt currentStmt) {
    startStmt(currentStmt);
    // replace switch with lookupswitch (TODO: [ms] or tableswitch if possible)
    if (currentStmt instanceof JSwitchStmt) {
      // prepend to switch Stmt
      literal("lookup");
    }
    currentStmt.toString(this);
    endStmt(currentStmt);
    literal(";");
    newline();
  }
}
