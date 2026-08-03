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

import java.io.PrintWriter;
import java.time.Instant;

public class ProgressTracker {
    private final PrintWriter out;
    private final boolean showProgress;
    public final long totalBytes;
    private volatile long uploadedBytes = 0;
    private volatile int lastPercent = -1;
    private boolean finished = false;
    private final int barWidth = 40;
    private final Instant startTime = Instant.now();

    public ProgressTracker(PrintWriter out, boolean showProgress, long totalBytes) {
        this.out = out;
        this.showProgress = showProgress;
        this.totalBytes = totalBytes;
    }

    /**
     * Creates a progress accounting handle scoped to a single upload part. Each part reports its
     * bytes through the returned {@link PartProgress}, which can roll back only its own bytes on a
     * failed attempt — safe even when many parts upload concurrently.
     */
    public PartProgress newPart() {
        return new PartProgress(this);
    }

    /** Current cumulative uploaded-bytes count. Package-private, for observability in tests. */
    synchronized long currentBytes() {
        return uploadedBytes;
    }

    synchronized void update(long count) {
        uploadedBytes += count;
        int percent = (int) ((uploadedBytes * 100) / totalBytes);
        if (percent != lastPercent) {
            lastPercent = percent;

            long elapsedMillis = java.time.Duration.between(startTime, Instant.now()).toMillis();
            long elapsedSeconds = elapsedMillis / 1000;
            double speed = uploadedBytes / (elapsedMillis / 1000.0);
            double eta = (elapsedMillis <= 0 || !Double.isFinite(speed) || speed <= 0)
                    ? 0.0 : (totalBytes - uploadedBytes) / speed;
            if (showProgress) {
                if (totalBytes > 1024) {
                    renderBar(percent, uploadedBytes / 1024, totalBytes / 1024, "KBs", eta, elapsedSeconds);
                }
                else {
                    renderBar(percent, uploadedBytes, totalBytes, "bytes", eta, elapsedSeconds);
                }
            }
        }
        if (showProgress && percent == 100 && !finished) {
            finished = true;
            out.println("");
        }
    }

    private void renderBar(int percent, long current, long total, String sizeUnitLabel, double eta, long elapsedSeconds) {
        int filled = (int) ((percent / 100.0) * barWidth);
        String bar = "[" + "=".repeat(filled) + " ".repeat(barWidth - filled) + "]";
        out.printf("\r Progress: %s %3d%% (%d/%d %s, ETA: %.1fs, Elapsed: %ds)", bar, percent, current, total, sizeUnitLabel, eta, elapsedSeconds);
    }
}