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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class ProgressTrackingBodyPublisher implements HttpRequest.BodyPublisher {
    private final byte[] data;
    private final ProgressTracker tracker;

    public ProgressTrackingBodyPublisher(byte[] data, ProgressTracker tracker) {
        this.data = data;
        this.tracker = tracker;
    }

    @Override
    public long contentLength() {
        return data.length;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {
        // Wrap byte array in InputStream and monitor progress
        InputStream input = new ProgressInputStream(new ByteArrayInputStream(data), tracker);
        subscriber.onSubscribe(new InputStreamSubscription(input, subscriber));
    }

    private static class InputStreamSubscription implements Flow.Subscription {
        private final InputStream input;
        private final Flow.Subscriber<? super ByteBuffer> subscriber;
        private final int bufferSize = 8192;
        private boolean completed = false;

        public InputStreamSubscription(InputStream input, Flow.Subscriber<? super ByteBuffer> subscriber) {
            this.input = input;
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            try {
                for (long i = 0; i < n && !completed; i++) {
                    byte[] buffer = new byte[bufferSize];
                    int read = input.read(buffer);
                    if (read == -1) {
                        completed = true;
                        subscriber.onComplete();
                        input.close();
                    } else {
                        subscriber.onNext(ByteBuffer.wrap(buffer, 0, read));
                    }
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }
        }

        @Override
        public void cancel() {
            try {
                input.close();
            } catch (IOException ignored) {}
        }
    }
}