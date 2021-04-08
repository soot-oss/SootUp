package benchmark.objects;

public class A {

	// Object A with attributes of type B

	public int i = 5;

	public B f = new B();
	public B g = new B();
	public B h;

	public A() {
	}

	public A(B b) {
		this.f = b;
	}

	public B getF() {
		return f;
	}
	public B getH() {
		return h;
	}
	public B id(B b) {
		return b;
	}

}
