/*
 * Copyright 2021-2023, Seqera.
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
 *
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
    private final int barWidth = 40;
    private final Instant startTime = Instant.now();

    public ProgressTracker(PrintWriter out, boolean showProgress, long totalBytes) {
        this.out = out;
        this.showProgress = showProgress;
        this.totalBytes = totalBytes;
    }

    public synchronized void update(long count) {
        uploadedBytes += count;
        int percent = (int) ((uploadedBytes * 100) / totalBytes);
        if (percent != lastPercent) {
            lastPercent = percent;

            long elapsedMillis = java.time.Duration.between(startTime, Instant.now()).toMillis();
            double speed = uploadedBytes / (elapsedMillis / 1000.0);
            double eta = (totalBytes - uploadedBytes) / speed;
            if (showProgress) {
                if (totalBytes > 1024) {
                    renderBar(percent, uploadedBytes / 1024, totalBytes / 1024, "KBs", eta);
                }
                else {
                    renderBar(percent, uploadedBytes, totalBytes, "bytes", eta);
                }
            }
        }
        if (showProgress && percent == 100 ) {
            out.println("");
        }
    }

    private void renderBar(int percent, long current, long total, String sizeUnitLabel,double eta) {
        int filled = (int) ((percent / 100.0) * barWidth);
        String bar = "[" + "=".repeat(filled) + " ".repeat(barWidth - filled) + "]";
        out.printf("\r Progress: %s %3d%% (%d/%d %s, ETA: %.1fs)", bar, percent, current, total, sizeUnitLabel, eta);
    }
}