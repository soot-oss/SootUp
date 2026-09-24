package pkgmain;

import myservice.IService;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Factory {
    public static IService create() {
        ServiceLoader<IService> sl = ServiceLoader.load(IService.class);
        Iterator<IService> iter = sl.iterator();
        if (!iter.hasNext()) {
            throw new RuntimeException("No service providers found!");
        }
        
        IService service = null;
        while (iter.hasNext()) {
        	service = iter.next();
        	System.out.println("Found " + service.getName());
        }
        
        // simply return the last implementation found in the ServiceLoader,
        //    no fancy choose algorithm necessary here ...
        return service;
    }
}