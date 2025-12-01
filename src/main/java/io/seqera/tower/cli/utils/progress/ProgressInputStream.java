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

import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {
        private final InputStream source;
        private final ProgressTracker tracker;

        public ProgressInputStream(InputStream source, ProgressTracker tracker) {
            this.source = source;
            this.tracker = tracker;
        }

        @Override
        public int read() throws IOException {
            int b = source.read();
            if (b != -1) tracker.update(1);
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = source.read(b, off, len);
            if (count > 0) tracker.update(count);
            return count;
        }

        @Override
        public void close() throws IOException {
            source.close();
        }
    }
