/**
 * A tiny API with a usage protocol: a handle must be opened before it is written to,
 * and it must be closed afterwards.
 *
 * <p>Nothing in the Java type system enforces that order. The typestate analysis in
 * {@code sootup.examples.typestate} does.
 */
public class FileHandle {

    /** Moves the handle from CLOSED to OPEN. */
    public void open() {
        // no-op: only the call sequence matters for the analysis
    }

    /** Legal only while the handle is OPEN. */
    public void write(String data) {
        // no-op
    }

    /** Moves the handle from OPEN back to CLOSED. */
    public void close() {
        // no-op
    }
}
