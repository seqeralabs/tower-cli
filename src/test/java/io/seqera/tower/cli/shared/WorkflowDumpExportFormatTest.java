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

package io.seqera.tower.cli.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.model.Workflow;
import org.junit.jupiter.api.Test;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WorkflowDumpExportFormatTest {

    @Test
    void testFormatDeserialization() throws JsonProcessingException {
        String json = "{\n" +
                "  \"status\" : \"FAILED\",\n" +
                "  \"ownerId\" : 3976,\n" +
                "  \"id\" : \"2sk2IPkODYQqUY\",\n" +
                "  \"submit\" : \"2023-11-20T20:39:58Z\",\n" +
                "  \"dateCreated\" : \"2023-11-20T20:39:58Z\",\n" +
                "  \"lastUpdated\" : \"2023-11-20T20:40:38Z\",\n" +
                "  \"runName\" : \"amazing_carson\",\n" +
                "  \"sessionId\" : \"fake-session-id\",\n" +
                "  \"profile\" : \"\",\n" +
                "  \"workDir\" : \"s3://jaime-testing/scratch/redacted\",\n" +
                "  \"userName\" : \"jaime-munoz\",\n" +
                "  \"revision\" : \"master\",\n" +
                "  \"commandLine\" : \"nextflow run https://github.com/nextflow-io/hello -name amazing_carson -with-tower -r master\",\n" +
                "  \"projectName\" : \"nextflow-io/hello\",\n" +
                "  \"launchId\" : \"57pR2HI7vqiY4ey1lI5fP7\",\n" +
                "  \"errorReport\" : \"ECS was unable to assume the role 'arn:aws:iam::<redacted>:role/TowerForge-<redacted>-FargateRole' that was provided for this task. Please verify that the role being passed has the proper trust relationship and permissions and that your IAM user has permissions to pass this role.\",\n" +
                "  \"exitStatus\" : -1,\n" +
                "  \"resume\" : false,\n" +
                "  \"pipelineId\" : 99024642885338,\n" +
                "  \"workspaceId\" : 89708942889986,\n" +
                "  \"userId\" : 3976,\n" +
                "  \"userEmail\" : \"jaime@mail.com\"\n" +
                "}";

        WorkflowDumpExportFormat wf = WorkflowDumpExportFormat.deserialize(json);

        assertNotNull(wf.getWorkflow());
        assertNotNull(wf.getWorkspaceId());
        assertNotNull(wf.getPipelineId());
        assertNotNull(wf.getUserId());
        assertNotNull(wf.getUserEmail());

    }

    @Test
    void testFormatSerialization() throws JsonProcessingException {

        Workflow wf = parseJson("{\n" +
                "  \"status\" : \"FAILED\",\n" +
                "  \"ownerId\" : 3976,\n" +
                "  \"id\" : \"2sk2IPkODYQqUY\",\n" +
                "  \"submit\" : \"2023-11-20T20:39:58Z\",\n" +
                "  \"dateCreated\" : \"2023-11-20T20:39:58Z\",\n" +
                "  \"lastUpdated\" : \"2023-11-20T20:40:38Z\",\n" +
                "  \"runName\" : \"amazing_carson\",\n" +
                "  \"sessionId\" : \"fake-session-id\",\n" +
                "  \"profile\" : \"\",\n" +
                "  \"workDir\" : \"s3://jaime-testing/scratch/redacted\",\n" +
                "  \"userName\" : \"jaime-munoz\",\n" +
                "  \"revision\" : \"master\",\n" +
                "  \"commandLine\" : \"nextflow run https://github.com/nextflow-io/hello -name amazing_carson -with-tower -r master\",\n" +
                "  \"projectName\" : \"nextflow-io/hello\",\n" +
                "  \"launchId\" : \"57pR2HI7vqiY4ey1lI5fP7\",\n" +
                "  \"errorReport\" : \"ECS was unable to assume the role 'arn:aws:iam::<redacted>:role/TowerForge-<redacted>-FargateRole' that was provided for this task. Please verify that the role being passed has the proper trust relationship and permissions and that your IAM user has permissions to pass this role.\",\n" +
                "  \"exitStatus\" : -1,\n" +
                "  \"resume\" : false\n" +
                "}", Workflow.class);

        WorkflowDumpExportFormat fmt = new WorkflowDumpExportFormat(
                wf,
                99024642885338L,
                89708942889986L,
                3976L,
                "jaime@mail.com"
        );
        String json = WorkflowDumpExportFormat.serialize(fmt);
        WorkflowDumpExportFormat newFmt = WorkflowDumpExportFormat.deserialize(json);

        assertEquals(fmt.getWorkflow(), newFmt.getWorkflow());
        assertEquals(fmt.getWorkspaceId(), newFmt.getWorkspaceId());
        assertEquals(fmt.getPipelineId(), newFmt.getPipelineId());
        assertEquals(fmt.getUserId(), newFmt.getUserId());
        assertEquals(fmt.getUserEmail(), newFmt.getUserEmail());
    }


}