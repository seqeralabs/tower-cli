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

package io.seqera.tower.cli.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class FilesHelperTest {

    @Test
    void readStringOrStdinReturnsNullForNullPath() throws IOException {
        assertNull(FilesHelper.readStringOrStdin(null));
    }

    @Test
    void readStringOrStdinReadsRegularFile(@TempDir Path tmp) throws IOException {
        Path file = tmp.resolve("params.json");
        Files.writeString(file, "{\"genome\":\"GRCh38\"}");
        assertEquals("{\"genome\":\"GRCh38\"}", FilesHelper.readStringOrStdin(file));
    }

    @Test
    void readStringOrStdinPreservesInputVerbatim() throws IOException {
        // No line-ending rewrite, no trailing newline injection, UTF-8 preserved.
        String input = "process {\n    executor = 'awsbatch'\n}\n";
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        assertEquals(input, FilesHelper.readFromStdin(in));
    }

    @Test
    void readStringOrStdinPreservesUtf8MultiByte() throws IOException {
        // Non-ASCII content should survive — guards against platform-default charset.
        String input = "name: \"αβγ — 日本語\"\n";
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        assertEquals(input, FilesHelper.readFromStdin(in));
    }

    @Test
    void readFromStdinDoesNotCloseInput() throws IOException {
        // Reading must not close the underlying stream — guards against the previous
        // try-with-resources bug where closing System.in broke subsequent reads from
        // the same launch invocation.
        boolean[] closed = {false};
        InputStream in = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() {
                closed[0] = true;
            }
        };
        FilesHelper.readFromStdin(in);
        assertFalse(closed[0], "readFromStdin must not close the input stream");
    }

    @Test
    void readStringOrStdinHandlesEmptyInput() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertEquals("", FilesHelper.readFromStdin(in));
    }
}
