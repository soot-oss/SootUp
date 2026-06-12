package pkgb;

import pkgbinternal.*;

public class B extends InternalBSuperClass {
    public String doIt() {
        return "from B";								// can be called 
    }

    public String doIt_delegateToInternalClass() {
        return new InternalBHelper().doIt();			// can be called, does delegate to internal helper class
    }

    public String doIt_delegateToInternalSuperClass() {
        return super.doIt();							// can be called, does delegate to internal super class
    }

    protected String doIt_protected() {
        return "from B, but only protected";			// can be called only from derived class
    }

    String doIt_package() {
        return "from B, but only package";				// can be called only class in same package (i.e. only in same module, see example_splitpackages)
    }

    @SuppressWarnings("unused")
    private String doIt_internal() {
        return "from B, but only internal";				// can not be called from anywhere but from B itself (but is not ;-)
    }
}
