package sootup.core.jimple.visitor;

/**
 * Base class for retrieving a calculated result from the implemented Visitor (which is basically a
 * switch via OOP)
 *
 * @author Markus Schmidt
 */
public abstract class AbstractVisitor<V> implements Visitor {

  protected V result = null;

  public V getResult() {
    return result;
  }

  protected void setResult(V result) {
    this.result = result;
  }
}
