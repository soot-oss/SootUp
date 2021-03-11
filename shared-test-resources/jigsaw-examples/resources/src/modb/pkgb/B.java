package pkgb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import pkgc.*;

public class B {
    public String doIt() {
        return "from B";
    }

    public C getMyC() {
        return new C();
    }

    // access resources in modb
    public String getTextFromProperties() throws IOException {
    	String resourceFileName = "/resources.modb/resources.properties";		// now we get modb's resources.properties
    	
		final Properties properties = new Properties();
		try (final InputStream stream = B.class.getModule().getResourceAsStream(resourceFileName)) {
		    properties.load(stream);
		}
		return properties.getProperty("text", "modb's resources properties not found in modb");
    }

    // access resources in modc (from modb)
    public String getTextFromMODCsProperties() throws IOException {
    	String resourceFileName = "/pkgc/c.properties";		// The resource path must be resolveable as a package name *and* this package has to be open (see modc's module-info.java)
    														// Note that just an export on this package name is not sufficient for access!

    	final Properties properties = new Properties();
		try (final InputStream stream = C.class.getModule().getResourceAsStream(resourceFileName)) {
		    properties.load(stream);
		}
		String propertyFromMODC = properties.getProperty("text");
		if (propertyFromMODC == null) {
		    return "modc's resources properties not found via modb";
		}
		else {
		    return propertyFromMODC + " - but retrieved via modb!";
		}
    }
}
