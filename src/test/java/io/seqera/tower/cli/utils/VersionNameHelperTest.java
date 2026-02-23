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

package io.seqera.tower.cli.utils;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.PipelineVersionsApi;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionNameHelperTest {

    @Test
    void testGenerateNextVersionName_noSuffix() {
        assertEquals("pipeline-1", VersionNameHelper.generateNextVersionName("pipeline"));
    }

    @Test
    void testGenerateNextVersionName_withSuffix() {
        assertEquals("pipeline-6", VersionNameHelper.generateNextVersionName("pipeline-5"));
    }

    @Test
    void testGenerateNextVersionName_dotVersion() {
        assertEquals("v1.0-2", VersionNameHelper.generateNextVersionName("v1.0-1"));
    }

    @Test
    void testGenerateNextVersionName_multipleHyphens() {
        assertEquals("my-pipeline-name-4", VersionNameHelper.generateNextVersionName("my-pipeline-name-3"));
    }

    @Test
    void testGenerateNextVersionName_trailingHyphenNoNumber() {
        assertEquals("pipeline--1", VersionNameHelper.generateNextVersionName("pipeline-"));
    }

    @Test
    void testGenerateNextVersionName_onlyVersionNumber() {
        assertEquals("-6", VersionNameHelper.generateNextVersionName("-5"));
    }

    @Test
    void testGenerateNextVersionName_null() {
        assertNull(VersionNameHelper.generateNextVersionName(null));
    }

    @Test
    void testGenerateNextVersionName_empty() {
        assertEquals("", VersionNameHelper.generateNextVersionName(""));
    }

    @Test
    void testGenerateValidVersionName_firstCandidateValid() throws ApiException {
        // API that accepts all names
        PipelineVersionsApi api = new PipelineVersionsApi() {
            @Override
            public void validatePipelineVersionName(Long pipelineId, String name, Long wspId) {
                // success — name is valid
            }
        };

        String result = VersionNameHelper.generateValidVersionName("pipeline", 1L, 1L, api);

        assertEquals("pipeline-1", result);
    }

    @Test
    void testGenerateValidVersionName_incrementsOnConflict() throws ApiException {
        Set<String> taken = Set.of("pipeline-1");
        PipelineVersionsApi api = new PipelineVersionsApi() {
            @Override
            public void validatePipelineVersionName(Long pipelineId, String name, Long wspId) throws ApiException {
                if (taken.contains(name)) {
                    throw new ApiException(400, "Name taken");
                }
            }
        };

        String result = VersionNameHelper.generateValidVersionName("pipeline", 1L, 1L, api);

        assertEquals("pipeline-2", result);
    }

    @Test
    void testGenerateValidVersionName_withExistingSuffix() throws ApiException {
        PipelineVersionsApi api = new PipelineVersionsApi() {
            @Override
            public void validatePipelineVersionName(Long pipelineId, String name, Long wspId) {
                // success
            }
        };

        String result = VersionNameHelper.generateValidVersionName("pipeline-3", 1L, 1L, api);

        assertEquals("pipeline-4", result);
    }

    @Test
    void testGenerateValidVersionName_fallsBackToRandom() throws ApiException {
        Set<String> validated = new HashSet<>();
        PipelineVersionsApi api = new PipelineVersionsApi() {
            @Override
            public void validatePipelineVersionName(Long pipelineId, String name, Long wspId) throws ApiException {
                validated.add(name);
                throw new ApiException(400, "Name taken");
            }
        };

        String result = VersionNameHelper.generateValidVersionName("pipeline", 1L, 1L, api);

        assertTrue(result.startsWith("pipeline-"));
        assertEquals(4, result.substring("pipeline-".length()).length());
        assertEquals(10, validated.size());
    }

    @Test
    void testGenerateValidVersionName_nullBaseName() throws ApiException {
        PipelineVersionsApi api = new PipelineVersionsApi();

        String result = VersionNameHelper.generateValidVersionName(null, 1L, 1L, api);

        assertTrue(result.startsWith("-"));
    }

    @Test
    void testGenerateValidVersionName_emptyBaseName() throws ApiException {
        PipelineVersionsApi api = new PipelineVersionsApi();

        String result = VersionNameHelper.generateValidVersionName("", 1L, 1L, api);

        assertTrue(result.startsWith("-"));
    }
}
