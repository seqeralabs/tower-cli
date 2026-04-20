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

package io.seqera.tower.cli.utils.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliMetadataExtractorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGenerateValidJsonWithExpectedTopLevelStructure() throws Exception {
        CliMetadataExtractor extractor = new CliMetadataExtractor();

        JsonNode root = objectMapper.readTree(extractor.extractMetadataAsJson(CliMetadataExtractor.buildRootSpec()));

        assertTrue(root.has("metadata"));
        assertTrue(root.has("hierarchy"));
        assertTrue(root.has("commands"));
        assertEquals("java-reflection", root.path("metadata").path("extractor_type").asText());
        assertEquals("tw", root.path("hierarchy").path("name").asText());
    }

    @Test
    void shouldIncludeLaunchCommandAndSkipBuiltInHelpOptions() throws Exception {
        CliMetadataExtractor extractor = new CliMetadataExtractor();

        JsonNode root = objectMapper.readTree(extractor.extractMetadataAsJson(CliMetadataExtractor.buildRootSpec()));
        JsonNode commands = root.path("commands");
        JsonNode launch = commands.path("tw launch");

        assertTrue(launch.isObject());
        assertEquals("launch", launch.path("name").asText());
        assertTrue(hasOptionNamed(launch.path("options"), "--params-file"));
        assertFalse(hasOptionNamed(commands.path("tw").path("options"), "--help"));
        assertFalse(hasOptionNamed(commands.path("tw").path("options"), "--version"));
    }

    private boolean hasOptionNamed(JsonNode options, String name) {
        for (JsonNode option : options) {
            for (JsonNode optionName : option.path("names")) {
                if (name.equals(optionName.asText())) {
                    return true;
                }
            }
        }

        return false;
    }
}
