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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowLaunchResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelHelperTest {

    @Test
    void createLaunchRequestCopiesEveryLaunchField() throws JsonProcessingException {
        LaunchDbDto launch = new LaunchDbDto()
                .id("5nmCvXcarkvv8tELMF4KyY")
                .computeEnv(computeEnv("4X7YrYJp9B1d1DUpfur7DS"))
                .pipeline("https://github.com/nf-core/sarek")
                .workDir("/efs")
                .revision("dev")
                .commitId("f5b3a9c")
                .sessionId("d77a5ac4-1a25-45d6-bab0-358605abc332")
                .configProfiles(List.of("test", "docker"))
                .configText("process.cpus = 2")
                .towerConfig("tower {}")
                .paramsText("outdir: /results")
                .preRunScript("echo pre")
                .postRunScript("echo post")
                .mainScript("main.nf")
                .entryName("NFCORE_SAREK")
                .schemaName("nextflow_schema.json")
                .pipelineSchemaId(42L)
                .userSecrets(List.of("USER_SECRET"))
                .workspaceSecrets(List.of("WORKSPACE_SECRET"))
                .resume(true)
                .pullLatest(true)
                .stubRun(true)
                .optimizationId("rOYdwTnmTaRCJjUq")
                .optimizationTargets("cpus, memory")
                .headJobCpus(4)
                .headJobMemoryMb(8192)
                .launchContainer("quay.io/seqeralabs/nf-launcher:j17-24.10.0")
                .nextflowVersion("26.04.6")
                .outputDir("/outputs")
                .syntaxParser(LaunchDbDto.SyntaxParserEnum.V2);

        WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(launch);

        assertEquals("5nmCvXcarkvv8tELMF4KyY", request.getId());
        assertEquals("4X7YrYJp9B1d1DUpfur7DS", request.getComputeEnvId());
        assertEquals("https://github.com/nf-core/sarek", request.getPipeline());
        assertEquals("/efs", request.getWorkDir());
        assertEquals("dev", request.getRevision());
        assertEquals("f5b3a9c", request.getCommitId());
        assertEquals("d77a5ac4-1a25-45d6-bab0-358605abc332", request.getSessionId());
        assertEquals(List.of("test", "docker"), request.getConfigProfiles());
        assertEquals("process.cpus = 2", request.getConfigText());
        assertEquals("tower {}", request.getTowerConfig());
        assertEquals("outdir: /results", request.getParamsText());
        assertEquals("echo pre", request.getPreRunScript());
        assertEquals("echo post", request.getPostRunScript());
        assertEquals("main.nf", request.getMainScript());
        assertEquals("NFCORE_SAREK", request.getEntryName());
        assertEquals("nextflow_schema.json", request.getSchemaName());
        assertEquals(42L, request.getPipelineSchemaId());
        assertEquals(List.of("USER_SECRET"), request.getUserSecrets());
        assertEquals(List.of("WORKSPACE_SECRET"), request.getWorkspaceSecrets());
        assertTrue(request.getResume());
        assertTrue(request.getPullLatest());
        assertTrue(request.getStubRun());
        assertEquals("rOYdwTnmTaRCJjUq", request.getOptimizationId());
        assertEquals("cpus, memory", request.getOptimizationTargets());
        assertEquals(4, request.getHeadJobCpus());
        assertEquals(8192, request.getHeadJobMemoryMb());
        assertEquals("quay.io/seqeralabs/nf-launcher:j17-24.10.0", request.getLaunchContainer());
        assertEquals("26.04.6", request.getNextflowVersion());
        assertEquals("/outputs", request.getOutputDir());
        assertEquals(WorkflowLaunchRequest.SyntaxParserEnum.V2, request.getSyntaxParser());
    }

    @Test
    void createLaunchRequestFromWorkflowLaunchResponseCopiesSyntaxParser() throws JsonProcessingException {
        WorkflowLaunchResponse launch = new WorkflowLaunchResponse()
                .computeEnv(computeEnv("2lu3NFms1qRvwTVnrs1yhg"))
                .pipeline("https://github.com/nf-core/rnaseq")
                .nextflowVersion("26.04.6")
                .syntaxParser(WorkflowLaunchResponse.SyntaxParserEnum.V2);

        WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(launch);

        assertEquals("2lu3NFms1qRvwTVnrs1yhg", request.getComputeEnvId());
        assertEquals("https://github.com/nf-core/rnaseq", request.getPipeline());
        assertEquals("26.04.6", request.getNextflowVersion());
        assertEquals(WorkflowLaunchRequest.SyntaxParserEnum.V2, request.getSyntaxParser());
    }

    @Test
    void createLaunchRequestKeepsUndefinedNullableFieldsOutOfThePayload() throws JsonProcessingException {
        LaunchDbDto launch = new LaunchDbDto().pipeline("https://github.com/nf-core/sarek");

        String json = serialize(ModelHelper.createLaunchRequest(launch));

        assertFalse(json.contains("syntaxParser"), json);
        assertFalse(json.contains("nextflowVersion"), json);
        assertFalse(json.contains("outputDir"), json);
    }

    @Test
    void createLaunchRequestPreservesExplicitlyNullNullableFields() throws JsonProcessingException {
        LaunchDbDto launch = new LaunchDbDto()
                .pipeline("https://github.com/nf-core/sarek")
                .syntaxParser(null)
                .outputDir(null);

        WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(launch);

        assertNull(request.getSyntaxParser());
        assertNull(request.getOutputDir());
        String json = serialize(request);
        assertTrue(json.contains("\"syntaxParser\" : null"), json);
        assertTrue(json.contains("\"outputDir\" : null"), json);
    }

    @Test
    void createLaunchRequestWithoutComputeEnvHasNoComputeEnvId() {
        WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(new LaunchDbDto().pipeline("https://github.com/nf-core/sarek"));

        assertNull(request.getComputeEnvId());
    }

    // ComputeEnvComputeConfig exposes no setter for 'id', so the fixture is built from JSON.
    private static ComputeEnvComputeConfig computeEnv(String id) throws JsonProcessingException {
        return new JSON().getContext(ComputeEnvComputeConfig.class)
                .readValue(String.format("{\"id\":\"%s\"}", id), ComputeEnvComputeConfig.class);
    }

    private static String serialize(WorkflowLaunchRequest request) throws JsonProcessingException {
        return new JSON().getContext(WorkflowLaunchRequest.class)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(request);
    }
}
