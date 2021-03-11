package pkgmain;

import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;

import pkgb.B;
import pkgc.C;

public class Main {
    public static void main(String[] args) throws IOException {
        Main mymain = new Main();		
        B myb = new B();

        C myc = myb.getMyC(); 
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt() + ", C: " + myc.doIt());
        
        System.out.println("B: Get text from modb's properties: " + myb.getTextFromProperties());
        System.out.println("C: Get text from modc's properties: " + myc.getTextFromProperties());
        
        // ------------------------------------------------------------------------------------------------------------

        // works, as resources.properties in modc is not encapsulated (because its location is not a package)
        
        System.out.println("B: Get text from modc's properties: " + myb.getTextFromMODCsProperties());

        // ------------------------------------------------------------------------------------------------------------

        System.out.println("Main: Get text from modb's /pkgb/b.properties                 , whose package is opened:                 " + getTextFromMODBProperties("/pkgb/b.properties"));
        System.out.println("Main: Get text from modb's /pkgbinternal/binternal.properties , whose package is not opened:             " + getTextFromMODBProperties("/pkgbinternal/pbinternal.properties"));
        
        System.out.println("Main: Get text from modc's /pkgc/c.properties                 , whose package is opened:                 " + getTextFromMODCProperties("/pkgc/c.properties"));
        System.out.println("Main: Get text from modc's /pkgcinternal/cinternal.properties , whose package is not opened:             " + getTextFromMODCProperties("/pkgcinternal/cinternal.properties"));
        System.out.println("Main: Get text from modc's /cnopackage.properties             , whose package is in the unnamed package: " + getTextFromMODCProperties("/cnopackage.properties"));
    }
    
    public static String getTextFromMODBProperties(String resourceFileName) throws IOException {
		final Properties properties = new Properties();
		try (final InputStream stream = B.class.getModule().getResourceAsStream(resourceFileName)) {
		    try {
		    	properties.load(stream);
				return properties.getProperty("text", "modb's " + resourceFileName + " not found in modmain");
		    }
		    catch (NullPointerException npex) {
				return "ERROR: Cannot be loaded";
		    }
		}
    }

    public static String getTextFromMODCProperties(String resourceFileName) throws IOException {
		final Properties properties = new Properties();
		try (final InputStream stream = C.class.getModule().getResourceAsStream(resourceFileName)) {
		    try {
		    	properties.load(stream);
				return properties.getProperty("text", "modc's " + resourceFileName + " not found in modmain");
		    }
		    catch (NullPointerException npex) {
				return "ERROR: Cannot be loaded";
		    }
		}
    }
}
