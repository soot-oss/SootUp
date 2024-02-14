package instruction;

import main.DexBody;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.Type;

import java.util.List;
import java.util.Set;

/**
 * Interface for instructions that can/must be retyped, i.e. instructions that assign to a local and
 * have to retype it after local splitting.
 *
 * @author Michael Markert <michael.markert@googlemail.com>
 */
public interface ReTypeableInstruction {
  /**
   * Swap generic exception type with the given one.
   *
   * @param body the body that contains the instruction
   * @param t the real type.
   */
  public void setRealType(DexBody body, Type t);

  /**
   * Do actual retype.
   *
   * <p>Retyping is separated from setting the type, to make it possible to retype after local
   * splitting.
   *
   */
  public void retype(List<Stmt> stmt, Set<Local> locals);
}
