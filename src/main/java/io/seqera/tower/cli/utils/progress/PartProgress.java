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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Progress accounting scoped to a single upload part, created once per part via
 * {@link ProgressTracker#newPart()} and reused across all of that part's retry attempts.
 *
 * <p>{@code reported} is an {@link AtomicLong} because {@link #update(long)} runs on the HTTP
 * client's internal executor thread while {@link #reset()} runs on the uploading worker thread.</p>
 */
public class PartProgress implements ProgressSink {

    private final ProgressTracker parent;
    private final AtomicLong reported = new AtomicLong();

    PartProgress(ProgressTracker parent) {
        this.parent = parent;
    }

    @Override
    public void update(long count) {
        reported.addAndGet(count);
        parent.update(count);
    }

    /**
     * Rolls back the bytes this part has reported so far (used before retrying a failed attempt).
     * A no-op when nothing has been reported yet.
     */
    public void reset() {
        long toUndo = reported.getAndSet(0);
        if (toUndo != 0) {
            parent.update(-toUndo);
        }
    }
}
