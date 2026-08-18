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

/**
 * A sink that byte-streaming components ({@link ProgressTrackingBodyPublisher}/{@link ProgressInputStream})
 * report uploaded byte counts to as a request body is transmitted. Implemented by {@link PartProgress}
 * so the streaming code is decoupled from the shared {@link ProgressTracker}.
 */
public interface ProgressSink {
    void update(long count);
}
