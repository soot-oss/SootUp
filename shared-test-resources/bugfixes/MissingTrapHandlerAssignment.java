
public class MissingTrapHandlerAssignment{

    public long run() {
            try {
                return 1;
            } catch (Throwable t) {
                return 2;
            }
    }
}