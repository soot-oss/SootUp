/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.util;

public class Stopwatch {
    private final String name;
    private long elapsedTime;
    private long startTime;
    private boolean inCounting;

    public static Stopwatch newAndStart(final String name) {
        Stopwatch stopwatch = new Stopwatch(name);
        stopwatch.start();
        return stopwatch;
    }

    private Stopwatch(final String name) {
        this.elapsedTime = 0L;
        this.inCounting = false;
        this.name = name;
    }

    private void start() {
        if (!this.inCounting) {
            this.inCounting = true;
            this.startTime = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (this.inCounting) {
            this.elapsedTime += System.currentTimeMillis() - this.startTime;
            this.inCounting = false;
        }
    }

    public float elapsed() {
        return this.elapsedTime / 1000.0f;
    }

    public void reset() {
        this.elapsedTime = 0L;
        this.inCounting = false;
    }

    public void restart() {
        reset();
        start();
    }

    @Override
    public String toString() {
        return String.format("%s elapsed time: %.2fs", this.name, this.elapsed());
    }
}
