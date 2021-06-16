package pkgcommon;

import java.util.UUID;

public class IdGen {
	public static String createID() {
		long id = UUID.randomUUID().getLeastSignificantBits();

		return String.format("%016X", id);
	}
}
