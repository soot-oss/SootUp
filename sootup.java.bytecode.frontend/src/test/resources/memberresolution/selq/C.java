package selq;

// overrides B1.m, which overrides A.m, so C.m overrides A.m as well
public class C extends selp.B1 {
  public void m() {}
}
