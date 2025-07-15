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

package io.seqera.tower.cli.commands.data.links.upload;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;

public abstract class AbstractProviderUploader implements CloudProviderUploader {

    static final Integer MULTI_UPLOAD_PART_SIZE_IN_BYTES = 250 * 1024 * 1024; // 250 MB

    protected byte[] getChunk(File file, int index) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long start = (long) index * MULTI_UPLOAD_PART_SIZE_IN_BYTES;
            long end = Math.min(start + MULTI_UPLOAD_PART_SIZE_IN_BYTES, file.length());
            int length = (int) (end - start);

            byte[] buffer = new byte[length];
            raf.seek(start);
            raf.readFully(buffer);

            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
} 