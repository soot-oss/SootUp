/**
 * Two uses of the {@link FileHandle} protocol: one that respects it and one that does not.
 *
 * <p>{@code correctUsage} additionally performs the {@code write} inside a second method,
 * so the analysis only reaches the right answer if it follows the call.
 */
public class Example {

    public static void correctUsage() {
        FileHandle handle = new FileHandle();   // state: CLOSED
        handle.open();                          // state: OPEN
        writeGreeting(handle);                  // state: OPEN  (write happens in the callee)
        handle.close();                         // state: CLOSED
    }

    public static void protocolViolation() {
        FileHandle handle = new FileHandle();   // state: CLOSED
        handle.write("nothing is open yet");    // no CLOSED --write--> transition: ERROR
        handle.close();                         // still ERROR
    }

    private static void writeGreeting(FileHandle handle) {
        handle.write("hello");
    }
}
