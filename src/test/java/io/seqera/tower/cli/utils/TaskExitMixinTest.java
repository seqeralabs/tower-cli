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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.seqera.tower.JSON;
import io.seqera.tower.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link TaskExitMixin} and {@link LenientIntegerDeserializer}.
 * Verifies that the Task exit field is correctly deserialised when the Platform API
 * returns it as a string, an integer, or null.
 *
 * FIXME: Workaround for Platform versions before 26.x. Remove once those versions are phased out (see #578).
 */
class TaskExitMixinTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JSON().getContext(Task.class);
        mapper.addMixIn(Task.class, TaskExitMixin.class);
    }

    @Test
    void testExitAsStringIsDeserialised() throws IOException {
        String json = loadResource("runs/task_object_exit_as_string");
        Task task = mapper.readValue(json, Task.class);
        assertEquals(0, task.getExit());
    }

    @Test
    void testExitAsIntegerIsDeserialised() throws IOException {
        String json = loadResource("runs/task_object");
        Task task = mapper.readValue(json, Task.class);
        assertEquals(0, task.getExit());
    }

    @Test
    void testExitAsNullIsDeserialised() throws IOException {
        String json = "{\"exit\": null}";
        Task task = mapper.readValue(json, Task.class);
        assertNull(task.getExit());
    }

    private String loadResource(String name) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/runcmd/" + name + ".json")) {
            if (is == null) {
                throw new IOException("Resource not found: " + name);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
