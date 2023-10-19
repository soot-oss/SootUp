package instruction;

import main.DexBody;
import sootup.core.model.Body;
import sootup.core.types.Type;

/**
 * Interface for instructions that can/must be retyped, i.e. instructions that assign to a local and have to retype it after
 * local splitting.
 *
 * @author Michael Markert <michael.markert@googlemail.com>
 */
public interface ReTypeableInstruction {
    /**
     * Swap generic exception type with the given one.
     *
     * @param body
     *          the body that contains the instruction
     * @param t
     *          the real type.
     */
    public void setRealType(DexBody body, Type t);

    /**
     * Do actual retype.
     *
     * Retyping is separated from setting the type, to make it possible to retype after local splitting.
     *
     * @param body
     *          The body containing the processed statement
     */
    public void retype(Body body);
}
