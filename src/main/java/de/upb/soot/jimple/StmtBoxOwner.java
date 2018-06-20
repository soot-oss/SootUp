package de.upb.soot.jimple;

import java.util.List;

public interface StmtBoxOwner {

  public List<StmtBox> getStmtBoxes();

  public void clearStmtBoxes();
}
