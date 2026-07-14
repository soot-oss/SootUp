public class TryWithResourcesFinally {

	public static void nop() {}

	public void test0(final AutoCloseable ac0) throws Exception {
		try (AutoCloseable ac1 = ac0) {
			nop();
		} finally {
			throw new Exception();
		}
	}
}
