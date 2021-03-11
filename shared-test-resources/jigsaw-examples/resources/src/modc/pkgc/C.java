package pkgc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class C {
    public String doIt() {
        return "from C";
    }
    
    // access resources in modc!
    public String getTextFromProperties() throws IOException {
		final Properties properties = new Properties();
		try (final InputStream stream = this.getClass().getResourceAsStream("/resources.modc/resources.properties")) {
		    properties.load(stream);
		}
				
		return properties.getProperty("text", "not found");
    }
}
