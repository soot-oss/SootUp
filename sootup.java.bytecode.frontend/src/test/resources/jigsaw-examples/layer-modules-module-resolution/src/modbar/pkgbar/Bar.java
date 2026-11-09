package pkgbar;

import pkgcommon.IdGen;
import pkgcommon.LayerPrinter;

public class Bar {
	private String id;

	public Bar() throws Exception {
		// we are using functionality from modcommon here
		id = IdGen.createID();
		LayerPrinter.printRuntimeInfos(this.getClass().getModule().getLayer());
	}

	@Override
	public String toString() {
		return this.getClass().getName() + ", id=" + id + ", using automatic module version "
				+ new pkgversion.Version2().toString(); // we are using functionality from modversion2 here
	}
}