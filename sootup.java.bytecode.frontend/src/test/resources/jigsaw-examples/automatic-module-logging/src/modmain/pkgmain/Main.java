package pkgmain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static void main(String[] args) {
        // Logging - only using the slf4j API here 
        // (logging implementation is added via Runtime option by adding an automatic module,
        // see libraries of the three launch files variants)
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("This is Main - logging.");
    }
}
