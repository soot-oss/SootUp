package de.upb.sootup.concrete.generics;

/**
 * @author Manuel Benz created on 13.07.18
 */
public class A<T> {
  T t;

  void setT(T t) {
    this.t = t;
  }

  T getT() {
    return this.t;
  }
}
