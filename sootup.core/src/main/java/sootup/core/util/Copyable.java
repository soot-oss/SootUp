package sootup.core.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

// TODO: [ms] Copyable makes no sense anymore as all stmts are immutable

/**
 * Marker interface for final classes that are immutable and can be copied. For instance:
 *
 * <pre>
 *   final class Foo {
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
