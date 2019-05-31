package de.upb.soot.util;

/**
 * Marker interface for classes that are immutable and can be copied. For instance:
 *
 * <pre>
 *   class Foo {
 *     private final Bar bar;
 *     private final Baz baz;
 *
 *     public Foo(Bar bar, Baz baz) {
 *       this.bar = bar;
 *       this.baz = baz;
 *     }
 *
 *     public Foo withBar(Bar bar) {
 *      return new Foo(bar, baz);
 *     }
 *
 *     public Foo withBaz(Baz baz) {
 *       return new Foo(bar, baz);
 *     }
 *
 *     public Bar getBar() {
 *       return bar;
 *     }
 *
 *     public Baz getBaz() {
 *       return baz;
 *     }
 *   }
 * </pre>
 */
public interface Copyable {}
