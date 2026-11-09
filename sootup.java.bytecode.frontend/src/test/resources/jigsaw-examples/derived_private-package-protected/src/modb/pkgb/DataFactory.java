package pkgb;

import pkgbinternal.*;

public class DataFactory {
    public Data createData() {     
        return new Data();                              // returns data which is exported in Module modb
    }

    public Data createInternalData1() {
        return new InternalData();                      // returns data which is not exported from Module modb
    }

// Note that this *does* compile though the method returns a type which is not visible outside of modb
// A compiler warning is shown (also in Eclipse)
    public InternalData createInternalData2() {
        return new InternalData();                      // returns data which is not exported from Module modb
    }
}
