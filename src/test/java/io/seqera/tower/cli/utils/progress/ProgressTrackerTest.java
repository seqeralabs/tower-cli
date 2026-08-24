/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.utils.progress;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTrackerTest {

    /** A clock whose current time is set explicitly by the test. */
    private static final class FakeClock implements LongSupplier {
        private final AtomicLong now = new AtomicLong(0);
        void set(long millis) { now.set(millis); }
        void advance(long millis) { now.addAndGet(millis); }
        @Override public long getAsLong() { return now.get(); }
    }

    private static ProgressTracker tracker(long totalBytes, PrintWriter out) {
        return new ProgressTracker(out, true, totalBytes);
    }

    private static long countRenders(StringWriter sw) {
        Matcher m = Pattern.compile("Progress:").matcher(sw.toString());
        long count = 0;
        while (m.find()) count++;
        return count;
    }

    @Test
    void resetRollsBackOnlyThisPartsBytes() {
        ProgressTracker t = tracker(100, new PrintWriter(new StringWriter()));
        PartProgress part = t.newPart();

        part.update(50);   // failed attempt reports 50 of 100
        part.reset();      // roll it back
        assertEquals(0, t.currentBytes());

        part.update(100);  // successful retry reports the full part
        assertEquals(100, t.currentBytes());
    }

    @Test
    void terminatingNewlinePrintedExactlyOnce() {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        ProgressTracker t = tracker(100, out);
        PartProgress part = t.newPart();

        part.update(100);  // reach 100% -> newline
        part.reset();      // dip back below 100%
        part.update(100);  // reach 100% again -> latch must suppress a second newline
        out.flush();

        long newlines = sw.toString().chars().filter(c -> c == '\n').count();
        assertEquals(1, newlines);
    }

    @Test
    void concurrentPartsWithRollbacksSettleAtTotal() throws Exception {
        int parts = 16;
        long bytesPerPart = 4096;
        long total = parts * bytesPerPart;

        ProgressTracker t = tracker(total, new PrintWriter(new StringWriter()));
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < parts; i++) {
                futures.add(pool.submit(() -> {
                    PartProgress part = t.newPart();
                    // Simulate two failed attempts that each report a partial amount and roll back,
                    // then a successful attempt that reports the full part in small increments.
                    for (int attempt = 0; attempt < 2; attempt++) {
                        part.update(bytesPerPart / 2);
                        part.reset();
                    }
                    for (long sent = 0; sent < bytesPerPart; sent += 512) {
                        part.update(512);
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        }

        // Every part's rollbacks only ever undid its own bytes, so the shared total lands exactly
        // on the sum of all committed parts — no cross-part corruption.
        assertEquals(total, t.currentBytes());
    }

    @Test
    void repaintsAtMostOncePerSecond() {
        StringWriter sw = new StringWriter();
        FakeClock clock = new FakeClock();
        ProgressTracker t = new ProgressTracker(new PrintWriter(sw), true, 1000, clock);
        PartProgress part = t.newPart();

        // First update paints immediately (initial frame).
        part.update(100);
        assertEquals(1, countRenders(sw));

        // More updates within the same second do not repaint.
        clock.advance(500);
        part.update(100);
        clock.advance(499);
        part.update(100);
        assertEquals(1, countRenders(sw));

        // Crossing the 1s boundary paints again.
        clock.advance(1);   // now 1000ms since the last paint
        part.update(100);
        assertEquals(2, countRenders(sw));
    }

    @Test
    void finalFrameShows100PercentAndNewlineEvenWithinOneSecond() {
        StringWriter sw = new StringWriter();
        FakeClock clock = new FakeClock();
        ProgressTracker t = new ProgressTracker(new PrintWriter(sw), true, 1000, clock);
        PartProgress part = t.newPart();

        // Complete the whole transfer inside a single sub-second window.
        part.update(400);   // first frame (immediate)
        part.update(600);   // reaches total within the same second -> forced final frame

        String out = sw.toString();
        assertTrue(out.contains("100%"), "final frame should show 100%");
        assertEquals(1, out.chars().filter(c -> c == '\n').count(), "exactly one terminating newline");
    }

    @Test
    void elapsedSecondsAreNonDecreasingAcrossPaints() {
        StringWriter sw = new StringWriter();
        FakeClock clock = new FakeClock();
        ProgressTracker t = new ProgressTracker(new PrintWriter(sw), true, 1000, clock);
        PartProgress part = t.newPart();

        part.update(100);            // Elapsed: 0s
        clock.advance(1000);
        part.update(100);            // Elapsed: 1s
        clock.advance(2000);
        part.update(100);            // Elapsed: 3s

        Matcher m = Pattern.compile("Elapsed: (\\d+)s").matcher(sw.toString());
        long previous = -1;
        int frames = 0;
        while (m.find()) {
            long elapsed = Long.parseLong(m.group(1));
            assertTrue(elapsed >= previous, "elapsed must be non-decreasing");
            previous = elapsed;
            frames++;
        }
        assertEquals(3, frames);
    }
}
